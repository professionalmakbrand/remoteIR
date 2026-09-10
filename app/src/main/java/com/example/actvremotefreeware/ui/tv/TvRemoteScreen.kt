package com.example.actvremotefreeware.ui.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import com.example.actvremotefreeware.ui.share.ShareRemoteDialog
import com.example.actvremotefreeware.util.ShortcutUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actvremotefreeware.core.ir.IrTransmitter
import com.example.actvremotefreeware.core.ir.tv.TvBrandCodeRegistry
import com.example.actvremotefreeware.data.SavedRemote
import com.example.actvremotefreeware.data.SavedRemotesRepository
import com.example.actvremotefreeware.model.IrTransmissionResult
import com.example.actvremotefreeware.model.TvBrand
import com.example.actvremotefreeware.model.TvCommand
import com.example.actvremotefreeware.ui.ac.components.TransmissionDetailsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tactile TV Remote Control Screen featuring Power, Mute, Volume/Channel rockers,
 * Directional D-Pad, Home/Back, Number Pad, and Code Profile switcher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvRemoteScreen(
    remote: SavedRemote,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { SavedRemotesRepository(context) }
    val transmitter = remember { IrTransmitter(context) }
    val coroutineScope = rememberCoroutineScope()

    val brand = remote.tvBrand ?: TvBrand.SAMSUNG
    val totalBrandCodes = TvBrandCodeRegistry.getCodeCount(brand)
    var activeCodeIndex by remember { mutableIntStateOf(remote.codeIndex) }

    var lastTransmission by remember { mutableStateOf<IrTransmissionResult?>(null) }
    var isTransmitting by remember { mutableStateOf(false) }
    var showCodeSwitchDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showNumberPad by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val currentCodeLabel = TvBrandCodeRegistry.getCodeLabel(brand, activeCodeIndex)

    fun transmit(command: TvCommand) {
        coroutineScope.launch {
            isTransmitting = true
            val (frequency, pattern) = TvBrandCodeRegistry.encode(brand, command, activeCodeIndex)
            val result = transmitter.transmitGeneric(frequency, pattern)
            lastTransmission = result
            delay(120)
            isTransmitting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(remote.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${remote.room} • Code ${activeCodeIndex + 1} of $totalBrandCodes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Share Remote")
                    }
                    IconButton(onClick = { ShortcutUtils.pinRemoteShortcut(context, remote) }) {
                        Icon(Icons.Default.AddHome, contentDescription = "Add to Home Screen")
                    }
                    IconButton(onClick = { showCodeSwitchDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Switch Code")
                    }
                    IconButton(onClick = { showDiagnosticsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Diagnostics")
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Code Quick-Switch Chip
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

            Spacer(modifier = Modifier.height(16.dp))

            // Remote Control Physical Chassis
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Power, Input/Source, Mute
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Power Button (Red)
                        IconButton(
                            onClick = { transmit(TvCommand.POWER) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = Color.White)
                        }

                        // Source / Input
                        IconButton(
                            onClick = { transmit(TvCommand.INPUT) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Input, contentDescription = "Source")
                        }

                        // Mute Button
                        IconButton(
                            onClick = { transmit(TvCommand.MUTE) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.VolumeMute, contentDescription = "Mute")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Row 2: Volume & Channel Rockers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Volume Rocker
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.width(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                IconButton(onClick = { transmit(TvCommand.VOL_UP) }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Vol +")
                                }
                                Text("VOL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                IconButton(onClick = { transmit(TvCommand.VOL_DOWN) }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Vol -")
                                }
                            }
                        }

                        // Center: Quick action buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalIconButton(onClick = { transmit(TvCommand.HOME) }) {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            }
                            FilledTonalIconButton(onClick = { transmit(TvCommand.MENU) }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }

                        // Channel Rocker
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.width(64.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                IconButton(onClick = { transmit(TvCommand.CH_UP) }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "CH +")
                                }
                                Text("CH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                IconButton(onClick = { transmit(TvCommand.CH_DOWN) }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "CH -")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Row 3: D-Pad Navigation Cluster
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(190.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // UP
                            IconButton(
                                onClick = { transmit(TvCommand.UP) },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", modifier = Modifier.size(32.dp))
                            }

                            // DOWN
                            IconButton(
                                onClick = { transmit(TvCommand.DOWN) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", modifier = Modifier.size(32.dp))
                            }

                            // LEFT
                            IconButton(
                                onClick = { transmit(TvCommand.LEFT) },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 8.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", modifier = Modifier.size(32.dp))
                            }

                            // RIGHT
                            IconButton(
                                onClick = { transmit(TvCommand.RIGHT) },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", modifier = Modifier.size(32.dp))
                            }

                            // CENTER OK BUTTON
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { transmit(TvCommand.OK) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "OK",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Row 4: Back & Number Pad Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilledTonalButton(
                            onClick = { transmit(TvCommand.BACK) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        FilledTonalButton(
                            onClick = { showNumberPad = !showNumberPad },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (showNumberPad) "Hide 123" else "Num Pad (123)", fontSize = 12.sp)
                        }
                    }

                    // Number Pad (Optional Expandable)
                    if (showNumberPad) {
                        Spacer(modifier = Modifier.height(16.dp))
                        NumberPad(onDigitClick = { digit ->
                            val cmd = when (digit) {
                                0 -> TvCommand.DIGIT_0
                                1 -> TvCommand.DIGIT_1
                                2 -> TvCommand.DIGIT_2
                                3 -> TvCommand.DIGIT_3
                                4 -> TvCommand.DIGIT_4
                                5 -> TvCommand.DIGIT_5
                                6 -> TvCommand.DIGIT_6
                                7 -> TvCommand.DIGIT_7
                                8 -> TvCommand.DIGIT_8
                                else -> TvCommand.DIGIT_9
                            }
                            transmit(cmd)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transmission info toast
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Code Profile Switcher Dialog
    if (showCodeSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showCodeSwitchDialog = false },
            title = {
                Column {
                    Text("Select TV Code Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "${brand.displayName} has $totalBrandCodes candidate code sets",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(totalBrandCodes) { index ->
                        val label = TvBrandCodeRegistry.getCodeLabel(brand, index)
                        val isSelected = index == activeCodeIndex

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeCodeIndex = index
                                    repository.updateRemoteCodeIndex(remote.id, index, label)
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
                                        activeCodeIndex = index
                                        repository.updateRemoteCodeIndex(remote.id, index, label)
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

    // Diagnostics Dialog
    if (showDiagnosticsDialog) {
        TransmissionDetailsDialog(
            result = lastTransmission,
            hardwareInfo = transmitter.getCarrierFrequencyDescription(),
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    // Share Remote QR Dialog
    if (showShareDialog) {
        ShareRemoteDialog(
            remote = remote.copy(codeIndex = activeCodeIndex, codeDescription = currentCodeLabel),
            onDismiss = { showShareDialog = false }
        )
    }
}

@Composable
private fun NumberPad(onDigitClick: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9),
            listOf(-1, 0, -1)
        )

        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (digit in row) {
                    if (digit == -1) {
                        Spacer(modifier = Modifier.size(48.dp))
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onDigitClick(digit) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$digit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
