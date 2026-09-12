package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Authentic Voltas AC Native Infrared Protocol Synthesizer (38kHz).
 *
 * Transmits an 80-bit (10-byte) frame with:
 * - 0 µs header (starts directly with bit 0 mark)
 * - 1026 µs bit mark
 * - 2550 µs bit 1 space / 550 µs bit 0 space
 * - MSB transmitted first per byte
 * - 1026 µs footer mark followed by 40,000 µs space
 * - One's complement 8-bit checksum: ~(sum(bytes[0..8])) & 0xFF
 *
 * Used extensively in Voltas Inverter, Split, and Window ACs across India
 * (e.g., 183V MZJ3, 122LZF, 1.5T 3-Star/5-Star All-Weather series).
 */
object VoltasAcProtocol {

    const val FREQUENCY = 38000

    private const val BIT_MARK = 1026
    private const val BIT_ONE_SPACE = 2550
    private const val BIT_ZERO_SPACE = 550
    private const val FOOTER_MARK = 1026
    private const val FOOTER_SPACE = 40000

    fun encode(state: AcState): IntArray {
        val bytes = IntArray(10)

        // Byte 0: SwingH / Default prefix 0x33
        bytes[0] = 0x33

        // Byte 1: FanSpeed (high nibble) and Mode (low nibble)
        val modeNibble = when (state.mode) {
            AcMode.COOL -> 0x08
            AcMode.DRY -> 0x04
            AcMode.HEAT -> 0x02
            AcMode.FAN -> 0x01
            AcMode.AUTO -> 0x08
        }
        val fanNibble = when (state.fanSpeed) {
            FanSpeed.LOW -> 0x80
            FanSpeed.MED -> 0x40
            FanSpeed.HIGH -> 0x20
            FanSpeed.AUTO -> 0xE0
        }
        bytes[1] = (fanNibble or modeNibble) and 0xFF

        // Byte 2: Power (bit 7), Turbo (bit 5), Fixed (bit 3), Swing (bits 0..2)
        var b2 = 0x08
        if (state.power) {
            b2 = b2 or 0x80
        }
        if (state.swing) {
            b2 = b2 or 0x07
        }
        if (state.turbo) {
            b2 = b2 or 0x20
        }
        bytes[2] = b2 and 0xFF

        // Byte 3: Fixed prefix 0x10 | Temperature offset (temp - 16, 16..30°C -> 0..14)
        val tempOffset = (state.temp - 16).coerceIn(0, 14)
        bytes[3] = 0x10 or tempOffset

        // Bytes 4..8: Standard Voltas timings and flags
        bytes[4] = 0x3B // OnTimer default
        bytes[5] = 0x3B // OffTimer default
        bytes[6] = 0x3B // Fixed 0x3B
        bytes[7] = 0x11 // TimerHours default
        bytes[8] = 0x00 // Flags default

        // Byte 9: One's complement checksum of bytes 0..8
        var sum = 0
        for (i in 0..8) {
            sum += bytes[i]
        }
        bytes[9] = (sum.inv()) and 0xFF

        // Modulate into 162 microsecond pulses (MSB first)
        val pulses = mutableListOf<Int>()
        for (b in bytes) {
            for (bit in 7 downTo 0) {
                pulses.add(BIT_MARK)
                if (((b shr bit) and 1) == 1) {
                    pulses.add(BIT_ONE_SPACE)
                } else {
                    pulses.add(BIT_ZERO_SPACE)
                }
            }
        }
        pulses.add(FOOTER_MARK)
        pulses.add(FOOTER_SPACE)

        return pulses.toIntArray()
    }
}
