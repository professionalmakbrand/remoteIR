package com.maahi.iractvremote.core.ir.ac

import com.maahi.iractvremote.model.AcState

/**
 * Routes the active AcState to its designated protocol engine using the selected code set index.
 * Returns Pair(carrierFrequencyInHz, patternArrayInMicroseconds).
 */
object AcProtocolRouter {

    fun buildPattern(state: AcState, codeIndex: Int = 0): Pair<Int, IntArray> {
        return BrandCodeRegistry.encode(state, codeIndex)
    }
}
