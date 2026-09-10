package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Gree AC Infrared Protocol Synthesizer (38kHz).
 * Synthesizes 64-bit dual-block frame with modulo-8 checksum.
 */
object GreeAcProtocol {
    const val FREQUENCY = 38000

    private const val HDR_MARK = 9000
    private const val HDR_SPACE = 4500
    private const val BIT_MARK = 620
    private const val ONE_SPACE = 1600
    private const val ZERO_SPACE = 540
    private const val BLOCK_SPACE = 20000
    private const val FOOTER_SPACE = 40000

    fun encode(state: AcState): IntArray {
        // Block 1 (4 bytes): Mode, Power, Fan, Swing, Temp
        val block1 = IntArray(4)

        val modeVal = when (state.mode) {
            AcMode.AUTO -> 0
            AcMode.COOL -> 1
            AcMode.DRY -> 2
            AcMode.FAN -> 3
            AcMode.HEAT -> 4
        }
        val powerVal = if (state.power) 1 else 0
        val fanVal = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0
            FanSpeed.LOW -> 1
            FanSpeed.MED -> 2
            FanSpeed.HIGH -> 3
        }
        val swingVal = if (state.swing) 1 else 0

        // Byte 0: Mode (bits 0..2), Power (bit 3), Fan (bits 4..5), Swing (bit 6)
        block1[0] = (modeVal and 0x07) or (powerVal shl 3) or (fanVal shl 4) or (swingVal shl 6)

        // Byte 1: Temperature (16-30 °C, offset 16)
        val tempOffset = (state.temp - 16).coerceIn(0, 14)
        block1[1] = tempOffset and 0x0F

        // Byte 2: Defaults
        block1[2] = 0x20

        // Byte 3: Defaults
        block1[3] = 0x50

        // Block 2 (4 bytes): Swing angles, Turbo, Light, Checksum
        val block2 = IntArray(4)
        block2[0] = 0x00
        block2[1] = if (state.turbo) 0x10 else 0x00
        block2[2] = 0x20

        // Checksum: (mode - 1) + (temp - 16) + 5 + ... mod 16
        val checksum = (
            (modeVal and 0x07) +
            (tempOffset and 0x0F) +
            (fanVal and 0x03) +
            (powerVal) +
            (if (state.swing) 1 else 0) +
            (if (state.turbo) 1 else 0) +
            5
        ) and 0x0F

        block2[3] = (checksum shl 4)

        // Modulate into pulses
        val pulses = mutableListOf<Int>()

        // Send Block 1
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in block1) {
            for (bit in 0..7) {
                pulses.add(BIT_MARK)
                if ((b and (1 shl bit)) != 0) {
                    pulses.add(ONE_SPACE)
                } else {
                    pulses.add(ZERO_SPACE)
                }
            }
        }

        // Inter-block separator
        pulses.add(BIT_MARK)
        pulses.add(BLOCK_SPACE)

        // Send Block 2
        for (b in block2) {
            for (bit in 0..7) {
                pulses.add(BIT_MARK)
                if ((b and (1 shl bit)) != 0) {
                    pulses.add(ONE_SPACE)
                } else {
                    pulses.add(ZERO_SPACE)
                }
            }
        }

        pulses.add(BIT_MARK)
        pulses.add(FOOTER_SPACE)

        return pulses.toIntArray()
    }
}
