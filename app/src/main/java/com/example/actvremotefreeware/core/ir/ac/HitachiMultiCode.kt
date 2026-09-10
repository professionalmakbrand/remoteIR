package com.example.actvremotefreeware.core.ir.ac

import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed

/**
 * Multiple Hitachi AC Infrared Code Models across generations:
 * - Code 1: Hitachi 2018 Inverter (Captured - RAS-DX18HDK / RAK-35RPC) with 30ms wake-up leader
 * - Code 2: Hitachi 2018 Inverter (RAR-8P2 53-byte / 424-bit Dynamic) with 30ms wake-up leader
 * - Code 3: Hitachi 2018 Frame (28-Byte with 30ms Wakeup)
 * - Code 4: Hitachi Kaze / i-Clean / Kashikoi (13-Byte R-LT0541-HTA)
 * - Code 5: Hitachi RAR-2P2 / RAR-3V2 (33-Byte 264-bit)
 * - Code 6: Hitachi Split AC Series (RAS-10EH1 Broadlink)
 * - Code 7: Hitachi RAR-5H1 Series (40-Byte 320-bit)
 * - Code 8: Hitachi Indian Split AC (NEC 32-bit @ 38kHz)
 * - Code 9: Hitachi Inverter HSPE Series (Broadlink 1091)
 * - Code 10: Hitachi Coolix / Midea OEM (48-bit Frame)
 */
object HitachiMultiCode {

    const val CODE_COUNT = 10

    fun getCodeDescription(index: Int): String {
        return when (index) {
            0 -> "Code 1: Hitachi 2018 Inverter (Captured - RAS-DX18HDK)"
            1 -> "Code 2: Hitachi 2018 Inverter (RAR-8P2 424-bit Dynamic)"
            2 -> "Code 3: Hitachi 2018 Frame (28-Byte with 30ms Wakeup)"
            3 -> "Code 4: Hitachi Kaze / i-Clean (13-Byte R-LT0541)"
            4 -> "Code 5: Hitachi RAR-2P2 / RAR-3V2 (33-Byte 264-bit)"
            5 -> "Code 6: Hitachi Split AC Series (RAS-10EH1 Broadlink)"
            6 -> "Code 7: Hitachi RAR-5H1 Series (40-Byte 320-bit)"
            7 -> "Code 8: Hitachi Indian Split AC (NEC 32-bit @ 38kHz)"
            8 -> "Code 9: Hitachi Inverter HSPE (Broadlink 1091)"
            9 -> "Code 10: Hitachi Coolix / Midea OEM (48-bit Frame)"
            else -> "Code ${index + 1}: Hitachi Generic"
        }
    }

    fun encode(state: AcState, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex % CODE_COUNT) {
            0 -> HitachiBroadlink1084.encode(state)
            1 -> Pair(HitachiAc424Protocol.FREQUENCY, HitachiAc424Protocol.encode(state))
            2 -> Pair(HitachiAc28Protocol.FREQUENCY, HitachiAc28Protocol.encode(state))
            3 -> Pair(HitachiAc1Protocol.FREQUENCY, HitachiAc1Protocol.encode(state))
            4 -> Pair(HitachiAcProtocol.FREQUENCY, HitachiAcProtocol.encode(state))
            5 -> HitachiBroadlink1081.encode(state)
            6 -> Pair(38000, encodeHitachiRar5H1(state))
            7 -> Pair(38000, encodeHitachiNec(state))
            8 -> HitachiBroadlink1091.encode(state)
            9 -> Pair(LloydCoolixAcProtocol.FREQUENCY, LloydCoolixAcProtocol.encode(state))
            else -> HitachiBroadlink1084.encode(state)
        }
    }

    /**
     * Hitachi Code 8: Indian split AC NEC 38kHz modulation (9000µs leader, 4500µs space).
     */
    private fun encodeHitachiNec(state: AcState): IntArray {
        val address = 0x55
        val invAddress = 0xAA

        // Command based on state
        val command = if (!state.power) {
            0x00 // Power off
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
        val invCommand = command.inv() and 0xFF

        val bytes = intArrayOf(address, invAddress, command, invCommand)

        val pulses = mutableListOf<Int>()
        // NEC Leader
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

    /**
     * Hitachi Code 7: RAR-5H1 Series (40 bytes / 320 bits).
     */
    private fun encodeHitachiRar5H1(state: AcState): IntArray {
        val bytes = IntArray(40)
        bytes[0] = 0x01
        bytes[1] = 0x10
        bytes[2] = 0x00
        bytes[3] = 0x40
        bytes[4] = 0xBF
        bytes[5] = 0xFF
        bytes[6] = 0x00
        bytes[7] = 0xCC

        val modeVal = when (state.mode) {
            AcMode.COOL -> 0x41
            AcMode.HEAT -> 0x43
            AcMode.DRY -> 0x42
            AcMode.FAN -> 0x44
            AcMode.AUTO -> 0x45
        }
        bytes[8] = if (state.power) modeVal else 0xE0
        bytes[9] = (state.temp.coerceIn(16, 30) shl 2) or 0x01
        bytes[10] = if (state.swing) 0xF2 else 0x02

        var sum = 0
        for (i in 8..38) {
            sum += bytes[i]
        }
        bytes[39] = (256 - (sum and 0xFF)) and 0xFF

        return modulateHitachiPulses(bytes)
    }

    private fun modulateHitachiPulses(bytes: IntArray): IntArray {
        val pulses = mutableListOf<Int>()
        pulses.add(3300)
        pulses.add(1700)

        for (b in bytes) {
            for (bit in 0..7) {
                pulses.add(400)
                if (((b shr bit) and 1) == 1) {
                    pulses.add(1250)
                } else {
                    pulses.add(400)
                }
            }
        }

        pulses.add(400)
        pulses.add(40000)
        return pulses.toIntArray()
    }
}
