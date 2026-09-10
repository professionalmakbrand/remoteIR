package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState

/**
 * Multi-code profiles for O General (Fujitsu General) ACs:
 * - Code 1: O General 128-bit Full Inverter Frame (38kHz)
 * - Code 2: O General Short Frame (56-bit / 7-Byte)
 * - Code 3: O General Old Split AC (NEC 32-bit)
 * - Code 4: O General AR-RAH1U / AR-RY1 Series
 */
object OGeneralMultiCode {

    const val CODE_COUNT = 4

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: O General Inverter (128-bit)"
            1 -> "Code 2: O General Short Frame (56-bit)"
            2 -> "Code 3: O General Old Split (NEC 32-bit)"
            3 -> "Code 4: O General AR-RY1 Series"
            else -> "Code ${index + 1}: O General Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> Pair(OGeneralFujitsuAcProtocol.FREQUENCY, OGeneralFujitsuAcProtocol.encode(state))
            1 -> Pair(OGeneralFujitsuAcProtocol.FREQUENCY, encodeShortFrame(state))
            2 -> Pair(38000, encodeOGeneralNec(state))
            3 -> SequenceAcProtocols.encode(state, 1)
            else -> Pair(OGeneralFujitsuAcProtocol.FREQUENCY, OGeneralFujitsuAcProtocol.encode(state))
        }
    }

    private fun encodeShortFrame(state: AcState): IntArray {
        // Fujitsu short frame: 7 bytes (56 bits)
        val bytes = IntArray(7)
        bytes[0] = 0x14
        bytes[1] = 0x63
        bytes[2] = 0x00
        bytes[3] = 0x10
        bytes[4] = 0x10
        bytes[5] = if (state.power) 0x02 else 0x03 // Power on vs off toggle
        var sum = 0
        for (i in 0..5) sum += bytes[i]
        bytes[6] = (256 - (sum and 0xFF)) and 0xFF

        val pulses = mutableListOf<Int>()
        pulses.add(3300)
        pulses.add(1600)
        for (b in bytes) {
            for (bit in 0..7) {
                pulses.add(430)
                if (((b shr bit) and 1) == 1) {
                    pulses.add(1250)
                } else {
                    pulses.add(430)
                }
            }
        }
        pulses.add(430)
        pulses.add(40000)
        return pulses.toIntArray()
    }

    private fun encodeOGeneralNec(state: AcState): IntArray {
        val address = 0x28
        val invAddress = 0xD7
        val command = if (!state.power) 0x00 else (0x80 or (state.temp - 16).coerceIn(0, 14))
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
