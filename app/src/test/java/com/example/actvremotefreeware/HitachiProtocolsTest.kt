package com.example.actvremotefreeware

import com.example.actvremotefreeware.core.ir.ac.BrandCodeRegistry
import com.example.actvremotefreeware.core.ir.ac.BroadlinkPacketDecoder
import com.example.actvremotefreeware.core.ir.ac.HitachiAc1Protocol
import com.example.actvremotefreeware.core.ir.ac.HitachiAc28Protocol
import com.example.actvremotefreeware.core.ir.ac.HitachiAc424Protocol
import com.example.actvremotefreeware.core.ir.ac.HitachiBroadlink1081
import com.example.actvremotefreeware.core.ir.ac.HitachiBroadlink1084
import com.example.actvremotefreeware.core.ir.ac.HitachiBroadlink1091
import com.example.actvremotefreeware.core.ir.ac.HitachiMultiCode
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.FanSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HitachiProtocolsTest {

    @Test
    fun testBroadlinkPacketDecoder_Hitachi1084Leader() {
        val pulses = BroadlinkPacketDecoder.decode(HitachiBroadlink1084.OFF_CMD)
        assertTrue("Pulses must not be empty", pulses.isNotEmpty())
        assertEquals("Pulses must be even length for ConsumerIrManager", 0, pulses.size % 2)

        // Verify the critical 30ms / 50ms wake-up leader
        val leaderMark = pulses[0]
        val leaderSpace = pulses[1]
        assertTrue("Leader mark must be approx 30ms (was $leaderMark µs)", leaderMark in 28000..32000)
        assertTrue("Leader space must be approx 50ms (was $leaderSpace µs)", leaderSpace in 48000..52000)

        // Verify header
        val hdrMark = pulses[2]
        val hdrSpace = pulses[3]
        assertTrue("Header mark must be approx 3.4ms (was $hdrMark µs)", hdrMark in 3000..3800)
        assertTrue("Header space must be approx 1.7ms (was $hdrSpace µs)", hdrSpace in 1400..2000)
    }

    @Test
    fun testHitachiAc424Protocol_Synthesis() {
        val state = AcState(
            brand = AcBrand.HITACHI,
            power = true,
            temp = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO,
            swing = false
        )
        val pulses = HitachiAc424Protocol.encode(state)

        // 2 (leader) + 2 (header) + 53 * 8 * 2 (data) + 2 (footer) = 854 pulses
        assertEquals("HITACHI_AC424 pattern must have exactly 854 pulses", 854, pulses.size)

        // Verify leader
        assertEquals(29784, pulses[0])
        assertEquals(49290, pulses[1])

        // Verify header
        assertEquals(3416, pulses[2])
        assertEquals(1604, pulses[3])

        // Verify all pulses are positive
        for (p in pulses) {
            assertTrue("All pulse durations must be positive", p > 0)
        }

        // Verify power off alters state
        val stateOff = state.copy(power = false)
        val pulsesOff = HitachiAc424Protocol.encode(stateOff)
        assertEquals(854, pulsesOff.size)
        assertFalse("Power OFF pattern should differ from Power ON", pulses.contentEquals(pulsesOff))
    }

    @Test
    fun testHitachiAc28Protocol_Synthesis() {
        val state = AcState(brand = AcBrand.HITACHI, power = true, temp = 24)
        val pulses = HitachiAc28Protocol.encode(state)

        // 2 (leader) + 2 (header) + 28 * 8 * 2 (data) + 2 (footer) = 454 pulses
        assertEquals(454, pulses.size)
        assertTrue("Must have 30ms leader mark", pulses[0] in 28000..32000)
        assertTrue("Must have 50ms leader space", pulses[1] in 48000..52000)
    }

    @Test
    fun testHitachiAc1Protocol_KazeSeries() {
        val state = AcState(
            brand = AcBrand.HITACHI,
            power = true,
            temp = 24,
            mode = AcMode.COOL
        )
        val pulses = HitachiAc1Protocol.encode(state)

        // 2 (header) + 13 * 8 * 2 (data) + 2 (footer) = 212 pulses
        assertEquals(212, pulses.size)
        assertEquals("Header mark must be 3400 µs", 3400, pulses[0])
        assertEquals("Header space must be 3400 µs", 3400, pulses[1])
    }

    @Test
    fun testHitachiMultiCode_All10CodesGenerateValidWaveforms() {
        assertEquals("Hitachi must provide 10 distinct candidate code sets", 10, HitachiMultiCode.CODE_COUNT)
        assertEquals(10, BrandCodeRegistry.getCodeCount(AcBrand.HITACHI))

        val baseState = AcState(
            brand = AcBrand.HITACHI,
            power = true,
            temp = 24,
            mode = AcMode.COOL,
            fanSpeed = FanSpeed.AUTO
        )

        for (codeIndex in 0 until 10) {
            val label = BrandCodeRegistry.getCodeLabel(AcBrand.HITACHI, codeIndex)
            assertNotNull("Code label must not be null for index $codeIndex", label)
            assertTrue("Code label must mention Code ${codeIndex + 1}", label.startsWith("Code ${codeIndex + 1}"))

            val (frequency, pattern) = HitachiMultiCode.encode(baseState, codeIndex)
            assertEquals("Carrier frequency for Hitachi must be 38000 Hz", 38000, frequency)
            assertTrue("Pattern must not be empty for code index $codeIndex", pattern.isNotEmpty())
            assertEquals("Pattern length must be even for code index $codeIndex", 0, pattern.size % 2)

            for (p in pattern) {
                assertTrue("Pulse must be positive for code index $codeIndex", p > 0)
            }
        }
    }
}
