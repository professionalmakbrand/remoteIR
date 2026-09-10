package com.example.actvremotefreeware.core.ir.ac

/**
 * Universal decoder for Broadlink IR base64 packets into microsecond pulse arrays
 * compatible with Android ConsumerIrManager.
 *
 * Broadlink IR Packet Structure:
 * - Byte 0: 0x26 (IR transmission type)
 * - Byte 1: Repeat count
 * - Byte 2..3: Payload length (little endian)
 * - Byte 4..N: Pulse tick data
 *   - If byte != 0: Single-byte tick duration
 *   - If byte == 0: 2-byte big-endian tick duration: (next_byte << 8) | next_next_byte
 * - Tick conversion: 1 tick = 8192 / 269 microseconds (~30.4535 µs)
 */
object BroadlinkPacketDecoder {

    const val CARRIER_FREQUENCY_HZ = 38000

    fun decode(base64String: String): IntArray {
        val cleanString = base64String.trim().replace("\n", "").replace("\r", "").replace(" ", "")
        val bytes = decodeBase64(cleanString)
        if (bytes.size < 6) {
            return IntArray(0)
        }

        val pulses = mutableListOf<Int>()
        var i = 4

        // Some Broadlink payloads end with 0x0D 0x05 0x00 0x00 or trailing zeroes
        while (i < bytes.size) {
            val rawByte = bytes[i].toInt() and 0xFF

            if (rawByte == 0) {
                // If there aren't at least 2 bytes remaining, break
                if (i + 2 >= bytes.size) break
                val hi = bytes[i + 1].toInt() and 0xFF
                val lo = bytes[i + 2].toInt() and 0xFF
                val ticks = (hi shl 8) or lo
                i += 3

                if (ticks > 0) {
                    val us = Math.round(ticks * 8192.0 / 269.0).toInt()
                    pulses.add(us)
                }
            } else {
                val ticks = rawByte
                i += 1

                val us = Math.round(ticks * 8192.0 / 269.0).toInt()
                pulses.add(us)
            }
        }

        // Drop trailing zeroes or stray terminator if present
        while (pulses.isNotEmpty() && pulses.last() == 0) {
            pulses.removeAt(pulses.size - 1)
        }

        // ConsumerIrManager requires an even number of pulses (alternating mark and space)
        if (pulses.size % 2 != 0) {
            pulses.add(40000) // Standard 40ms tail gap
        }

        return pulses.toIntArray()
    }

    private fun decodeBase64(input: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(input)
        } catch (_: Throwable) {
            android.util.Base64.decode(input, android.util.Base64.DEFAULT)
        }
    }
}
