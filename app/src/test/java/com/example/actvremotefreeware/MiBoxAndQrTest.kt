package com.example.actvremotefreeware

import com.example.actvremotefreeware.core.ir.tv.MiBoxProtocol
import com.example.actvremotefreeware.core.ir.tv.TvBrandCodeRegistry
import com.example.actvremotefreeware.data.SavedRemote
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.ApplianceType
import com.example.actvremotefreeware.model.FanSpeed
import com.example.actvremotefreeware.model.TvBrand
import com.example.actvremotefreeware.model.TvCommand
import com.example.actvremotefreeware.util.QrCodeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiBoxAndQrTest {

    @Test
    fun testMiBoxProtocol_AllCommandsEncodeProperly() {
        val testCommands = listOf(
            TvCommand.POWER,
            TvCommand.UP,
            TvCommand.DOWN,
            TvCommand.LEFT,
            TvCommand.RIGHT,
            TvCommand.OK,
            TvCommand.BACK,
            TvCommand.HOME,
            TvCommand.MENU,
            TvCommand.VOL_UP,
            TvCommand.VOL_DOWN,
            TvCommand.MUTE
        )

        for (cmd in testCommands) {
            val (freq, pulses) = MiBoxProtocol.encode(cmd)
            assertEquals(38000, freq)
            assertTrue("Command $cmd should produce pulses", pulses.isNotEmpty())
            assertEquals("Consumer IR requires an even number of pulses", 0, pulses.size % 2)
            // Verify pulses are valid positive microsecond timings
            for (p in pulses) {
                assertTrue("Pulse duration must be > 0", p > 0)
            }
        }
    }

    @Test
    fun testXiaomiTvBrand_IncludesMiBox4K() {
        val count = TvBrandCodeRegistry.getCodeCount(TvBrand.XIAOMI)
        assertTrue("Xiaomi should have at least 4 candidate codes", count >= 4)

        val miBoxLabel = TvBrandCodeRegistry.getCodeLabel(TvBrand.XIAOMI, 2)
        assertTrue("Code 3 should mention Mi Box 4K", miBoxLabel.contains("Mi Box 4K"))

        val (freq, pulses) = TvBrandCodeRegistry.encode(TvBrand.XIAOMI, TvCommand.POWER, 2)
        assertEquals(38000, freq)
        assertTrue(pulses.isNotEmpty())
        assertEquals(0, pulses.size % 2)
    }

    @Test
    fun testQrCodeUtils_AcRemoteSerializationRoundTrip() {
        val original = SavedRemote(
            id = "test-ac-123",
            name = "Hitachi Master Bed",
            room = "Bedroom",
            brand = AcBrand.HITACHI,
            codeIndex = 5,
            state = AcState(
                brand = AcBrand.HITACHI,
                temp = 25,
                mode = AcMode.COOL,
                fanSpeed = FanSpeed.MED,
                power = true
            ),
            applianceType = ApplianceType.AC,
            codeDescription = "Code 6: Hitachi 2018 Inverter (Captured 1084)"
        )

        val json = QrCodeUtils.serializeRemote(original)
        assertNotNull(json)
        assertTrue(json.contains("ACTVRemote"))
        assertTrue(json.contains("HITACHI"))

        val deserialized = QrCodeUtils.deserializeRemote(json)
        assertNotNull(deserialized)
        assertEquals("Hitachi Master Bed", deserialized!!.name)
        assertEquals("Bedroom", deserialized.room)
        assertEquals(AcBrand.HITACHI, deserialized.brand)
        assertEquals(5, deserialized.codeIndex)
        assertEquals(ApplianceType.AC, deserialized.applianceType)
        assertEquals(25, deserialized.state.temp)
        assertEquals(AcMode.COOL, deserialized.state.mode)
        assertEquals(FanSpeed.MED, deserialized.state.fanSpeed)
        assertEquals("Code 6: Hitachi 2018 Inverter (Captured 1084)", deserialized.codeDescription)
    }

    @Test
    fun testQrCodeUtils_TvRemoteSerializationRoundTrip() {
        val original = SavedRemote(
            id = "test-tv-456",
            name = "Living Room Mi Box",
            room = "Living Room",
            codeIndex = 2,
            applianceType = ApplianceType.TV,
            tvBrand = TvBrand.XIAOMI,
            codeDescription = "Code 3: Mi Box 4K / TV Stick (Xiaomi Protocol)"
        )

        val json = QrCodeUtils.serializeRemote(original)
        assertNotNull(json)
        assertTrue(json.contains("ACTVRemote"))
        assertTrue(json.contains("XIAOMI"))

        val deserialized = QrCodeUtils.deserializeRemote(json)
        assertNotNull(deserialized)
        assertEquals("Living Room Mi Box", deserialized!!.name)
        assertEquals("Living Room", deserialized.room)
        assertEquals(TvBrand.XIAOMI, deserialized.tvBrand)
        assertEquals(2, deserialized.codeIndex)
        assertEquals(ApplianceType.TV, deserialized.applianceType)
        assertEquals("Code 3: Mi Box 4K / TV Stick (Xiaomi Protocol)", deserialized.codeDescription)
    }
}
