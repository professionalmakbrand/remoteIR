package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState

/**
 * Multi-code profiles for Voltas ACs covering classic window units, split ACs, and inverters:
 * - Code 1: Voltas Inverter & Split (Native 80-bit, 122LZF / 183V MZJ3 Series)
 * - Code 2: Voltas Gree OEM Split (64-bit Modulo-8)
 * - Code 3: Voltas All-Weather Inverter (Coolix 48-bit OEM)
 * - Code 4: Voltas Vertis / Classic Split (NEC 0x20DF)
 * - Code 5: Voltas Classic Window AC (NEC 32-bit @ 38kHz)
 * - Code 6: Voltas Standard Split (Sequence 3 / 148-Pulse)
 */
object VoltasMultiCode {

    const val CODE_COUNT = 6

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: Voltas Inverter & Split (Native 80-bit)"
            1 -> "Code 2: Voltas Gree OEM Split (64-bit)"
            2 -> "Code 3: Voltas All-Weather Inverter (Coolix 48-bit)"
            3 -> "Code 4: Voltas Vertis / Classic Split (0x20DF)"
            4 -> "Code 5: Voltas Classic Window AC (NEC 32-bit)"
            5 -> "Code 6: Voltas Standard Split (148-Pulse)"
            else -> "Code ${index + 1}: Voltas Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> Pair(VoltasAcProtocol.FREQUENCY, VoltasAcProtocol.encode(state))
            1 -> Pair(GreeAcProtocol.FREQUENCY, GreeAcProtocol.encode(state))
            2 -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            3 -> Pair(38000, encodeVoltasVertisNec(state))
            4 -> Pair(38000, encodeVoltasWindowNec(state))
            5 -> SequenceAcProtocols.encode(state, 3)
            else -> Pair(VoltasAcProtocol.FREQUENCY, VoltasAcProtocol.encode(state))
        }
    }

    private fun encodeVoltasWindowNec(state: AcState): IntArray {
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
        return modulateNec(address, invAddress, command, invCommand)
    }

    private fun encodeVoltasVertisNec(state: AcState): IntArray {
        val address = 0x20
        val invAddress = 0xDF
        val command = if (!state.power) {
            0x10 // Power toggle / off
        } else {
            val tempOffset = (state.temp - 16).coerceIn(0, 14)
            val modeNibble = when (state.mode) {
                AcMode.COOL -> 0x80
                AcMode.DRY -> 0x90
                AcMode.FAN -> 0xA0
                AcMode.HEAT -> 0xB0
                AcMode.AUTO -> 0xC0
            }
            modeNibble or tempOffset
        }
        val invCommand = (command.inv()) and 0xFF
        return modulateNec(address, invAddress, command, invCommand)
    }

    private fun modulateNec(addr: Int, invAddr: Int, cmd: Int, invCmd: Int): IntArray {
        val bytes = intArrayOf(addr, invAddr, cmd, invCmd)
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
