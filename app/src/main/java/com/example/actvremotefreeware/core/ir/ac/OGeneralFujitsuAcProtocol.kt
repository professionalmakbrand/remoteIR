package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed

/**
 * O General / Fujitsu General AC Infrared Protocol Synthesizer (38kHz).
 * Synthesizes 128-bit frame (16 bytes) with manufacturer preamble and 8-bit complement checksum.
 */
object OGeneralFujitsuAcProtocol {
    const val FREQUENCY = 38000

    private const val HDR_MARK = 3320
    private const val HDR_SPACE = 1580
    private const val BIT_MARK = 420
    private const val ONE_SPACE = 1240
    private const val ZERO_SPACE = 420
    private const val FOOTER_SPACE = 8000

    fun encode(state: AcState): IntArray {
        val bytes = IntArray(16)

        // Fujitsu / O General manufacturer preamble
        bytes[0] = 0x14
        bytes[1] = 0x63
        bytes[2] = 0x00
        bytes[3] = 0x10
        bytes[4] = 0x10

        // Power command
        bytes[5] = if (state.power) 0xFE else 0x02

        // Mode
        bytes[6] = when (state.mode) {
            AcMode.AUTO -> 0x00
            AcMode.COOL -> 0x01
            AcMode.DRY -> 0x02
            AcMode.FAN -> 0x03
            AcMode.HEAT -> 0x04
        }

        // Temperature (16-30 °C)
        val tempOffset = (state.temp - 16).coerceIn(0, 14)
        bytes[7] = (tempOffset shl 4) or 0x00

        // Fan Speed
        bytes[8] = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0x00
            FanSpeed.HIGH -> 0x01
            FanSpeed.MED -> 0x02
            FanSpeed.LOW -> 0x03
        }

        // Swing
        bytes[9] = if (state.swing) 0x01 else 0x00

        // Bytes 10..14 defaults
        bytes[10] = 0x00
        bytes[11] = 0x00
        bytes[12] = 0x00
        bytes[13] = 0x00
        bytes[14] = 0x20

        // Checksum: modulo 256 sum complement
        var sum = 0
        for (i in 0..14) {
            sum += bytes[i]
        }
        bytes[15] = (0x100 - (sum and 0xFF)) and 0xFF

        // Modulate into pulses (LSB first)
        val pulses = mutableListOf<Int>()
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in bytes) {
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
