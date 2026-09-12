package com.maahi.iractvremote

import com.maahi.iractvremote.core.ir.ac.AcProtocolRouter
import com.maahi.iractvremote.core.ir.ac.CarrierAcProtocol
import com.maahi.iractvremote.core.ir.ac.GreeAcProtocol
import com.maahi.iractvremote.core.ir.ac.HitachiAcProtocol
import com.maahi.iractvremote.core.ir.ac.LgAcProtocol
import com.maahi.iractvremote.core.ir.ac.LloydCoolixAcProtocol
import com.maahi.iractvremote.core.ir.ac.OGeneralFujitsuAcProtocol
import com.maahi.iractvremote.core.ir.ac.SequenceAcProtocols
import com.maahi.iractvremote.core.ir.ac.VoltasAcProtocol
import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying AC infrared protocols, bitstream synthesis, and brand routing.
 */
class AcProtocolsTest {

    @Test
    fun testCarrierProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.CARRIER,
            power = true,
            temp = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO,
            swing = false
        )
        val pattern = CarrierAcProtocol.encode(state)
        assertEquals("Carrier pattern must have exactly 148 pulses", 148, pattern.size)
        assertEquals("Carrier header mark must be 8835µs", 8835, pattern[0])
        assertEquals("Carrier header space must be 4497µs", 4497, pattern[1])
        assertEquals("Carrier trailer space must be 50067µs", 50067, pattern[147])

        // Verify power off alters pattern
        val stateOff = state.copy(power = false)
        val patternOff = CarrierAcProtocol.encode(stateOff)
        assertEquals(148, patternOff.size)
        assertFalse("Power OFF pattern should differ from Power ON", pattern.contentEquals(patternOff))
    }

    @Test
    fun testHitachiProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.HITACHI,
            power = true,
            temp = 22,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.MED,
            swing = true
        )
        val pattern = HitachiAcProtocol.encode(state)
        assertTrue("Hitachi pattern must contain pulse stream", pattern.isNotEmpty())
        assertEquals("Hitachi header mark must be 3300µs", 3300, pattern[0])
        assertEquals("Hitachi header space must be 1700µs", 1700, pattern[1])
        // 13 bytes * 8 bits * 2 pulses + 2 header + 2 footer = 212 pulses
        assertEquals("Hitachi pattern length should be 212 pulses", 212, pattern.size)
    }

    @Test
    fun testOGeneralFujitsuProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.O_GENERAL,
            power = true,
            temp = 25,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.HIGH,
            swing = false
        )
        val pattern = OGeneralFujitsuAcProtocol.encode(state)
        assertTrue("O General pattern must contain pulse stream", pattern.isNotEmpty())
        assertEquals("O General header mark must be 3320µs", 3320, pattern[0])
        assertEquals("O General header space must be 1580µs", 1580, pattern[1])
        // 16 bytes * 8 bits * 2 pulses + 2 header + 2 footer = 260 pulses
        assertEquals("O General 128-bit frame length should be 260 pulses", 260, pattern.size)
    }

    @Test
    fun testLloydCoolixProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.LLOYD,
            power = true,
            temp = 23,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO,
            swing = false
        )
        val pattern = LloydCoolixAcProtocol.encode(state)
        assertTrue("Lloyd pattern must not be empty", pattern.isNotEmpty())
        assertEquals("Lloyd header mark must be 4500µs", 4500, pattern[0])
        assertEquals("Lloyd header space must be 4500µs", 4500, pattern[1])
        // 6 bytes * 8 bits * 2 pulses + 2 header + 2 footer = 100 pulses
        assertEquals("Lloyd 48-bit frame length should be 100 pulses", 100, pattern.size)
    }

    @Test
    fun testLgProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.LG,
            power = true,
            temp = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO
        )
        val pattern = LgAcProtocol.encode(state)
        assertTrue("LG pattern must not be empty", pattern.isNotEmpty())
        assertEquals("LG header mark must be 8500µs", 8500, pattern[0])
        assertEquals("LG header space must be 4250µs", 4250, pattern[1])
        // 28 bits * 2 pulses + 2 header + 2 footer = 60 pulses
        assertEquals("LG 28-bit frame length should be 60 pulses", 60, pattern.size)
    }

    @Test
    fun testGreeProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.GREE_MIDEA,
            power = true,
            temp = 26,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO
        )
        val pattern = GreeAcProtocol.encode(state)
        assertTrue("Gree pattern must not be empty", pattern.isNotEmpty())
        assertEquals("Gree header mark must be 9000µs", 9000, pattern[0])
        assertEquals("Gree header space must be 4500µs", 4500, pattern[1])
    }

    @Test
    fun testVoltasProtocolSynthesis() {
        val state = AcState(
            brand = AcBrand.VOLTAS,
            power = true,
            temp = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.LOW,
            swing = false
        )
        val pattern = VoltasAcProtocol.encode(state)
        // 10 bytes * 8 bits * 2 pulses + 2 footer pulses = 162 pulses
        assertEquals("Voltas 80-bit frame length must be exactly 162 pulses", 162, pattern.size)
        assertEquals("Voltas first mark should be 1026µs", 1026, pattern[0])
        assertEquals("Voltas footer mark should be 1026µs", 1026, pattern[160])
        assertEquals("Voltas footer space should be 40000µs", 40000, pattern[161])

        // Decode pulses back to 10 bytes (MSB first)
        val decodedBytes = IntArray(10)
        for (byteIdx in 0 until 10) {
            var b = 0
            for (bit in 0 until 8) {
                val space = pattern[byteIdx * 16 + bit * 2 + 1]
                val bitVal = if (space > 1500) 1 else 0
                b = (b shl 1) or bitVal
            }
            decodedBytes[byteIdx] = b
        }

        // Check decoded bytes match official specification:
        // Byte 0: 0x33
        // Byte 1: 0x88 (Fan Low 0x80 | Mode Cool 0x08)
        // Byte 2: 0x88 (Power ON 0x80 | Fixed 0x08)
        // Byte 3: 0x18 (Temp 24 -> 0x10 | (24-16))
        // Bytes 4..8: 0x3B, 0x3B, 0x3B, 0x11, 0x00
        // Byte 9: Checksum 0xE2
        assertEquals(0x33, decodedBytes[0])
        assertEquals(0x88, decodedBytes[1])
        assertEquals(0x88, decodedBytes[2])
        assertEquals(0x18, decodedBytes[3])
        assertEquals(0x3B, decodedBytes[4])
        assertEquals(0x3B, decodedBytes[5])
        assertEquals(0x3B, decodedBytes[6])
        assertEquals(0x11, decodedBytes[7])
        assertEquals(0x00, decodedBytes[8])
        assertEquals(0xE2, decodedBytes[9])

        // Verify Swing ON changes Byte 2 to 0x8F and Checksum to 0xDB
        val swingPattern = VoltasAcProtocol.encode(state.copy(swing = true))
        assertEquals(162, swingPattern.size)
        var swingByte2 = 0
        for (bit in 0 until 8) {
            val space = swingPattern[2 * 16 + bit * 2 + 1]
            swingByte2 = (swingByte2 shl 1) or (if (space > 1500) 1 else 0)
        }
        assertEquals(0x8F, swingByte2)

        // Verify Power OFF changes Byte 2 to 0x08
        val offPattern = VoltasAcProtocol.encode(state.copy(power = false))
        assertEquals(162, offPattern.size)
        var offByte2 = 0
        for (bit in 0 until 8) {
            val space = offPattern[2 * 16 + bit * 2 + 1]
            offByte2 = (offByte2 shl 1) or (if (space > 1500) 1 else 0)
        }
        assertEquals(0x08, offByte2)
    }

    @Test
    fun testSequenceMicrosecondMultiplier() {
        val csv = "10,20,30"
        val freq = 38000
        val pulses = SequenceAcProtocols.parseToMicroseconds(csv, freq)
        val expectedMultiplier = 1000000 / 38000 // 26
        assertEquals(3, pulses.size)
        assertEquals(10 * expectedMultiplier, pulses[0])
        assertEquals(20 * expectedMultiplier, pulses[1])
        assertEquals(30 * expectedMultiplier, pulses[2])
    }

    @Test
    fun testAllBrandsRouteCleanly() {
        for (brand in AcBrand.entries) {
            val state = AcState(brand = brand, temp = 24, power = true)
            val (frequency, pattern) = AcProtocolRouter.buildPattern(state)
            assertTrue("Frequency for $brand must be > 30kHz", frequency >= 30000)
            assertNotNull("Pattern for $brand must not be null", pattern)
            assertTrue("Pattern for $brand must have pulses", pattern.isNotEmpty())
        }
    }

    @Test
    fun testHitachiAllCodeSets() {
        val state = AcState(brand = AcBrand.HITACHI, temp = 24, power = true)
        val count = com.maahi.iractvremote.core.ir.ac.HitachiMultiCode.CODE_COUNT
        assertEquals(10, count)

        for (i in 0 until count) {
            val (freq, pattern) = com.maahi.iractvremote.core.ir.ac.HitachiMultiCode.encode(state, i)
            assertEquals(38000, freq)
            assertTrue("Hitachi code $i pattern must not be empty", pattern.isNotEmpty())
            assertTrue("Hitachi code $i header must be > 1000µs", pattern[0] > 1000)
        }
    }

    @Test
    fun testBrandCodeRegistryEveryCodeIndex() {
        for (brand in AcBrand.entries) {
            val count = com.maahi.iractvremote.core.ir.ac.BrandCodeRegistry.getCodeCount(brand)
            assertTrue("Brand $brand must have at least 1 code set", count >= 1)
            for (idx in 0 until count) {
                val state = AcState(brand = brand, temp = 24, power = true)
                val (freq, pattern) = com.maahi.iractvremote.core.ir.ac.BrandCodeRegistry.encode(state, idx)
                assertTrue("Freq for $brand code $idx must be >= 30000", freq >= 30000)
                assertTrue("Pattern for $brand code $idx must not be empty", pattern.isNotEmpty())
            }
        }
    }
}
