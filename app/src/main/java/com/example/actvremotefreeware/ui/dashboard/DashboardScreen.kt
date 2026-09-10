package com.example.actvremotefreeware.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actvremotefreeware.BuildConfig
import com.example.actvremotefreeware.billing.DonationManager
import com.example.actvremotefreeware.data.SavedRemote
import com.example.actvremotefreeware.model.ApplianceType
import com.example.actvremotefreeware.ui.about.AboutAndDonateDialog
import com.example.actvremotefreeware.ui.about.SupportReminderDialog
import com.example.actvremotefreeware.ui.share.ScanRemoteDialog
import com.example.actvremotefreeware.ui.share.ShareRemoteDialog
import com.example.actvremotefreeware.util.ShortcutUtils

/**
 * Main Home Dashboard displaying all configured user remotes (AC & TV),
 * with fast QR sharing/importing, launcher shortcuts, creator attribution, and cup of tea donation support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    remotes: List<SavedRemote>,
    hasHardwareIr: Boolean,
    onRemoteClick: (SavedRemote) -> Unit,
    onQuickPowerToggle: (SavedRemote) -> Unit,
    onDeleteRemote: (SavedRemote) -> Unit,
    onImportRemote: (SavedRemote) -> Unit,
    onAddClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    isLaunchedFromShortcut: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val donationManager = remember { DonationManager(context) }

    var remoteToShare by remember { mutableStateOf<SavedRemote?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember {
        mutableStateOf(!isLaunchedFromShortcut && donationManager.shouldShowDashboardReminder())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Remotes", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            text = if (remotes.isNotEmpty()) "${remotes.size} device${if (remotes.size > 1) "s" else ""} configured" else "Universal IR Remote",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showScanDialog = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Remote QR")
                    }
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Default.Coffee, contentDescription = "About & Support", tint = Color(0xFF795548))
                    }
                    IconButton(onClick = onDiagnosticsClick) {
                        Icon(Icons.Default.Sensors, contentDescription = "Diagnostics")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Remote", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Hardware Status Banner
            Card(
                onClick = onDiagnosticsClick,
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
                        contentDescription = null,
                        tint = if (hasHardwareIr) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasHardwareIr) "IR Blaster Ready (OnePlus 11R)" else "Simulation / Test Mode Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasHardwareIr) Color(0xFF1B5E20) else Color(0xFFBF360C)
                        )
                        Text(
                            text = if (hasHardwareIr) "Physical IR transmitter active and transmitting" else "App will simulate transmissions without crashing",
                            fontSize = 10.sp,
                            color = if (hasHardwareIr) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (remotes.isEmpty()) {
                // Empty State
                EmptyDashboardState(
                    onAddClick = onAddClick,
                    onScanClick = { showScanDialog = true }
                )
            } else {
                // List of Remotes
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(remotes, key = { it.id }) { remote ->
                        SavedRemoteCard(
                            remote = remote,
                            onClick = { onRemoteClick(remote) },
                            onPowerToggle = { onQuickPowerToggle(remote) },
                            onShare = { remoteToShare = remote },
                            onPinShortcut = { ShortcutUtils.pinRemoteShortcut(context, remote) },
                            onDelete = { onDeleteRemote(remote) }
                        )
                    }

                    // App Version and Attribution Footer
                    item {
                        Card(
                            onClick = { showAboutDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "AC TV Remote Freeware v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Developed by Advit Singh • 100% Free & Ad-Free • Tap for Info & Support",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Share Remote QR Dialog
    if (remoteToShare != null) {
        ShareRemoteDialog(
            remote = remoteToShare!!,
            onDismiss = { remoteToShare = null }
        )
    }

    // Scan / Import Remote Dialog
    if (showScanDialog) {
        ScanRemoteDialog(
            onRemoteImported = { imported ->
                onImportRemote(imported)
                showScanDialog = false
            },
            onDismiss = { showScanDialog = false }
        )
    }

    // About & Support Dialog
    if (showAboutDialog) {
        AboutAndDonateDialog(
            donationManager = donationManager,
            onDismiss = { showAboutDialog = false }
        )
    }

    // 3-Day Dashboard Support Reminder Dialog
    if (showReminderDialog && !isLaunchedFromShortcut) {
        SupportReminderDialog(
            donationManager = donationManager,
            onDismiss = { showReminderDialog = false }
        )
    }
}

@Composable
private fun EmptyDashboardState(
    onAddClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Remotes Added Yet",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your Air Conditioner, TV, or Set-Top Box to start controlling it directly with your phone's IR blaster, or scan a remote QR code from a friend.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Remote", fontWeight = FontWeight.SemiBold)
            }

            androidx.compose.material3.OutlinedButton(
                onClick = onScanClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan QR", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SavedRemoteCard(
    remote: SavedRemote,
    onClick: () -> Unit,
    onPowerToggle: () -> Unit,
    onShare: () -> Unit,
    onPinShortcut: () -> Unit,
    onDelete: () -> Unit
) {
    val isTv = remote.applianceType == ApplianceType.TV

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isTv) Icons.Default.Tv else Icons.Default.AcUnit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = remote.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = remote.room,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val subtitle = if (isTv) {
                        val brandLabel = remote.tvBrand?.displayName ?: "TV"
                        val codeLabel = remote.codeDescription.ifBlank { "Code ${remote.codeIndex + 1}" }
                        "$brandLabel • $codeLabel"
                    } else {
                        val brandLabel = remote.brand.displayName
                        val codeLabel = remote.codeDescription.ifBlank { "Code ${remote.codeIndex + 1}" }
                        val stateLabel = if (remote.state.power) "${remote.state.temp}°C ${remote.state.mode.displayName}" else "OFF"
                        "$brandLabel • $codeLabel • $stateLabel"
                    }

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Power Button
                val powerColor = if (!isTv && remote.state.power) Color(0xFF2E7D32) else Color(0xFFB0BEC5)
                IconButton(
                    onClick = onPowerToggle,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Quick Power",
                        tint = if (isTv) Color(0xFFD32F2F) else powerColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Bar for each remote card: Share QR, Pin to Home Screen, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share QR
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Share QR",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Pin Shortcut
                IconButton(
                    onClick = onPinShortcut,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddHome,
                        contentDescription = "Add to Home Screen",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
