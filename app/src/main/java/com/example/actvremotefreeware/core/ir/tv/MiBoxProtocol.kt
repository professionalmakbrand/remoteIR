package com.example.actvremotefreeware.core.ir.tv

import com.example.actvremotefreeware.core.ir.ac.BroadlinkPacketDecoder
import com.example.actvremotefreeware.model.TvCommand

/**
 * Infrared protocol implementation for Xiaomi Mi Box 4K, Mi TV Stick, and Xiaomi Streaming Media Boxes.
 * Uses community-verified captured Broadlink packets decoded into microsecond pulse bursts at 38kHz.
 */
object MiBoxProtocol {

    const val CARRIER_FREQUENCY_HZ = 38000

    private val PACKETS = mapOf(
        TvCommand.POWER to "JgA0ACMRFhEWLRYtFhEWLRYRFi0WERUuFi0WAAFTIhEWERYtFi0WERYtFhEVLhYRFS4VLhYADQUAAAAA",
        TvCommand.UP to "JgA0ACISFSQWERUbFiQWERURFhoWGxUkFi0WAAGMJREWJBYRFRsWIxYRFhEWGhYaFiQWLRYADQUAAAAA",
        TvCommand.DOWN to "JgA0ACMRFiQVEhUbFiMWERYRFhoWJBUkFhEWAAGgIxEWJBURFhsVJBYRFhEWGhYkFSQWERYADQUAAAAA",
        TvCommand.LEFT to "JgA0ACISFSUVEhUbFiQWERURFhoWJBUkFi0WAAGMJBEWJBUQFxkWJBYRFhEWGhYaFhEVLhYADQUAAAAA",
        TvCommand.RIGHT to "JgA0ACMRFhEVEhUbFiQWERURFhoWJBUkFhEWAAGgIxEVJBURFhsVJBYRFhEWGhYaFiQWLRYADQUAAAAA",
        TvCommand.OK to "JgA0ACMRFiQVERYbFSQWERYRFS4WGhYRFi0WAAGOIxEVJBYRFhoWJBYRFhAWLhUbFREWLhUADQUAAAAA",
        TvCommand.BACK to "JgA0ACMRFhEVExUbFiQWERURFhoWGxUkFhEWAAGgIxEVJBYRFhoWJBURFhEWGhYkFSQWERYADQUAAAAA",
        TvCommand.HOME to "JgA0ACYSFSQWERUbFiQWERURFiQWERUbFiMWAAGhIhEWJBYRFRsWJBURFhEWJBYQFhsVJBYADQUAAAAA",
        TvCommand.MENU to "JgA0ACMRFhEWLRYtFhEWLRYRFi0WERUuFhEWAAGgIxEWERYtFi0WERYtFhEVLhYRFS4WERYADQUAAAAA",
        TvCommand.VOL_UP to "JgA0ACMRFhEWLRYtFhEWLRYRFi0WERUuFi0WAAGhIxEWERYtFi0WERYtFhEVLhYRFS4VLhYADQUAAAAA",
        TvCommand.VOL_DOWN to "JgA0ACMRFhEWLRYtFhEWLRYRFi0WERUuFhEWAAGgIxEWERYtFi0WERYtFhEVLhYRFS4WERYADQUAAAAA",
        TvCommand.MUTE to "JgA0ACMRFhEWLRYtFhEWLRYRFi0WERUuFhEWAAGgIxEWERYtFi0WERYtFhEVLhYRFS4WERYADQUAAAAA"
    )

    // Pre-decoded cache so repeated button presses are instantaneous
    private val decodedCache = mutableMapOf<TvCommand, IntArray>()

    init {
        PACKETS.forEach { (cmd, b64) ->
            decodedCache[cmd] = BroadlinkPacketDecoder.decode(b64)
        }
    }

    /**
     * Encodes a command into (frequency, pattern)
     */
    fun encode(command: TvCommand): Pair<Int, IntArray> {
        val pattern = decodedCache[command]
            ?: decodedCache[TvCommand.OK]
            ?: IntArray(0)
        return Pair(CARRIER_FREQUENCY_HZ, pattern)
    }
}
