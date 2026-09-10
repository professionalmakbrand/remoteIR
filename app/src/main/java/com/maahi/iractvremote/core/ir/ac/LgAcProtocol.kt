package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * LG AC 28-bit Infrared Protocol Synthesizer (38kHz).
 * Synthesizes 28-bit frame with 4-bit nibble checksum.
 */
object LgAcProtocol {
    const val FREQUENCY = 38000

    private const val HDR_MARK = 8500
    private const val HDR_SPACE = 4250
    private const val BIT_MARK = 550
    private const val ONE_SPACE = 1600
    private const val ZERO_SPACE = 550
    private const val FOOTER_SPACE = 10000

    fun encode(state: AcState): IntArray {
        val address = 0x88

        val modeVal = when (state.mode) {
            AcMode.COOL -> 0x0
            AcMode.DRY -> 0x1
            AcMode.FAN -> 0x2
            AcMode.AUTO -> 0x3
            AcMode.HEAT -> 0x4
        }

        val tempOffset = (state.temp - 15).coerceIn(0, 15)

        val fanVal = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0x5
            FanSpeed.LOW -> 0x0
            FanSpeed.MED -> 0x2
            FanSpeed.HIGH -> 0x4
        }

        val powerVal = if (state.power) 0x0 else 0xC

        // Checksum is 4-bit sum of all preceding nibbles mod 16
        val checksum = (
            (address shr 4) +
            (address and 0x0F) +
            powerVal +
            modeVal +
            tempOffset +
            fanVal
        ) and 0x0F

        // 28-bit data word
        val data = (address.toLong() shl 20) or
            (powerVal.toLong() shl 16) or
            (modeVal.toLong() shl 12) or
            (tempOffset.toLong() shl 8) or
            (fanVal.toLong() shl 4) or
            checksum.toLong()

        val pulses = mutableListOf<Int>()
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        // Transmit 28 bits (MSB first)
        for (bit in 27 downTo 0) {
            pulses.add(BIT_MARK)
            if (((data shr bit) and 1L) != 0L) {
                pulses.add(ONE_SPACE)
            } else {
                pulses.add(ZERO_SPACE)
            }
        }

        pulses.add(BIT_MARK)
        pulses.add(FOOTER_SPACE)

        return pulses.toIntArray()
    }
}
