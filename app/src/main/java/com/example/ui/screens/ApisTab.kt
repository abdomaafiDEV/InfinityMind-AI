package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.ApiKey
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfinityViewModel

@Composable
fun ApisTab(viewModel: InfinityViewModel) {
    val keysList by viewModel.apiKeysList.collectAsState()
    val testStatuses by viewModel.apiTestingStatus.collectAsState()

    var editingProvider by remember { mutableStateOf<String?>(null) }
    var inputKey by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        // Header
        CyberCard(borderColor = NeonBlue) {
            HologramHeader(
                title = "Credential Security Desk",
                subtitle = "Active operational telemetry grids & custom provider overrides"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Operational credentials are compiled locally using SQLite Room integration and never shared outside device environments. To replace fallback shared conduits, override a node key down below.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlowButton(
                text = "TEST ALL PORT GATEWAYS",
                onClick = {
                    viewModel.testAllApiKeys()
                    Toast.makeText(context, "Initiating diagnostic sweep on all nodes...", Toast.LENGTH_SHORT).show()
                },
                glowColor = NeonPurple,
                modifier = Modifier.fillMaxWidth().testTag("test_all_apis_button")
            )
        }

        Text(
            text = "COG COMPUTE COGNITIVE PROVIDERS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        // Providers Grid Layout
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keysList.forEach { keyObj ->
                val currentTestStatus = testStatuses[keyObj.modelProvider] ?: keyObj.healthStatus

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceGlow.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable {
                            editingProvider = keyObj.modelProvider
                            inputKey = keyObj.apiKey
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status glowing circle indicator
                    val statusColor = when (currentTestStatus) {
                        "Active" -> GlowGreen
                        "Testing..." -> GlowYellow
                        "Unavailable" -> CyberPink
                        "Unconfigured" -> Color.Gray
                        else -> GlowGreen
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = keyObj.modelProvider.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Tokens processed: ${keyObj.usageTokens}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Mode: ${if (keyObj.apiKey.isBlank()) "FALLBACK RELAY" else "DIRECT OPERATION"}",
                                color = if (keyObj.apiKey.isBlank()) NeonBlue.copy(alpha = 0.8f) else NeonPurple,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Key state indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (keyObj.apiKey.isBlank()) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = "Lock status",
                            tint = if (keyObj.apiKey.isBlank()) Color.Gray else GlowGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (keyObj.apiKey.isBlank()) "UNSECURED" else "ENCRYPTED",
                            color = if (keyObj.apiKey.isBlank()) Color.Gray else GlowGreen,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Node test trigger
                    IconButton(
                        onClick = { viewModel.testApiKeyHealth(keyObj.modelProvider) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.NetworkCheck, contentDescription = "Scan Node", tint = NeonBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Editing Key Dialogue
    editingProvider?.let { provider ->
        Dialog(onDismissRequest = { editingProvider = null }) {
            CyberCard(borderColor = NeonPurple) {
                Text(
                    text = "CONFIGURE OPERATIONAL KEY OVERRIDE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "NODE PROVIDER: $provider",
                    color = NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = {
                        Text(
                            "Enter secret credential token here...",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .testTag("api_key_override_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.deleteApiKey(provider)
                            editingProvider = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink)
                    ) {
                        Text("PURGE NODE", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row {
                        TextButton(onClick = { editingProvider = null }) {
                            Text("ABORT", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                viewModel.updateApiKey(provider, inputKey)
                                editingProvider = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                        ) {
                            Text("OVERRIDE", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
