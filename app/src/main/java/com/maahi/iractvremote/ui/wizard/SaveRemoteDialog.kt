package com.maahi.iractvremote.ui.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maahi.iractvremote.model.AcBrand

/**
 * Save Remote Dialog to name the verified remote and assign it to a room.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaveRemoteDialog(
    brand: AcBrand,
    matchedCodeIndex: Int,
    codeDescription: String = "",
    onSave: (name: String, room: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("${brand.displayName} AC") }
    var selectedRoom by remember { mutableStateOf("Bedroom") }

    val roomOptions = listOf("Bedroom", "Living Room", "Office", "Dining Room", "Kids Room", "Guest Room")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pairing Successful! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (codeDescription.isNotBlank()) codeDescription else "Matched ${brand.displayName} (Code ${matchedCodeIndex + 1})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Name Input
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

                // Room selection chips
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
                    val finalName = name.ifBlank { "${brand.displayName} AC" }
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
