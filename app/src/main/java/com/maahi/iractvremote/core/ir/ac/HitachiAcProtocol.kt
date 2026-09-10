package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Hitachi AC Infrared Protocol Synthesizer (38kHz).
 * Synthesizes dynamic multi-byte frame with bitwise pulse-width modulation.
 */
object HitachiAcProtocol {
    const val FREQUENCY = 38000

    private const val HDR_MARK = 3300
    private const val HDR_SPACE = 1700
    private const val BIT_MARK = 400
    private const val ONE_SPACE = 1250
    private const val ZERO_SPACE = 400
    private const val FOOTER_SPACE = 40000

    fun encode(state: AcState): IntArray {
        // Construct 13 payload bytes
        val bytes = IntArray(13)

        // Hitachi header signature
        bytes[0] = 0x01
        bytes[1] = 0x10
        bytes[2] = 0x00
        bytes[3] = 0x40
        bytes[4] = 0xBF
        bytes[5] = 0xFF
        bytes[6] = 0x00
        bytes[7] = 0xCC

        // Mode & Power byte
        val modeVal = when (state.mode) {
            AcMode.COOL -> 0x41
            AcMode.HEAT -> 0x43
            AcMode.DRY -> 0x42
            AcMode.FAN -> 0x44
            AcMode.AUTO -> 0x45
        }
        bytes[8] = if (state.power) modeVal else 0xE0

        // Temperature byte (16-30 °C)
        val tempClamped = state.temp.coerceIn(16, 30)
        bytes[9] = (tempClamped shl 2) or 0x01

        // Fan speed
        bytes[10] = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0x01
            FanSpeed.LOW -> 0x02
            FanSpeed.MED -> 0x03
            FanSpeed.HIGH -> 0x04
        }

        // Swing byte
        bytes[11] = if (state.swing) 0x81 else 0x01

        // Checksum byte: Inverted sum of control bytes (bytes 8..11)
        var sum = 0
        for (i in 8..11) {
            sum += bytes[i]
        }
        bytes[12] = (256 - (sum and 0xFF)) and 0xFF

        // Modulate into IR mark/space timings
        val pulses = mutableListOf<Int>()
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in bytes) {
            // LSB first
            for (bit in 0..7) {
                pulses.add(BIT_MARK)
                if ((b and (1 shl bit)) != 0) {
                    pulses.add(ONE_SPACE)
                } else {
                    pulses.add(ZERO_SPACE)
                }
            }
        }

        // Trailing pulse
        pulses.add(BIT_MARK)
        pulses.add(FOOTER_SPACE)

        return pulses.toIntArray()
    }
}
