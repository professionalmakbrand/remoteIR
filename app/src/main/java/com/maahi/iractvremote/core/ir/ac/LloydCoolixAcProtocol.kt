package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Lloyd / Coolix AC Infrared Protocol Synthesizer (38kHz).
 * Synthesizes 48-bit frame with inverted byte pairs for error validation.
 */
object LloydCoolixAcProtocol {
    const val FREQUENCY = 38000

    private const val HDR_MARK = 4500
    private const val HDR_SPACE = 4500
    private const val BIT_MARK = 560
    private const val ONE_SPACE = 1680
    private const val ZERO_SPACE = 560
    private const val FOOTER_SPACE = 5200

    fun encode(state: AcState): IntArray {
        // 3 control bytes
        val byte0 = 0xB2

        val byte1: Int
        val byte2: Int

        if (!state.power) {
            // Power off code
            byte1 = 0x7B
            byte2 = 0xE0
        } else {
            // Temperature mapping (17-30 °C standard for Coolix/Lloyd)
            val tempVal = when (state.temp) {
                16 -> 0x00
                17 -> 0x00
                18 -> 0x01
                19 -> 0x03
                20 -> 0x02
                21 -> 0x06
                22 -> 0x07
                23 -> 0x05
                24 -> 0x04
                25 -> 0x0C
                26 -> 0x0D
                27 -> 0x09
                28 -> 0x08
                29 -> 0x0A
                30 -> 0x0B
                else -> 0x04
            }

            // Mode
            val modeVal = when (state.mode) {
                AcMode.COOL -> 0x00
                AcMode.DRY -> 0x01
                AcMode.AUTO -> 0x02
                AcMode.HEAT -> 0x03
                AcMode.FAN -> 0x04
            }

            byte1 = (tempVal shl 4) or (modeVal and 0x0F)

            // Fan speed
            val fanVal = when (state.fanSpeed) {
                FanSpeed.AUTO -> 0x05
                FanSpeed.LOW -> 0x04
                FanSpeed.MED -> 0x02
                FanSpeed.HIGH -> 0x01
            }

            byte2 = if (state.swing) (fanVal or 0x80) else fanVal
        }

        // Construct 6 bytes: [byte0, ~byte0, byte1, ~byte1, byte2, ~byte2]
        val payload = intArrayOf(
            byte0, byte0.inv() and 0xFF,
            byte1, byte1.inv() and 0xFF,
            byte2, byte2.inv() and 0xFF
        )

        val pulses = mutableListOf<Int>()
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in payload) {
            // MSB first for Coolix
            for (bit in 7 downTo 0) {
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
