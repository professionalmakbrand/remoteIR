package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState

/**
 * Multi-code profiles for Carrier ACs:
 * - Code 1: Carrier Dynamic 148-Pulse Protocol
 * - Code 2: Carrier Coolix Inverter OEM (48-bit)
 * - Code 3: Carrier Standard Split AC
 * - Code 4: Carrier Old Window AC (NEC 32-bit)
 * - Code 5: Carrier Carlyle / Durakool Series
 */
object CarrierMultiCode {

    const val CODE_COUNT = 5

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: Carrier Dynamic (148-Pulse)"
            1 -> "Code 2: Carrier Coolix Inverter (48-bit)"
            2 -> "Code 3: Carrier Standard Split AC"
            3 -> "Code 4: Carrier Old Window AC (NEC 32-bit)"
            4 -> "Code 5: Carrier Durakool Series"
            else -> "Code ${index + 1}: Carrier Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> Pair(CarrierAcProtocol.FREQUENCY, CarrierAcProtocol.encode(state))
            1 -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            2 -> SequenceAcProtocols.encode(state, 1)
            3 -> Pair(38000, encodeCarrierNec(state))
            4 -> SequenceAcProtocols.encode(state, 3)
            else -> Pair(CarrierAcProtocol.FREQUENCY, CarrierAcProtocol.encode(state))
        }
    }

    private fun encodeCarrierNec(state: AcState): IntArray {
        val address = 0x4D
        val invAddress = 0xB2
        val command = if (!state.power) 0x00 else (0x20 or (state.temp - 16).coerceIn(0, 14))
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
