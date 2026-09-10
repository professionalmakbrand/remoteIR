package com.example.actvremotefreeware

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.actvremotefreeware.core.ir.IrTransmitter
import com.example.actvremotefreeware.core.ir.ac.AcProtocolRouter
import com.example.actvremotefreeware.core.ir.tv.TvBrandCodeRegistry
import com.example.actvremotefreeware.data.SavedRemotesRepository
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.ApplianceType
import com.example.actvremotefreeware.model.TvBrand
import com.example.actvremotefreeware.model.TvCommand
import com.example.actvremotefreeware.ui.ac.AcRemoteScreen
import com.example.actvremotefreeware.ui.ac.components.TransmissionDetailsDialog
import com.example.actvremotefreeware.ui.dashboard.DashboardScreen
import com.example.actvremotefreeware.ui.tv.TvRemoteScreen
import com.example.actvremotefreeware.ui.wizard.BrandPickerScreen
import com.example.actvremotefreeware.ui.wizard.DeviceTypePickerScreen
import com.example.actvremotefreeware.ui.wizard.SaveRemoteDialog
import com.example.actvremotefreeware.ui.wizard.TestingWizardScreen
import com.example.actvremotefreeware.ui.wizard.TvBrandPickerScreen
import com.example.actvremotefreeware.ui.wizard.TvTestingWizardScreen

/**
 * Main application navigation implementing the OnePlus IR Remote user flow:
 * Dashboard -> Appliance Picker (AC / TV) -> Brand Picker -> Interactive Testing Wizard -> Save -> Active Remote.
 */
