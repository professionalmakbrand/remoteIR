package com.example.actvremotefreeware.core.ir.tv

/**
 * Low-level infrared modulation engines for Television protocols:
 * - NEC 32-bit (38kHz)
 * - Samsung 32-bit (38kHz)
 * - Sony SIRC 12/15-bit (40kHz)
 * - Philips RC5 (36kHz)
 */
object TvProtocols {

    /**
     * Standard NEC Protocol:
     * - Leader: 9000µs Mark, 4500µs Space
     * - Bit 0: 560µs Mark, 560µs Space
     * - Bit 1: 560µs Mark, 1690µs Space
     * - Trailer: 560µs Mark, 40000µs Space
     */
    fun encodeNec(address: Int, command: Int, isExtended: Boolean = false): Pair<Int, IntArray> {
        val bytes = if (isExtended) {
            // 16-bit address (low byte, high byte), 8-bit command, 8-bit inverted command
            intArrayOf(
                address and 0xFF,
                (address shr 8) and 0xFF,
                command and 0xFF,
                (command.inv()) and 0xFF
            )
        } else {
            // 8-bit address, 8-bit inverted address, 8-bit command, 8-bit inverted command
            intArrayOf(
                address and 0xFF,
                (address.inv()) and 0xFF,
                command and 0xFF,
                (command.inv()) and 0xFF
            )
        }

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

        return Pair(38000, pulses.toIntArray())
    }

    /**
     * Samsung TV Protocol:
     * - Leader: 4500µs Mark, 4500µs Space
     * - Bit 0: 560µs Mark, 560µs Space
     * - Bit 1: 560µs Mark, 1690µs Space
     * - Carrier: 38,000 Hz
     */
    fun encodeSamsung(address: Int, command: Int): Pair<Int, IntArray> {
        val bytes = intArrayOf(
            address and 0xFF,
            (address shr 8) and 0xFF,
            command and 0xFF,
            (command.inv()) and 0xFF
        )

        val pulses = mutableListOf<Int>()
        pulses.add(4500)
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

        return Pair(38000, pulses.toIntArray())
    }

    /**
     * Sony SIRC Protocol (12-bit / 15-bit):
     * - Leader: 2400µs Mark, 600µs Space
     * - Bit 0: 600µs Mark, 600µs Space
     * - Bit 1: 1200µs Mark, 600µs Space
     * - Carrier: 40,000 Hz
     * - Sends 7-bit command then 5-bit (or 8-bit) address, LSB first
     */
    fun encodeSony(address: Int, command: Int, bits: Int = 12): Pair<Int, IntArray> {
        val addressBits = bits - 7
        val rawData = (command and 0x7F) or ((address and ((1 shl addressBits) - 1)) shl 7)

        val pulses = mutableListOf<Int>()
        pulses.add(2400) // Leader Mark
        pulses.add(600)  // Leader Space

        for (bit in 0 until bits) {
            if (((rawData shr bit) and 1) == 1) {
                pulses.add(1200) // Mark
            } else {
                pulses.add(600)  // Mark
            }
            if (bit == bits - 1) {
                pulses.add(25000) // Final Tail Space
            } else {
                pulses.add(600)   // Intermediate Space
            }
        }

        return Pair(40000, pulses.toIntArray())
    }

    /**
     * Philips RC5 Protocol (36kHz biphase):
     * - Half-bit duration: 889µs
     */
    fun encodeRc5(address: Int, command: Int): Pair<Int, IntArray> {
        val frame = (1 shl 13) or (1 shl 12) or ((address and 0x1F) shl 6) or (command and 0x3F)
        val pulses = mutableListOf<Int>()
        val halfBit = 889

        for (bit in 13 downTo 0) {
            val bitVal = (frame shr bit) and 1
            if (bitVal == 1) {
                pulses.add(halfBit)
                pulses.add(halfBit)
            } else {
                pulses.add(halfBit)
                pulses.add(halfBit)
            }
        }
        pulses.add(halfBit)
        pulses.add(30000)
        return Pair(36000, pulses.toIntArray())
    }
}
