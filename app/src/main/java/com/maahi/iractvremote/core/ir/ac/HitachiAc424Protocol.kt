package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Hitachi AC424 Protocol Engine (53 bytes / 424 bits).
 * Directly ported and adapted from IRremoteESP8266 (sendHitachiAc424).
 * Used by 2017-2020 Hitachi models including RAR-8P2 remote and RAS-AJ25H / RAS-DX18HDK A/C.
 *
 * CRITICAL REQUIREMENT:
 * This protocol utilizes an ultra-long wake-up preamble:
 * - Leader Mark:  29,784 µs (~30ms)
 * - Leader Space: 49,290 µs (~50ms)
 * Without this leader, the optical sensor in the 2018 AC remains in sleep mode.
 */
object HitachiAc424Protocol {

    const val FREQUENCY = 38000

    private const val LEADER_MARK = 29784
    private const val LEADER_SPACE = 49290
    private const val HDR_MARK = 3416
    private const val HDR_SPACE = 1604
    private const val BIT_MARK = 463
    private const val ONE_SPACE = 1208
    private const val ZERO_SPACE = 372
    private const val FOOTER_MARK = 463
    private const val MIN_GAP = 40000

    const val STATE_LENGTH = 53

    // Native Modes
    private const val MODE_FAN = 1
    private const val MODE_COOL = 3
    private const val MODE_DRY = 5
    private const val MODE_HEAT = 6

    // Native Fans
    private const val FAN_MIN = 1
    private const val FAN_LOW = 2
    private const val FAN_MED = 3
    private const val FAN_HIGH = 4
    private const val FAN_AUTO = 5
    private const val FAN_MAX = 6

    // Buttons
    private const val BUTTON_POWER_MODE = 0x13
    private const val BUTTON_FAN = 0x42
    private const val BUTTON_TEMP_DOWN = 0x43
    private const val BUTTON_TEMP_UP = 0x44
    private const val BUTTON_SWING_V = 0x81

    fun encode(state: AcState): IntArray {
        val raw = IntArray(STATE_LENGTH)

        // Baseline initialization (from IRHitachiAc424::stateReset)
        raw[0] = 0x01
        raw[1] = 0x10
        raw[3] = 0x40
        raw[5] = 0xFF
        raw[7] = 0xCC
        raw[9] = 0x92
        raw[11] = BUTTON_POWER_MODE
        raw[27] = 0xE1
        raw[33] = 0x80
        raw[35] = 0x03
        raw[37] = 0x01
        raw[39] = 0x88
        raw[45] = 0xFF
        raw[47] = 0xFF
        raw[49] = 0xFF
        raw[51] = 0xFF

        // Temperature (Byte 13, bits 2..7)
        val temp = state.temp.coerceIn(16, 32)
        raw[13] = (temp shl 2) and 0xFC

        // Mode & Fan (Byte 25: bits 0..3 mode, bits 4..7 fan)
        val modeVal = when (state.mode) {
            AcMode.COOL -> MODE_COOL
            AcMode.HEAT -> MODE_HEAT
            AcMode.DRY -> MODE_DRY
            AcMode.FAN -> MODE_FAN
            AcMode.AUTO -> MODE_COOL
        }

        val fanVal = when (state.fanSpeed) {
            FanSpeed.LOW -> FAN_LOW
            FanSpeed.MED -> FAN_MED
            FanSpeed.HIGH -> FAN_HIGH
            FanSpeed.AUTO -> FAN_AUTO
        }
        raw[25] = (modeVal and 0x0F) or ((fanVal and 0x0F) shl 4)

        // Fan-specific tweaks on Byte 9 and 29
        if (fanVal == FAN_MIN || fanVal == FAN_LOW) {
            raw[9] = 0x98
        } else if (fanVal == FAN_HIGH || fanVal == FAN_MAX) {
            raw[9] = 0xA9
            raw[29] = 0x30
        }

        // Power (Byte 27, bit 4)
        if (state.power) {
            raw[27] = raw[27] or 0x10
        } else {
            raw[27] = raw[27] and 0xEF
        }

        // Swing (Byte 37, bit 5)
        if (state.swing) {
            raw[11] = BUTTON_SWING_V
            raw[37] = raw[37] or (1 shl 5)
        }

        // Invert Byte Pairs (from index 3 to 52)
        // raw[i + 1] = ~raw[i]
        for (i in 3 until STATE_LENGTH - 1 step 2) {
            raw[i + 1] = raw[i].inv() and 0xFF
        }

        // Modulate into microsecond pulses (LSBF order per byte)
        val pulses = mutableListOf<Int>()

        // 1. Leader preamble
        pulses.add(LEADER_MARK)
        pulses.add(LEADER_SPACE)

        // 2. Header
        pulses.add(HDR_MARK)
        pulses.add(HDR_SPACE)

        // 3. 53 Bytes Data (LSBF)
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

        // 4. Trailer / Footer
        pulses.add(FOOTER_MARK)
        pulses.add(MIN_GAP)

        return pulses.toIntArray()
    }
}