@Composable
fun MainNavigation(
    initialRemoteId: String? = null,
    initialRemoteType: String? = null
) {
    val context = LocalContext.current
    val repository = remember { SavedRemotesRepository(context) }
    val transmitter = remember { IrTransmitter(context) }
    val remotes by repository.remotes.collectAsState()

    val initialNavKey = remember(initialRemoteId, initialRemoteType) {
        if (!initialRemoteId.isNullOrBlank()) {
            if (initialRemoteType == ApplianceType.TV.name) {
                TvRemoteNavKey(initialRemoteId)
            } else {
                RemoteNavKey(initialRemoteId)
            }
        } else {
            null
        }
    }

    val backStack = if (initialNavKey != null) {
        rememberNavBackStack(DashboardNavKey, initialNavKey)
    } else {
        rememberNavBackStack(DashboardNavKey)
    }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            // 1. Home Dashboard
            entry<DashboardNavKey> {
                DashboardScreen(
                    remotes = remotes,
                    hasHardwareIr = transmitter.hasHardwareEmitter(),
                    onRemoteClick = { remote ->
                        if (remote.applianceType == ApplianceType.TV) {
                            backStack.add(TvRemoteNavKey(remote.id))
                        } else {
                            backStack.add(RemoteNavKey(remote.id))
                        }
                    },
                    onQuickPowerToggle = { remote ->
                        if (remote.applianceType == ApplianceType.TV) {
                            val tvBrand = remote.tvBrand ?: TvBrand.SAMSUNG
                            val (frequency, pattern) = TvBrandCodeRegistry.encode(tvBrand, TvCommand.POWER, remote.codeIndex)
                            transmitter.transmitGeneric(frequency, pattern)
                        } else {
                            val newState = remote.state.copy(power = !remote.state.power)
                            repository.updateRemoteState(remote.id, newState)
                            val (frequency, pattern) = AcProtocolRouter.buildPattern(newState, remote.codeIndex)
                            transmitter.transmit(remote.brand, frequency, pattern)
                        }
                    },
                    onDeleteRemote = { remote ->
                        repository.deleteRemote(remote.id)
                        Toast.makeText(context, "Deleted ${remote.name}", Toast.LENGTH_SHORT).show()
                    },
                    onImportRemote = { importedRemote ->
                        val saved = repository.importRemote(importedRemote)
                        Toast.makeText(context, "Imported ${saved.name}!", Toast.LENGTH_SHORT).show()
                    },
                    onAddClick = {
                        backStack.add(DeviceTypePickerNavKey)
                    },
                    onDiagnosticsClick = {
                        showDiagnosticsDialog = true
                    },
                    isLaunchedFromShortcut = (initialRemoteId != null),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Select Appliance (AC vs TV)
            entry<DeviceTypePickerNavKey> {
                DeviceTypePickerScreen(
                    onSelectAc = {
                        backStack.add(BrandPickerNavKey)
                    },
                    onSelectTv = {
                        backStack.add(TvBrandPickerNavKey)
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Select AC Brand
            entry<BrandPickerNavKey> {
                BrandPickerScreen(
                    onSelectBrand = { brand ->
                        backStack.add(TestingWizardNavKey(brand.name))
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Interactive AC Testing Wizard (OnePlus style: Code 1 of N)
            entry<TestingWizardNavKey> { key ->
                val brand = try {
                    AcBrand.valueOf(key.brandName)
                } catch (_: Exception) {
                    AcBrand.HITACHI
                }

                var pendingMatchedCodeIndex by remember { mutableStateOf<Int?>(null) }
                var pendingMatchedCodeDesc by remember { mutableStateOf("") }

                TestingWizardScreen(
                    brand = brand,
                    onSuccess = { matchedIndex, matchedDesc ->
                        pendingMatchedCodeIndex = matchedIndex
                        pendingMatchedCodeDesc = matchedDesc
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Save Dialog after user confirms "Yes, It Works!"
                pendingMatchedCodeIndex?.let { matchedIndex ->
                    SaveRemoteDialog(
                        brand = brand,
                        matchedCodeIndex = matchedIndex,
                        codeDescription = pendingMatchedCodeDesc,
                        onSave = { name, room ->
                            val newRemote = repository.saveRemote(
                                name = name,
                                room = room,
                                brand = brand,
                                codeIndex = matchedIndex,
                                codeDescription = pendingMatchedCodeDesc
                            )
                            pendingMatchedCodeIndex = null
                            // Navigate directly to active remote, popping wizard from stack
                            backStack.removeLastOrNull() // remove wizard
                            backStack.removeLastOrNull() // remove brand picker
                            backStack.removeLastOrNull() // remove device picker
                            backStack.add(RemoteNavKey(newRemote.id))
                        },
                        onDismiss = {
                            pendingMatchedCodeIndex = null
                        }
                    )
                }
            }

            // 5. Active AC Remote Screen
            entry<RemoteNavKey> { key ->
                val savedRemote = remotes.find { it.id == key.remoteId }
                AcRemoteScreen(
                    remote = savedRemote,
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 6. Select TV Brand
            entry<TvBrandPickerNavKey> {
                TvBrandPickerScreen(
                    onSelectBrand = { brand ->
                        backStack.add(TvTestingWizardNavKey(brand.name))
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 7. Interactive TV Testing Wizard
            entry<TvTestingWizardNavKey> { key ->
                val brand = try {
                    TvBrand.valueOf(key.brandName)
                } catch (_: Exception) {
                    TvBrand.SAMSUNG
                }

                TvTestingWizardScreen(
                    brand = brand,
                    onSuccess = { codeIndex, codeDesc, name, room ->
                        val newRemote = repository.saveTvRemote(
                            name = name,
                            room = room,
                            tvBrand = brand,
                            codeIndex = codeIndex,
                            codeDescription = codeDesc
                        )
                        // Navigate directly to active TV remote, popping wizard from stack
                        backStack.removeLastOrNull() // remove wizard
                        backStack.removeLastOrNull() // remove TV brand picker
                        backStack.removeLastOrNull() // remove device picker
                        backStack.add(TvRemoteNavKey(newRemote.id))
                    },
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 8. Active TV Remote Screen
            entry<TvRemoteNavKey> { key ->
                val savedRemote = remotes.find { it.id == key.remoteId }
                if (savedRemote != null) {
                    TvRemoteScreen(
                        remote = savedRemote,
                        onBack = {
                            backStack.removeLastOrNull()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    )

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        TransmissionDetailsDialog(
            result = null,
            hardwareInfo = transmitter.getCarrierFrequencyDescription(),
            onDismiss = { showDiagnosticsDialog = false }
        )
    }
}
