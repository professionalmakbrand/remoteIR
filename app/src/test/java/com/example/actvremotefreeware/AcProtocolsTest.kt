package com.example.actvremotefreeware

import com.example.actvremotefreeware.core.ir.ac.AcProtocolRouter
import com.example.actvremotefreeware.core.ir.ac.CarrierAcProtocol
import com.example.actvremotefreeware.core.ir.ac.GreeAcProtocol
import com.example.actvremotefreeware.core.ir.ac.HitachiAcProtocol
import com.example.actvremotefreeware.core.ir.ac.LgAcProtocol
import com.example.actvremotefreeware.core.ir.ac.LloydCoolixAcProtocol
import com.example.actvremotefreeware.core.ir.ac.OGeneralFujitsuAcProtocol
import com.example.actvremotefreeware.core.ir.ac.SequenceAcProtocols
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed
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
        val count = com.example.actvremotefreeware.core.ir.ac.HitachiMultiCode.CODE_COUNT
        assertEquals(10, count)

        for (i in 0 until count) {
            val (freq, pattern) = com.example.actvremotefreeware.core.ir.ac.HitachiMultiCode.encode(state, i)
            assertEquals(38000, freq)
            assertTrue("Hitachi code $i pattern must not be empty", pattern.isNotEmpty())
            assertTrue("Hitachi code $i header must be > 1000µs", pattern[0] > 1000)
        }
    }

    @Test
    fun testBrandCodeRegistryEveryCodeIndex() {
        for (brand in AcBrand.entries) {
            val count = com.example.actvremotefreeware.core.ir.ac.BrandCodeRegistry.getCodeCount(brand)
            assertTrue("Brand $brand must have at least 1 code set", count >= 1)
            for (idx in 0 until count) {
                val state = AcState(brand = brand, temp = 24, power = true)
                val (freq, pattern) = com.example.actvremotefreeware.core.ir.ac.BrandCodeRegistry.encode(state, idx)
                assertTrue("Freq for $brand code $idx must be >= 30000", freq >= 30000)
                assertTrue("Pattern for $brand code $idx must not be empty", pattern.isNotEmpty())
            }
        }
    }
}
