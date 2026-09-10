package com.maahi.iractvremote

import com.maahi.iractvremote.core.ir.ac.BrandCodeRegistry
import com.maahi.iractvremote.core.ir.ac.CarrierMultiCode
import com.maahi.iractvremote.core.ir.ac.LgMultiCode
import com.maahi.iractvremote.core.ir.ac.LloydMultiCode
import com.maahi.iractvremote.core.ir.ac.OGeneralMultiCode
import com.maahi.iractvremote.core.ir.ac.VoltasMultiCode
import com.maahi.iractvremote.core.ir.tv.TvBrandCodeRegistry
import com.maahi.iractvremote.core.ir.tv.TvProtocols
import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.TvBrand
import com.maahi.iractvremote.model.TvCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TvProtocolsTest {

    @Test
    fun testTvNecProtocol() {
        val (freq, pulses) = TvProtocols.encodeNec(0x04, 0x08)
        assertEquals(38000, freq)
        assertEquals(9000, pulses[0])
        assertEquals(4500, pulses[1])
        assertEquals(0, pulses.size % 2)
        assertTrue(pulses.size >= 66)
    }

    @Test
    fun testTvSamsungProtocol() {
        val (freq, pulses) = TvProtocols.encodeSamsung(0x0707, 0x02)
        assertEquals(38000, freq)
        assertEquals(4500, pulses[0])
        assertEquals(4500, pulses[1])
        assertEquals(0, pulses.size % 2)
    }

    @Test
    fun testTvSonyProtocol() {
        val (freq, pulses) = TvProtocols.encodeSony(1, 0x15, 12)
        assertEquals(40000, freq)
        assertEquals(2400, pulses[0])
        assertEquals(600, pulses[1])
        assertEquals(0, pulses.size % 2)
    }

    @Test
    fun testTvBrandCodeRegistry_AllBrandsAndCommands() {
        for (brand in TvBrand.entries) {
            val codeCount = TvBrandCodeRegistry.getCodeCount(brand)
            assertTrue("Brand $brand must have at least 2 code sets", codeCount >= 2)

            for (codeIndex in 0 until codeCount) {
                val label = TvBrandCodeRegistry.getCodeLabel(brand, codeIndex)
                assertNotNull(label)
                assertTrue(label.isNotBlank())

                for (cmd in listOf(TvCommand.POWER, TvCommand.MUTE, TvCommand.VOL_UP, TvCommand.OK, TvCommand.DIGIT_1)) {
                    val (freq, pulses) = TvBrandCodeRegistry.encode(brand, cmd, codeIndex)
                    assertTrue("Frequency for $brand must be >= 36000", freq >= 36000)
                    assertTrue("Pulses must not be empty for $brand $cmd", pulses.isNotEmpty())
                    assertEquals("Pulses must be even for $brand $cmd", 0, pulses.size % 2)
                    for (p in pulses) {
                        assertTrue("All pulses must be positive", p > 0)
                    }
                }
            }
        }
    }

    @Test
    fun testAcMultiCode_VoltasLloydLgOGeneralCarrier() {
        val state = AcState(power = true, temp = 24)

        // Voltas
        assertEquals(5, VoltasMultiCode.CODE_COUNT)
        for (i in 0 until 5) {
            val (freq, pattern) = VoltasMultiCode.encode(state.copy(brand = AcBrand.VOLTAS), i)
            assertTrue(freq >= 36000)
            assertTrue(pattern.isNotEmpty())
        }

        // Lloyd
        assertEquals(5, LloydMultiCode.CODE_COUNT)
        for (i in 0 until 5) {
            val (freq, pattern) = LloydMultiCode.encode(state.copy(brand = AcBrand.LLOYD), i)
            assertTrue(freq >= 36000)
            assertTrue(pattern.isNotEmpty())
        }

        // LG
        assertEquals(5, LgMultiCode.CODE_COUNT)
        for (i in 0 until 5) {
            val (freq, pattern) = LgMultiCode.encode(state.copy(brand = AcBrand.LG), i)
            assertTrue(freq >= 36000)
            assertTrue(pattern.isNotEmpty())
        }

        // O General
        assertEquals(4, OGeneralMultiCode.CODE_COUNT)
        for (i in 0 until 4) {
            val (freq, pattern) = OGeneralMultiCode.encode(state.copy(brand = AcBrand.O_GENERAL), i)
            assertTrue(freq >= 36000)
            assertTrue(pattern.isNotEmpty())
        }

        // Carrier
        assertEquals(5, CarrierMultiCode.CODE_COUNT)
        for (i in 0 until 5) {
            val (freq, pattern) = CarrierMultiCode.encode(state.copy(brand = AcBrand.CARRIER), i)
            assertTrue(freq >= 36000)
            assertTrue(pattern.isNotEmpty())
        }
    }
}
