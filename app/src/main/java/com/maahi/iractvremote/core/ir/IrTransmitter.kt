package com.maahi.iractvremote.core.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.IrTransmissionResult

/**
 * Hardware IR Transmitter wrapper around Android ConsumerIrManager.
 * Gracefully simulates transmission and records pulse details on devices without hardware IR blaster.
 */
class IrTransmitter(private val context: Context) {

    private val irManager: ConsumerIrManager? by lazy {
        try {
            context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring ConsumerIrManager", e)
            null
        }
    }

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    fun hasHardwareEmitter(): Boolean {
        return try {
            irManager?.hasIrEmitter() == true
        } catch (e: Exception) {
            Log.w(TAG, "hasIrEmitter check failed", e)
            false
        }
    }

    fun getCarrierFrequencyDescription(): String {
        return try {
            val ranges = irManager?.carrierFrequencies
            if (ranges != null && ranges.isNotEmpty()) {
                ranges.joinToString(", ") { "${it.minFrequency / 1000}kHz-${it.maxFrequency / 1000}kHz" }
            } else if (hasHardwareEmitter()) {
                "30kHz - 60kHz (Standard IR)"
            } else {
                "Hardware IR Blaster Not Detected (Simulation Mode Active)"
            }
        } catch (e: Exception) {
            "Simulation Mode Active"
        }
    }

    fun transmit(brand: AcBrand, frequency: Int, pattern: IntArray): IrTransmissionResult {
        triggerHapticFeedback()

        if (pattern.isEmpty()) {
            return IrTransmissionResult(
                success = false,
                brand = brand,
                frequency = frequency,
                patternLength = 0,
                isSimulated = true,
                message = "Pattern is empty",
                patternPreview = "[]"
            )
        }

        val preview = pattern.take(8).joinToString(prefix = "[", postfix = if (pattern.size > 8) ", ...]" else "]")

        return if (hasHardwareEmitter()) {
            try {
                irManager?.transmit(frequency, pattern)
                Log.d(TAG, "IR transmitted successfully: ${pattern.size} pulses @ ${frequency}Hz for $brand")
                IrTransmissionResult(
                    success = true,
                    brand = brand,
                    frequency = frequency,
                    patternLength = pattern.size,
                    isSimulated = false,
                    message = "Transmitted via IR Blaster (${pattern.size} pulses @ ${frequency / 1000}kHz)",
                    patternPreview = preview
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to transmit IR signal", e)
                IrTransmissionResult(
                    success = false,
                    brand = brand,
                    frequency = frequency,
                    patternLength = pattern.size,
                    isSimulated = false,
                    message = "Hardware transmit error: ${e.localizedMessage}",
                    patternPreview = preview
                )
            }
        } else {
            Log.i(TAG, "Simulated IR transmit: ${pattern.size} pulses @ ${frequency}Hz for $brand: $preview")
            IrTransmissionResult(
                success = true,
                brand = brand,
                frequency = frequency,
                patternLength = pattern.size,
                isSimulated = true,
                message = "Simulated: Generated ${pattern.size} pulses @ ${frequency / 1000}kHz for ${brand.displayName}",
                patternPreview = preview
            )
        }
    }

    fun transmitGeneric(frequency: Int, pattern: IntArray, deviceLabel: String = "Device"): IrTransmissionResult {
        triggerHapticFeedback()

        if (pattern.isEmpty()) {
            return IrTransmissionResult(
                success = false,
                brand = AcBrand.HITACHI,
                frequency = frequency,
                patternLength = 0,
                isSimulated = true,
                message = "Pattern is empty",
                patternPreview = "[]"
            )
        }

        val preview = pattern.take(8).joinToString(prefix = "[", postfix = if (pattern.size > 8) ", ...]" else "]")

        return if (hasHardwareEmitter()) {
            try {
                irManager?.transmit(frequency, pattern)
                Log.d(TAG, "IR transmitted successfully: ${pattern.size} pulses @ ${frequency}Hz for $deviceLabel")
                IrTransmissionResult(
                    success = true,
                    brand = AcBrand.HITACHI,
                    frequency = frequency,
                    patternLength = pattern.size,
                    isSimulated = false,
                    message = "Transmitted via IR Blaster (${pattern.size} pulses @ ${frequency / 1000}kHz)",
                    patternPreview = preview
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to transmit IR signal", e)
                IrTransmissionResult(
                    success = false,
                    brand = AcBrand.HITACHI,
                    frequency = frequency,
                    patternLength = pattern.size,
                    isSimulated = false,
                    message = "Hardware transmit error: ${e.localizedMessage}",
                    patternPreview = preview
                )
            }
        } else {
            Log.i(TAG, "Simulated IR transmit: ${pattern.size} pulses @ ${frequency}Hz for $deviceLabel: $preview")
            IrTransmissionResult(
                success = true,
                brand = AcBrand.HITACHI,
                frequency = frequency,
                patternLength = pattern.size,
                isSimulated = true,
                message = "Simulated: Generated ${pattern.size} pulses @ ${frequency / 1000}kHz for $deviceLabel",
                patternPreview = preview
            )
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    companion object {
        private const val TAG = "IrTransmitter"
    }
}
