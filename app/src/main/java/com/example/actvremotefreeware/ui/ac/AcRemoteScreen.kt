package com.example.actvremotefreeware.ui.ac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.actvremotefreeware.core.ir.ac.BrandCodeRegistry
import com.example.actvremotefreeware.data.SavedRemote
import com.example.actvremotefreeware.ui.ac.components.AcControlPad
import com.example.actvremotefreeware.ui.ac.components.AcLcdDisplay
import com.example.actvremotefreeware.ui.ac.components.BrandSelectorDialog
import com.example.actvremotefreeware.ui.ac.components.TransmissionDetailsDialog
import com.example.actvremotefreeware.ui.share.ShareRemoteDialog
import com.example.actvremotefreeware.util.ShortcutUtils

/**
 * Main AC Remote Screen displaying digital LCD, tactile buttons, and on-the-fly code switching.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcRemoteScreen(
    remote: SavedRemote? = null,
    onBack: (() -> Unit)? = null,
    viewModel: AcRemoteViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    remember(remote) {
        if (remote != null) {
            viewModel.loadRemote(remote)
        }
        true
    }

    val state by viewModel.acState.collectAsState()
    val activeRemote by viewModel.activeRemote.collectAsState()
    val hasHardwareIr by viewModel.hasHardwareIr.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val lastTransmission by viewModel.lastTransmission.collectAsState()
    val isTransmitting by viewModel.isTransmitting.collectAsState()

    val context = LocalContext.current
    var showBrandDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showCodeSwitchDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val currentCodeIndex = activeRemote?.codeIndex ?: 0
    val totalBrandCodes = BrandCodeRegistry.getCodeCount(state.brand)
    val currentCodeLabel = BrandCodeRegistry.getCodeLabel(state.brand, currentCodeIndex)

    val titleText = activeRemote?.name ?: "${state.brand.displayName} AC"
    val subtitleText = if (activeRemote != null) "${activeRemote!!.room} • Code ${currentCodeIndex + 1} of $totalBrandCodes" else "Universal AC Controller"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = subtitleText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (activeRemote != null) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Share Remote")
                        }
                        IconButton(onClick = { ShortcutUtils.pinRemoteShortcut(context, activeRemote!!) }) {
                            Icon(Icons.Default.AddHome, contentDescription = "Add to Home Screen")
                        }
                    }
                    IconButton(onClick = { showCodeSwitchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Switch Code Profile"
                        )
                    }
                    IconButton(onClick = { showDiagnosticsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Diagnostics"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hardware Status Banner
            HardwareStatusBanner(
                hasHardwareIr = hasHardwareIr,
                onClick = { showDiagnosticsDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Active Code Quick-Switch Chip (Checking & Switching method)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCodeSwitchDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentCodeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Change ▸",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Digital LCD Screen
            AcLcdDisplay(
                state = state,
                hasHardwareIr = hasHardwareIr,
                isTransmitting = isTransmitting,
                onBrandClick = { showBrandDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tactile Remote Buttons Control Pad
            AcControlPad(
                state = state,
                onPowerClick = viewModel::togglePower,
                onTempUpClick = viewModel::increaseTemp,
                onTempDownClick = viewModel::decreaseTemp,
                onModeClick = viewModel::cycleMode,
                onModeSelect = viewModel::setMode,
                onFanClick = viewModel::cycleFanSpeed,
                onSwingClick = viewModel::toggleSwing,
                onTurboClick = viewModel::toggleTurbo,
                onBrandClick = { showBrandDialog = true },
                onResendClick = viewModel::resend,
                onDiagnosticsClick = { showDiagnosticsDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transmission Feedback Banner
            lastTransmission?.let { tx ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (tx.isSimulated) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (tx.isSimulated) "⚡ Simulated" else "✓ Transmitted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (tx.isSimulated) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${tx.patternLength} pulses @ ${tx.frequency / 1000}kHz",
                            fontSize = 11.sp,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Code Profile Switcher Dialog (Lets user test and switch codes immediately)
    if (showCodeSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showCodeSwitchDialog = false },
            title = {
                Column {
                    Text(
                        text = "Select Code Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${state.brand.displayName} has $totalBrandCodes candidate code sets",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(totalBrandCodes) { index ->
                        val label = BrandCodeRegistry.getCodeLabel(state.brand, index)
                        val isSelected = index == currentCodeIndex

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchCodeIndex(index)
                                    showCodeSwitchDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.switchCodeIndex(index)
                                        showCodeSwitchDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Brand Selector Dialog
    if (showBrandDialog) {
        BrandSelectorDialog(
            currentBrand = state.brand,
            onBrandSelected = viewModel::selectBrand,
            onDismiss = { showBrandDialog = false }
        )
    }

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        TransmissionDetailsDialog(
            result = lastTransmission,
            hardwareInfo = hardwareInfo,
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    // Share Remote QR Dialog
    if (showShareDialog && activeRemote != null) {
        ShareRemoteDialog(
            remote = activeRemote!!,
            onDismiss = { showShareDialog = false }
        )
    }
}

@Composable
private fun HardwareStatusBanner(
    hasHardwareIr: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasHardwareIr) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasHardwareIr) Icons.Default.Sensors else Icons.Default.SensorsOff,
                contentDescription = "IR Status",
                tint = if (hasHardwareIr) Color(0xFF2E7D32) else Color(0xFFE65100),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasHardwareIr) "Hardware IR Blaster: Active" else "Simulation / Test Mode Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasHardwareIr) Color(0xFF1B5E20) else Color(0xFFBF360C)
                )
                Text(
                    text = if (hasHardwareIr) "Ready to transmit to real AC units" else "Tap for details and pulse inspection",
                    fontSize = 10.sp,
                    color = if (hasHardwareIr) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            }
            Text(
                text = "Details ▸",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasHardwareIr) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
        }
    }
}
