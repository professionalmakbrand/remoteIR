package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed
import java.util.Calendar

/**
 * Ported and enhanced from Android-AC-Remote-master.
 * Dynamically synthesizes the 148-element Carrier AC pulse frame with 4-bit checksum.
 */
object CarrierAcProtocol {
    const val FREQUENCY = 38000
    private const val LOW = 552
    private const val HIGH = 1683

    // Base template array of 148 intervals
    private val BASE_TEMPLATE = intArrayOf(
        8835, 4497, 552, 552, 552, 552, 552, 1683, 552, 552, 552, 552, 552, 552, 552, 552,
        552, 1683, 552, 1683, 552, 552, 552, 1683, 552, 552, 552, 1683, 552, 552, 552, 1683,
        552, 552, 552, 1683, 552, 1683, 552, 1683, 552, 1683, 552, 552, 552, 1683, 552, 1683,
        552, 552, 552, 1683, 552, 552, 552, 552, 552, 1683, 552, 552, 552, 552, 552, 552,
        552, 552, 552, 1683, 552, 552, 552, 1683, 552, 552, 552, 1683, 552, 552, 552, 552,
        552, 552, 552, 1683, 552, 552, 552, 1683, 552, 1683, 552, 1683, 552, 552, 552, 1683,
        552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 552, 1683, 552, 1683,
        552, 552, 552, 552, 552, 552, 552, 552, 552, 1683, 552, 552, 552, 1683, 552, 552,
        552, 1683, 552, 552, 552, 1683, 552, 1683, 552, 552, 552, 552, 552, 552, 552, 552,
        552, 552, 552, 50067
    )

    fun encode(state: AcState): IntArray {
        val raw = BASE_TEMPLATE.clone()

        val tempOffset = (state.temp - 16).coerceIn(0, 14)
        val modeVal = when (state.mode) {
            AcMode.DRY -> 0
            AcMode.HEAT -> 1
            AcMode.COOL -> 2
            AcMode.FAN, AcMode.AUTO -> 3
        }
        val fanVal = when (state.fanSpeed) {
            FanSpeed.AUTO -> 0
            FanSpeed.LOW -> 1
            FanSpeed.MED -> 2
            FanSpeed.HIGH -> 3
        }

        // Current time values for Carrier time frame
        val calendar = Calendar.getInstance()
        val nowHours = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val nowPm = calendar.get(Calendar.AM_PM) == Calendar.PM
        val minute = calendar.get(Calendar.MINUTE)
        val nowTens = minute / 10
        val nowUnits = minute % 10

        // Carrier checksum formula: modulo 16
        val checksum = (
            (nowHours and 0x0F) +
            (if (nowPm) 8 else 0) +
            (nowTens and 0x07) +
            (if (state.power) 1 else 0) +
            (nowUnits and 0x0F) +
            (if (state.swing) 2 else 0) +
            (tempOffset and 0x0F) +
            (fanVal and 0x03) * 4 +
            (modeVal and 0x03)
        ) % 16

        // Checksum bits
        raw[35] = if ((checksum and 0x01) > 0) HIGH else LOW
        raw[37] = if ((checksum and 0x02) > 0) HIGH else LOW
        raw[39] = if ((checksum and 0x04) > 0) HIGH else LOW
        raw[41] = if ((checksum and 0x08) > 0) HIGH else LOW

        // Mode bits
        raw[43] = if ((modeVal and 0x01) > 0) HIGH else LOW
        raw[45] = if ((modeVal and 0x02) > 0) HIGH else LOW

        // Fan bits
        raw[47] = if ((fanVal and 0x01) > 0) HIGH else LOW
        raw[49] = if ((fanVal and 0x02) > 0) HIGH else LOW

        // Temperature bits
        raw[51] = if ((tempOffset and 0x01) > 0) HIGH else LOW
        raw[53] = if ((tempOffset and 0x02) > 0) HIGH else LOW
        raw[55] = if ((tempOffset and 0x04) > 0) HIGH else LOW
        raw[57] = if ((tempOffset and 0x08) > 0) HIGH else LOW

        // Swing bit
        raw[61] = if (state.swing) HIGH else LOW

        // Time units
        raw[67] = if ((nowUnits and 0x01) > 0) HIGH else LOW
        raw[69] = if ((nowUnits and 0x02) > 0) HIGH else LOW
        raw[71] = if ((nowUnits and 0x04) > 0) HIGH else LOW
        raw[73] = if ((nowUnits and 0x08) > 0) HIGH else LOW

        // Power bit
        raw[75] = if (state.power) HIGH else LOW

        // Time tens
        raw[83] = if ((nowTens and 0x01) > 0) HIGH else LOW
        raw[85] = if ((nowTens and 0x02) > 0) HIGH else LOW
        raw[87] = if ((nowTens and 0x04) > 0) HIGH else LOW

        // Time PM
        raw[89] = if (nowPm) HIGH else LOW

        // Time hours
        raw[91] = if ((nowHours and 0x01) > 0) HIGH else LOW
        raw[93] = if ((nowHours and 0x02) > 0) HIGH else LOW
        raw[95] = if ((nowHours and 0x04) > 0) HIGH else LOW
        raw[97] = if ((nowHours and 0x08) > 0) HIGH else LOW

        return raw
    }
}
