package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState

/**
 * Multi-code profiles for Lloyd ACs across window, split, and inverter units:
 * - Code 1: Lloyd Coolix / Midea Inverter (48-bit)
 * - Code 2: Lloyd Gree OEM (64-bit Modulo-8)
 * - Code 3: Lloyd Indian Split AC (NEC 32-bit @ 38kHz)
 * - Code 4: Lloyd Classic Window AC (148-Pulse)
 * - Code 5: Lloyd Kelon / Hisense OEM (48-bit)
 */
object LloydMultiCode {

    const val CODE_COUNT = 5

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: Lloyd Coolix Inverter (48-bit)"
            1 -> "Code 2: Lloyd Gree OEM Split (64-bit)"
            2 -> "Code 3: Lloyd Indian Split (NEC 32-bit)"
            3 -> "Code 4: Lloyd Classic Window (148-Pulse)"
            4 -> "Code 5: Lloyd Kelon OEM Series (48-bit)"
            else -> "Code ${index + 1}: Lloyd Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            1 -> Pair(GreeAcProtocol.FREQUENCY, GreeAcProtocol.encode(state))
            2 -> Pair(38000, encodeLloydNec(state))
            3 -> SequenceAcProtocols.encode(state, 1)
            4 -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            else -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
        }
    }

    private fun encodeLloydNec(state: AcState): IntArray {
        val address = 0x55
        val invAddress = 0xAA
        val command = if (!state.power) {
            0x00
        } else {
            val tempOffset = (state.temp - 16).coerceIn(0, 14)
            val modeOffset = when (state.mode) {
                AcMode.COOL -> 0x10
                AcMode.HEAT -> 0x20
                AcMode.DRY -> 0x30
                AcMode.FAN -> 0x40
                AcMode.AUTO -> 0x50
            }
            modeOffset or tempOffset
        }
        val invCommand = (command.inv()) and 0xFF

        val bytes = intArrayOf(address, invAddress, command, invCommand)
        val pulses = mutableListOf<Int>()
        pulses.add(9000)
        pulses.add(4500)

        for (b in bytes) {
            for (bit in 0..7) {
                pulses.add(560)
                if (((b shr bit) and 1) == 1) {
                    pulses.add(1690)
                } else {
                    pulses.add(560)
                }
            }
        }

        pulses.add(560)
        pulses.add(40000)
        return pulses.toIntArray()
    }
}
