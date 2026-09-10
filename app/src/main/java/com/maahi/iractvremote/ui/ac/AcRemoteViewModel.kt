package com.maahi.iractvremote.ui.ac

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maahi.iractvremote.core.ir.IrTransmitter
import com.maahi.iractvremote.core.ir.ac.AcProtocolRouter
import com.maahi.iractvremote.core.ir.ac.BrandCodeRegistry
import com.maahi.iractvremote.data.SavedRemote
import com.maahi.iractvremote.data.SavedRemotesRepository
import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed
import com.maahi.iractvremote.model.IrTransmissionResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing the active AC Remote state, code profile, and IR transmissions.
 */
class AcRemoteViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SavedRemotesRepository(application)
    private val transmitter = IrTransmitter(application)

    private val _activeRemote = MutableStateFlow<SavedRemote?>(null)
    val activeRemote: StateFlow<SavedRemote?> = _activeRemote.asStateFlow()

    private val _acState = MutableStateFlow(AcState(brand = AcBrand.HITACHI))
    val acState: StateFlow<AcState> = _acState.asStateFlow()

    private var activeCodeIndex = 0

    private val _hasHardwareIr = MutableStateFlow(transmitter.hasHardwareEmitter())
    val hasHardwareIr: StateFlow<Boolean> = _hasHardwareIr.asStateFlow()

    private val _hardwareInfo = MutableStateFlow(transmitter.getCarrierFrequencyDescription())
    val hardwareInfo: StateFlow<String> = _hardwareInfo.asStateFlow()

    private val _lastTransmission = MutableStateFlow<IrTransmissionResult?>(null)
    val lastTransmission: StateFlow<IrTransmissionResult?> = _lastTransmission.asStateFlow()

    private val _isTransmitting = MutableStateFlow(false)
    val isTransmitting: StateFlow<Boolean> = _isTransmitting.asStateFlow()

    fun loadRemote(remote: SavedRemote) {
        _activeRemote.value = remote
        _acState.value = remote.state
        activeCodeIndex = remote.codeIndex
    }

    fun togglePower() {
        _acState.update { current ->
            val updated = current.copy(power = !current.power)
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun increaseTemp() {
        _acState.update { current ->
            if (!current.power) return@update current
            val newTemp = (current.temp + 1).coerceAtMost(30)
            if (newTemp != current.temp) {
                val updated = current.copy(temp = newTemp)
                transmit(updated)
                persistUpdatedState(updated)
                updated
            } else {
                current
            }
        }
    }

    fun decreaseTemp() {
        _acState.update { current ->
            if (!current.power) return@update current
            val newTemp = (current.temp - 1).coerceAtLeast(16)
            if (newTemp != current.temp) {
                val updated = current.copy(temp = newTemp)
                transmit(updated)
                persistUpdatedState(updated)
                updated
            } else {
                current
            }
        }
    }

    fun setMode(mode: AcMode) {
        _acState.update { current ->
            if (!current.power) return@update current
            val updated = current.copy(mode = mode)
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun cycleMode() {
        _acState.update { current ->
            if (!current.power) return@update current
            val modes = AcMode.entries
            val nextIndex = (modes.indexOf(current.mode) + 1) % modes.size
            val updated = current.copy(mode = modes[nextIndex])
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun cycleFanSpeed() {
        _acState.update { current ->
            if (!current.power) return@update current
            val speeds = FanSpeed.entries
            val nextIndex = (speeds.indexOf(current.fanSpeed) + 1) % speeds.size
            val updated = current.copy(fanSpeed = speeds[nextIndex])
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun toggleSwing() {
        _acState.update { current ->
            if (!current.power) return@update current
            val updated = current.copy(swing = !current.swing)
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun toggleTurbo() {
        _acState.update { current ->
            if (!current.power) return@update current
            val updated = current.copy(turbo = !current.turbo)
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun selectBrand(brand: AcBrand) {
        _acState.update { current ->
            activeCodeIndex = 0
            val updated = current.copy(brand = brand)
            transmit(updated)
            persistUpdatedState(updated)
            updated
        }
    }

    fun switchCodeIndex(newIndex: Int) {
        activeCodeIndex = newIndex
        val desc = BrandCodeRegistry.getCodeLabel(_acState.value.brand, newIndex)
        _activeRemote.value?.let { remote ->
            repository.updateRemoteCodeIndex(remote.id, newIndex, desc)
            _activeRemote.value = remote.copy(codeIndex = newIndex, codeDescription = desc)
        }
        transmit(_acState.value)
    }

    fun setCodeIndex(index: Int) {
        activeCodeIndex = index
        transmit(_acState.value)
    }

    fun resend() {
        transmit(_acState.value)
    }

    private fun persistUpdatedState(state: AcState) {
        _activeRemote.value?.let { remote ->
            repository.updateRemoteState(remote.id, state)
        }
    }

    private fun transmit(state: AcState) {
        viewModelScope.launch {
            _isTransmitting.value = true
            val (frequency, pattern) = AcProtocolRouter.buildPattern(state, activeCodeIndex)
            val result = transmitter.transmit(state.brand, frequency, pattern)
            _lastTransmission.value = result

            delay(150)
            _isTransmitting.value = false
        }
    }
}
