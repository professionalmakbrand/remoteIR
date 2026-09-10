package com.maahi.iractvremote.ui.ac.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.FanSpeed

/**
 * Authentic backlit Digital LCD Screen for the Air Conditioner remote.
 */
@Composable
fun AcLcdDisplay(
    state: AcState,
    hasHardwareIr: Boolean,
    isTransmitting: Boolean,
    onBrandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // LCD Backlight colors: Soft ice-blue/cyan when ON, muted gray when OFF
    val lcdBackground = if (state.power) Color(0xFFC7E2E0) else Color(0xFF2B3235)
    val lcdTextColor = if (state.power) Color(0xFF14292C) else Color(0xFF5A666A)
    val lcdDimColor = if (state.power) Color(0x3314292C) else Color(0x1A5A666A)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(3.dp, Color(0xFF374145)),
        colors = CardDefaults.cardColors(containerColor = lcdBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Status Bar: Brand badge, IR Transmit LED indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable Brand Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.power) Color(0x2B14292C) else Color(0x2BFFFFFF),
                    modifier = Modifier.clickable { onBrandClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.brand.displayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = lcdTextColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            fontSize = 8.sp,
                            color = lcdTextColor
                        )
                    }
                }

                // IR Signal LED & Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (hasHardwareIr) "IR READY" else "SIMULATED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = lcdTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val ledColor by animateColorAsState(
                        targetValue = if (isTransmitting) Color(0xFF00E676) else if (state.power) Color(0xFFE53935) else Color(0xFF757575),
                        animationSpec = tween(durationMillis = 100),
                        label = "LedColor"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ledColor)
                            .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle: Mode Icons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeIndicator(
                    label = "COOL",
                    icon = Icons.Default.AcUnit,
                    active = state.power && state.mode == AcMode.COOL,
                    activeColor = lcdTextColor,
                    dimColor = lcdDimColor
                )
                ModeIndicator(
                    label = "HEAT",
                    icon = Icons.Default.WbSunny,
                    active = state.power && state.mode == AcMode.HEAT,
                    activeColor = lcdTextColor,
                    dimColor = lcdDimColor
                )
                ModeIndicator(
                    label = "DRY",
                    icon = Icons.Default.Grain,
                    active = state.power && state.mode == AcMode.DRY,
                    activeColor = lcdTextColor,
                    dimColor = lcdDimColor
                )
                ModeIndicator(
                    label = "FAN",
                    icon = Icons.Default.Air,
                    active = state.power && state.mode == AcMode.FAN,
                    activeColor = lcdTextColor,
                    dimColor = lcdDimColor
                )
                ModeIndicator(
                    label = "AUTO",
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    active = state.power && state.mode == AcMode.AUTO,
                    activeColor = lcdTextColor,
                    dimColor = lcdDimColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Center: Giant Temperature Readout & Units
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (state.power) "${state.temp}" else "--",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = lcdTextColor,
                    lineHeight = 72.sp
                )
                Text(
                    text = "°C",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = lcdTextColor,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Status: Fan Speed Bars & Swing / Turbo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fan speed gauge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FAN: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = lcdTextColor
                    )
                    Text(
                        text = state.fanSpeed.displayName.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = lcdTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FanBars(
                        speed = state.fanSpeed,
                        power = state.power,
                        activeColor = lcdTextColor,
                        dimColor = lcdDimColor
                    )
                }

                // Swing & Turbo badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.turbo && state.power) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Turbo",
                                tint = lcdTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TURBO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = lcdTextColor
                            )
                        }
                    }

                    Text(
                        text = if (state.power && state.swing) "SWING ON" else if (state.power) "SWING OFF" else "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.swing) lcdTextColor else lcdDimColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeIndicator(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    dimColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else dimColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) activeColor else dimColor
        )
    }
}

@Composable
private fun FanBars(
    speed: FanSpeed,
    power: Boolean,
    activeColor: Color,
    dimColor: Color
) {
    Row(verticalAlignment = Alignment.Bottom) {
        val level = if (!power) 0 else speed.level // 0: Auto, 1: Low, 2: Med, 3: High
        val isAuto = speed == FanSpeed.AUTO && power

        val barHeights = listOf(6.dp, 10.dp, 14.dp)
        for (i in 0..2) {
            val isActive = isAuto || (level > i)
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(4.dp)
                    .height(barHeights[i])
                    .background(if (isActive) activeColor else dimColor, RoundedCornerShape(1.dp))
            )
        }
    }
}
