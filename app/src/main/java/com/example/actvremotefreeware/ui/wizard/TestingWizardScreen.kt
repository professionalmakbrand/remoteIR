package com.example.actvremotefreeware.ui.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.actvremotefreeware.core.ir.ac.AcProtocolRouter
import com.example.actvremotefreeware.core.ir.ac.BrandCodeRegistry
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.IrTransmissionResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive Testing Wizard implementing the OnePlus IR Remote pairing flow:
 * Testing Code 1 of N -> Tap Power / Test buttons -> "Did it beep? Yes / No".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestingWizardScreen(
    brand: AcBrand,
    onSuccess: (codeIndex: Int, codeDescription: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transmitter = remember { IrTransmitter(context) }
    val totalCodes = remember(brand) { BrandCodeRegistry.getCodeCount(brand) }
    var currentCodeIndex by remember { mutableIntStateOf(0) }
    var isTransmitting by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<IrTransmissionResult?>(null) }
    var hasTappedTest by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pairing ${brand.displayName} AC", fontWeight = FontWeight.Bold) },
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

            // Detailed Protocol Sub-label
            val currentCodeLabel = BrandCodeRegistry.getCodeLabel(brand, currentCodeIndex)
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
                        contentDescription = "Point Remote",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Point phone towards your AC and tap TEST POWER. You can also test Temp & Mode below to verify.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Test Power Button
            val buttonColor by animateColorAsState(
                targetValue = if (isTransmitting) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                label = "ButtonColor"
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        isTransmitting = true
                        hasTappedTest = true

                        val testState = AcState(
                            brand = brand,
                            power = true,
                            temp = 24,
                            mode = AcMode.COOL
                        )
                        val (frequency, pattern) = AcProtocolRouter.buildPattern(testState, currentCodeIndex)
                        val result = transmitter.transmit(brand, frequency, pattern)
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
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isTransmitting) "SENDING" else "TEST POWER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Checking Buttons (Direct verification)
            Text(
                text = "Extra Check Methods",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Temp (+)
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransmitting = true
                            hasTappedTest = true
                            val testState = AcState(brand = brand, power = true, temp = 25, mode = AcMode.COOL)
                            val (freq, pattern) = AcProtocolRouter.buildPattern(testState, currentCodeIndex)
                            lastResult = transmitter.transmit(brand, freq, pattern)
                            delay(200)
                            isTransmitting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temp 25°", fontSize = 11.sp)
                }

                // Test Mode
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransmitting = true
                            hasTappedTest = true
                            val testState = AcState(brand = brand, power = true, temp = 24, mode = AcMode.FAN)
                            val (freq, pattern) = AcProtocolRouter.buildPattern(testState, currentCodeIndex)
                            lastResult = transmitter.transmit(brand, freq, pattern)
                            delay(200)
                            isTransmitting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mode", fontSize = 11.sp)
                }

                // Test Power OFF
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransmitting = true
                            hasTappedTest = true
                            val testState = AcState(brand = brand, power = false, temp = 24)
                            val (freq, pattern) = AcProtocolRouter.buildPattern(testState, currentCodeIndex)
                            lastResult = transmitter.transmit(brand, freq, pattern)
                            delay(200)
                            isTransmitting = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Off", fontSize = 11.sp)
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

            // Response Question Section (visible after first test tap)
            AnimatedVisibility(visible = hasTappedTest) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Did your AC respond / beep?",
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
                                onSuccess(currentCodeIndex, currentCodeLabel)
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
}
