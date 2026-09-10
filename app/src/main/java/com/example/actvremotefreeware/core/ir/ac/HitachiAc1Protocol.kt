package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed

/**
 * Hitachi AC1 Protocol Engine (13 bytes / 104 bits).
 * Used by popular Indian market Hitachi split ACs (Kaze, i-Clean, Kashikoi)
 * with remote models R-LT0541-HTA-A / R-LT0541-HTA-B.
 */
object HitachiAc1Protocol {

    const val FREQUENCY = 38000

    private const val HDR_MARK = 3400
    private const val HDR_SPACE = 3400
    private const val BIT_MARK = 400
    private const val ONE_SPACE = 1250
    private const val ZERO_SPACE = 500
    private const val FOOTER_MARK = 400
    private const val MIN_GAP = 40000

    private const val MODE_DRY = 2
    private const val MODE_FAN = 4
    private const val MODE_COOL = 6
    private const val MODE_HEAT = 9
    private const val MODE_AUTO = 14

    private const val FAN_AUTO = 1
    private const val FAN_HIGH = 2
    private const val FAN_MED = 4
    private const val FAN_LOW = 8

    fun encode(state: AcState): IntArray {
        val raw = IntArray(13)

        // Known good default state
        raw[0] = 0xB2
        raw[1] = 0xAE
        raw[2] = 0x4D
        raw[3] = 0x91 // Model A (0b10)
        raw[4] = 0xF0
        raw[5] = 0xE1
        raw[6] = 0xA4
        raw[7] = 0x00
        raw[8] = 0x00
        raw[9] = 0x00
        raw[10] = 0x00
        raw[11] = 0x61
        raw[12] = 0x24

        // Byte 5: Mode & Fan
        val modeVal = when (state.mode) {
            AcMode.COOL -> MODE_COOL
            AcMode.HEAT -> MODE_HEAT
            AcMode.DRY -> MODE_DRY
            AcMode.FAN -> MODE_FAN
            AcMode.AUTO -> MODE_AUTO
        }

        val fanVal = when (state.fanSpeed) {
            FanSpeed.LOW -> FAN_LOW
            FanSpeed.MED -> FAN_MED
            FanSpeed.HIGH -> FAN_HIGH
            FanSpeed.AUTO -> FAN_AUTO
        }
        raw[5] = (fanVal and 0x0F) or ((modeVal and 0x0F) shl 4)

        // Byte 6: Temperature (5 bits reversed in bits 2..6, delta = 7)
        val temp = state.temp.coerceIn(16, 32)
        val tempDelta = (temp - 7).coerceIn(0, 31)
        val reversedTemp = reverseBits(tempDelta, 5)
        raw[6] = (raw[6] and 0x83) or (reversedTemp shl 2)

        // Byte 11: Power & Swing
        var byte11 = 0x00
        if (state.power) {
            byte11 = byte11 or (1 shl 5) // Power bit
            byte11 = byte11 or (1 shl 4) // PowerToggle bit
        }
        if (state.swing) {
            byte11 = byte11 or (1 shl 6) // SwingV
            byte11 = byte11 or 1         // SwingToggle
        }
        raw[11] = byte11

        // Byte 12: Checksum (across bytes 5..11)
        var sum = 0
        for (i in 5..11) {
            val lowNibble = raw[i] and 0x0F
            val highNibble = (raw[i] shr 4) and 0x0F
            sum += reverseBits(lowNibble, 4)
            sum += reverseBits(highNibble, 4)
        }
        raw[12] = reverseBits(sum and 0xFF, 8)

        // Modulate (MSB First as per sendHitachiAC1)
        val pulses = mutableListOf<Int>()
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in raw) {
            for (bit in 7 downTo 0) {
                pulses.add(BIT_MARK)
                if (((b shr bit) and 1) == 1) {
                    pulses.add(ONE_SPACE)
                } else {
                    pulses.add(ZERO_SPACE)
                }
            }
        }

        pulses.add(FOOTER_MARK)
        pulses.add(MIN_GAP)

        return pulses.toIntArray()
    }

    private fun reverseBits(input: Int, bits: Int): Int {
        var value = input
        var result = 0
        for (i in 0 until bits) {
            result = (result shl 1) or (value and 1)
            value = value shr 1
        }
        return result
    }
}
