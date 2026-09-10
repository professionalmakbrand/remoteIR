package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed

/**
 * Hitachi 28-Byte Protocol Engine with 30ms Wake-up Preamble.
 * Emits the 28-byte frame (224 bits) preceded by the 30ms mark / 50ms space leader.
 */
object HitachiAc28Protocol {

    const val FREQUENCY = 38000

    private const val LEADER_MARK = 29966
    private const val LEADER_SPACE = 50096
    private const val HDR_MARK = 3380
    private const val HDR_SPACE = 1705
    private const val BIT_MARK = 400
    private const val ONE_SPACE = 1250
    private const val ZERO_SPACE = 400
    private const val FOOTER_MARK = 400
    private const val MIN_GAP = 40000

    fun encode(state: AcState): IntArray {
        val raw = IntArray(28)

        raw[0] = 0x01
        raw[1] = 0x10
        raw[2] = 0x30
        raw[3] = 0x40
        raw[4] = 0xBF
        raw[5] = 0x01
        raw[6] = 0xFE
        raw[7] = 0x11
        raw[8] = 0x12

        // Mode: Cool = 0x41, Heat = 0x43, Dry = 0x42, Fan = 0x44, Off = 0xE0
        val modeByte = if (!state.power) {
            0xE0
        } else {
            when (state.mode) {
                AcMode.COOL -> 0x41
                AcMode.HEAT -> 0x43
                AcMode.DRY -> 0x42
                AcMode.FAN -> 0x44
                AcMode.AUTO -> 0x45
            }
        }
        raw[9] = modeByte

        // Temp: (temp << 2) | 0x01
        val temp = state.temp.coerceIn(16, 32)
        raw[10] = ((temp shl 2) or 0x01) and 0xFF

        // Fan: Auto = 0x01, Low = 0x02, Med = 0x03, High = 0x04
        raw[11] = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0x01
            FanSpeed.LOW -> 0x02
            FanSpeed.MED -> 0x03
            FanSpeed.HIGH -> 0x04
        }

        // Swing: on = 0x81, off = 0x01
        raw[12] = if (state.swing) 0x81 else 0x01
        raw[24] = 0x01

        // Checksum at raw[27]
        var sum = 0
        for (i in 0..26) {
            sum += raw[i]
        }
        raw[27] = (sum and 0xFF)

        // Modulate (LSBF)
        val pulses = mutableListOf<Int>()
        pulses.add(LEADER_MARK)
        pulses.add(LEADER_SPACE)
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        for (b in raw) {
            for (bit in 0..7) {
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
}
