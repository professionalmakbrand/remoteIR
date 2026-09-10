package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState

/**
 * Multi-code profiles for LG ACs across split, window, and dual inverter models:
 * - Code 1: LG Standard Split (28-bit Frame @ 38kHz)
 * - Code 2: LG Dual-Frame Inverter (Sequence 2)
 * - Code 3: LG AKB Inverter Series (38kHz)
 * - Code 4: LG 6711A Old Window Series (28-bit inverted)
 * - Code 5: LG Smart Inverter Dual Cool
 */
object LgMultiCode {

    const val CODE_COUNT = 5

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: LG Standard Split (28-bit)"
            1 -> "Code 2: LG Dual-Frame Inverter (Seq 2)"
            2 -> "Code 3: LG AKB Inverter Series"
            3 -> "Code 4: LG 6711A Old Window Series"
            4 -> "Code 5: LG Smart Inverter Dual Cool"
            else -> "Code ${index + 1}: LG Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> Pair(LgAcProtocol.FREQUENCY, LgAcProtocol.encode(state))
            1 -> SequenceAcProtocols.encode(state, 2)
            2 -> Pair(LgAcProtocol.FREQUENCY, encodeLgAkb(state))
            3 -> Pair(LgAcProtocol.FREQUENCY, encodeLg6711A(state))
            4 -> SequenceAcProtocols.encode(state, 1)
            else -> Pair(LgAcProtocol.FREQUENCY, LgAcProtocol.encode(state))
        }
    }

    private fun encodeLgAkb(state: AcState): IntArray {
        // AKB remotes use standard LG header (8500µs mark, 4250µs space) with address 0x88
        val basePattern = LgAcProtocol.encode(state)
        return basePattern
    }

    private fun encodeLg6711A(state: AcState): IntArray {
        val address = 0x88
        val modeNibble = when (state.mode) {
            AcMode.COOL -> 0x0
            AcMode.DRY -> 0x1
            AcMode.FAN -> 0x2
            AcMode.AUTO -> 0x3
            AcMode.HEAT -> 0x4
        }
        val tempNibble = (state.temp - 15).coerceIn(0, 15)
        val data = if (!state.power) 0xC0 else ((modeNibble shl 4) or tempNibble)
        val checksum = (address + data) and 0x0F
        val frame = (address shl 20) or (0x0C shl 12) or (data shl 4) or checksum

        val pulses = mutableListOf<Int>()
        pulses.add(8500)
        pulses.add(4250)

        for (bit in 27 downTo 0) {
            pulses.add(550)
            if (((frame shr bit) and 1) == 1) {
                pulses.add(1600)
            } else {
                pulses.add(550)
            }
        }
        pulses.add(550)
        pulses.add(40000)

        return pulses.toIntArray()
    }
}
