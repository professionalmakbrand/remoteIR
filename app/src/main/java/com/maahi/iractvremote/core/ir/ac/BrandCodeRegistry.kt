package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.AcState

/**
 * Registry of multi-code candidate profiles for each AC Brand.
 * Powers the interactive OnePlus-style "Testing Code 1 of N" wizard.
 */
object BrandCodeRegistry {

    fun getCodeCount(brand: AcBrand): Int {
        return when (brand) {
            AcBrand.HITACHI -> HitachiMultiCode.CODE_COUNT // 10
            AcBrand.VOLTAS -> VoltasMultiCode.CODE_COUNT // 5
            AcBrand.LLOYD -> LloydMultiCode.CODE_COUNT // 5
            AcBrand.LG -> LgMultiCode.CODE_COUNT // 5
            AcBrand.O_GENERAL -> OGeneralMultiCode.CODE_COUNT // 4
            AcBrand.CARRIER -> CarrierMultiCode.CODE_COUNT // 5
            AcBrand.SAMSUNG -> 3
            AcBrand.DAIKIN -> 3
            AcBrand.PANASONIC -> 3
            AcBrand.WHIRLPOOL -> 3
            AcBrand.GREE_MIDEA -> 2
        }
    }

    fun getCodeLabel(brand: AcBrand, index: Int): String {
        return when (brand) {
            AcBrand.HITACHI -> HitachiMultiCode.getCodeDescription(index)
            AcBrand.VOLTAS -> VoltasMultiCode.getCodeDescription(index)
            AcBrand.LLOYD -> LloydMultiCode.getCodeDescription(index)
            AcBrand.LG -> LgMultiCode.getCodeDescription(index)
            AcBrand.O_GENERAL -> OGeneralMultiCode.getCodeDescription(index)
            AcBrand.CARRIER -> CarrierMultiCode.getCodeDescription(index)
            AcBrand.SAMSUNG -> when (index) {
                0 -> "Code 1: Samsung Dual Frame (28-byte)"
                1 -> "Code 2: Samsung Inverter (14-byte)"
                else -> "Code 3: Samsung Old Window/Split (NEC)"
            }
            AcBrand.DAIKIN -> when (index) {
                0 -> "Code 1: Daikin Dual-Frame (Seq 2)"
                1 -> "Code 2: Daikin Inverter (ARC452)"
                else -> "Code 3: Daikin Old Split AC"
            }
            AcBrand.PANASONIC -> when (index) {
                0 -> "Code 1: Panasonic Standard (Seq 3)"
                1 -> "Code 2: Panasonic Econavi Inverter"
                else -> "Code 3: Panasonic Old Window AC (NEC)"
            }
            AcBrand.WHIRLPOOL -> when (index) {
                0 -> "Code 1: Whirlpool Standard (Seq 1)"
                1 -> "Code 2: Whirlpool 6th Sense Inverter"
                else -> "Code 3: Whirlpool Old Split (NEC)"
            }
            AcBrand.GREE_MIDEA -> when (index) {
                0 -> "Code 1: Gree 64-bit Modulo-8"
                else -> "Code 2: Midea Coolix 48-bit"
            }
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        val count = getCodeCount(state.brand)
        val normalizedIndex = codeIndex.coerceIn(0, count - 1)

        return when (state.brand) {
            AcBrand.HITACHI -> HitachiMultiCode.encode(state, normalizedIndex)
            AcBrand.VOLTAS -> VoltasMultiCode.encode(state, normalizedIndex)
            AcBrand.LLOYD -> LloydMultiCode.encode(state, normalizedIndex)
            AcBrand.LG -> LgMultiCode.encode(state, normalizedIndex)
            AcBrand.O_GENERAL -> OGeneralMultiCode.encode(state, normalizedIndex)
            AcBrand.CARRIER -> CarrierMultiCode.encode(state, normalizedIndex)
            AcBrand.SAMSUNG -> when (normalizedIndex) {
                0 -> SequenceAcProtocols.encode(state, 1)
                1 -> SequenceAcProtocols.encode(state, 2)
                else -> Pair(38000, encodeGenericNec(state, 0x02))
            }
            AcBrand.DAIKIN -> when (normalizedIndex) {
                0 -> SequenceAcProtocols.encode(state, 2)
                1 -> SequenceAcProtocols.encode(state, 1)
                else -> SequenceAcProtocols.encode(state, 3)
            }
            AcBrand.PANASONIC -> when (normalizedIndex) {
                0 -> SequenceAcProtocols.encode(state, 3)
                1 -> SequenceAcProtocols.encode(state, 1)
                else -> Pair(38000, encodeGenericNec(state, 0x01))
            }
            AcBrand.WHIRLPOOL -> when (normalizedIndex) {
                0 -> SequenceAcProtocols.encode(state, 1)
                1 -> SequenceAcProtocols.encode(state, 3)
                else -> Pair(38000, encodeGenericNec(state, 0x05))
            }
            AcBrand.GREE_MIDEA -> when (normalizedIndex) {
                0 -> Pair(GreeAcProtocol.FREQUENCY, GreeAcProtocol.encode(state))
                else -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            }
        }
    }

    private fun encodeGenericNec(state: AcState, addr: Int): IntArray {
        val invAddr = (addr.inv()) and 0xFF
        val cmd = if (!state.power) 0x00 else (0x20 or (state.temp - 16).coerceIn(0, 14))
        val invCmd = (cmd.inv()) and 0xFF

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
