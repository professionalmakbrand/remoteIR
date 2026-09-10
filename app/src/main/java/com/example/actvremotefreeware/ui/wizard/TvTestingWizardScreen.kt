package com.example.actvremotefreeware.ui.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actvremotefreeware.core.ir.IrTransmitter
import com.example.actvremotefreeware.core.ir.tv.TvBrandCodeRegistry
import com.example.actvremotefreeware.model.IrTransmissionResult
import com.example.actvremotefreeware.model.TvBrand
import com.example.actvremotefreeware.model.TvCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive Testing Wizard for Television remote pairing:
 * Testing Code 1 of N -> Tap Power / Mute / Vol+ -> "Did it respond? Yes / No".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvTestingWizardScreen(
    brand: TvBrand,
    onSuccess: (codeIndex: Int, codeDescription: String, name: String, room: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transmitter = remember { IrTransmitter(context) }
    val totalCodes = remember(brand) { TvBrandCodeRegistry.getCodeCount(brand) }
    var currentCodeIndex by remember { mutableIntStateOf(0) }
    var isTransmitting by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<IrTransmissionResult?>(null) }
    var hasTappedTest by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val currentCodeLabel = TvBrandCodeRegistry.getCodeLabel(brand, currentCodeIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pairing ${brand.displayName} TV", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Bar
            val progress = (currentCodeIndex + 1).toFloat() / totalCodes.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Code Indicator Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Testing Code ${currentCodeIndex + 1} of $totalCodes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentCodeLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Point phone towards your ${brand.displayName} TV and tap TEST POWER or TEST VOL + below.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Test Power Button
            val buttonColor by animateColorAsState(
                targetValue = if (isTransmitting) Color(0xFF00E676) else Color(0xFFD32F2F),
                label = "ButtonColor"
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        isTransmitting = true
                        hasTappedTest = true

                        val (frequency, pattern) = TvBrandCodeRegistry.encode(brand, TvCommand.POWER, currentCodeIndex)
                        val result = transmitter.transmitGeneric(frequency, pattern)
                        lastResult = result

                        delay(250)
                        isTransmitting = false
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.size(105.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Test Power",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isTransmitting) "SENDING" else "TEST POWER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Test Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Mute
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransmitting = true
                            hasTappedTest = true
                            val (freq, pattern) = TvBrandCodeRegistry.encode(brand, TvCommand.MUTE, currentCodeIndex)
                            lastResult = transmitter.transmitGeneric(freq, pattern)
                            delay(200)
                            isTransmitting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VolumeMute, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Mute", fontSize = 12.sp)
                }

                // Test Vol +
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransmitting = true
                            hasTappedTest = true
                            val (freq, pattern) = TvBrandCodeRegistry.encode(brand, TvCommand.VOL_UP, currentCodeIndex)
                            lastResult = transmitter.transmitGeneric(freq, pattern)
                            delay(200)
                            isTransmitting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Vol +", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transmission info toast
            lastResult?.let { tx ->
                Text(
                    text = if (tx.isSimulated) "Simulated ${tx.patternLength} pulses" else "Transmitted ${tx.patternLength} pulses @ ${tx.frequency / 1000}kHz",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Response Question Section (visible after test tap)
            AnimatedVisibility(visible = hasTappedTest) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Did your TV respond?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // NO BUTTON (Try Next Code)
                        OutlinedButton(
                            onClick = {
                                if (currentCodeIndex < totalCodes - 1) {
                                    currentCodeIndex++
                                } else {
                                    currentCodeIndex = 0
                                }
                                lastResult = null
                                hasTappedTest = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentCodeIndex < totalCodes - 1) "No (Next Code)" else "No (Restart)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // YES BUTTON (Success!)
                        Button(
                            onClick = {
                                showSaveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yes, It Works!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Save TV Remote Dialog
    if (showSaveDialog) {
        SaveTvRemoteDialog(
            brand = brand,
            codeIndex = currentCodeIndex,
            codeDescription = currentCodeLabel,
            onSave = { name, room ->
                showSaveDialog = false
                onSuccess(currentCodeIndex, currentCodeLabel, name, room)
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SaveTvRemoteDialog(
    brand: TvBrand,
    codeIndex: Int,
    codeDescription: String,
    onSave: (name: String, room: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("${brand.displayName} TV") }
    var selectedRoom by remember { mutableStateOf("Living Room") }
    val roomOptions = listOf("Living Room", "Bedroom", "Drawing Room", "Office", "Guest Room")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "TV Pairing Successful! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = codeDescription,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Remote Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Room",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    roomOptions.forEach { room ->
                        FilterChip(
                            selected = selectedRoom == room,
                            onClick = { selectedRoom = room },
                            label = { Text(room, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.ifBlank { "${brand.displayName} TV" }
                    onSave(finalName, selectedRoom)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Remote", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
