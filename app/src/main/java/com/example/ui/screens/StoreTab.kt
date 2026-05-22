package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoreTab() {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Mock installation progress state
    val installationProgress = remember { mutableStateMapOf<String, Float>() }
    val installationState = remember { mutableStateMapOf<String, String>() } // "INSTALL", "DOWNLOADING...", "INSTALLED"

    val plugins = remember {
        listOf(
            PluginItem(
                id = "pl_finance",
                title = "Neuro-Financial Catalyst",
                description = "Predictive temporal market analysis and asset synthesis algorithms using localized DeepLogic arrays.",
                category = "ANALYTICS",
                version = "v3.1.2",
                rating = "4.9",
                cost = "FREE"
            ),
            PluginItem(
                id = "pl_blueprint",
                title = "Semantic Android Blueprint",
                description = "Compiles custom high-level Compose declarations into full production-grade structural specs.",
                category = "DEVELOPER",
                version = "v1.0.5",
                rating = "5.0",
                cost = "FREE"
            ),
            PluginItem(
                id = "pl_voice",
                title = "Synthesized Voice Node",
                description = "Clones regional accent profiles and converts streaming inputs into multi-lingual audio packets.",
                category = "AUDIO",
                version = "v2.0.0",
                rating = "4.8",
                cost = "FREE"
            ),
            PluginItem(
                id = "pl_security",
                title = "Aegis Threat Sentinel",
                description = "Monitors proxy token vulnerabilities and defends cloud relays against structural security anomalies.",
                category = "SECURITY",
                version = "v4.0.1",
                rating = "4.9",
                cost = "FREE"
            ),
            PluginItem(
                id = "pl_canvas",
                title = "Holographic Vector Brush",
                description = "Interpolates low-res matrix inputs into ultra-high-definition canvas drawings.",
                category = "GRAPHICS",
                version = "v2.4.0",
                rating = "4.7",
                cost = "FREE"
            )
        )
    }

    val filteredPlugins = plugins.filter {
        it.title.lowercase().contains(searchQuery.lowercase()) ||
        it.description.lowercase().contains(searchQuery.lowercase()) ||
        it.category.lowercase().contains(searchQuery.lowercase())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        // Welcome Header Banner
        CyberCard(borderColor = NeonPurple) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INFINITY PLUGINS COGNITIVE RELAY",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Load customized intelligence sub-systems directly into the OS shell.",
                        color = NeonBlue,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    Icons.Filled.Storefront,
                    contentDescription = "Store",
                    tint = NeonBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Search Bar deck
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search sub-system indices...",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search icon", tint = NeonBlue, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkGlass)
                .border(1.dp, NeonBlue.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        )

        Text(
            text = "DISCOVER COGNITIVE CORES (${filteredPlugins.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        // Listed market plugins
        filteredPlugins.forEach { plug ->
            val currentState = installationState[plug.id] ?: "DEPLOY NODE"
            val currentProgress = installationProgress[plug.id] ?: 0f

            CyberCard(borderColor = if (currentState == "INSTALLED") GlowGreen else NeonBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(NeonBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(0.8.dp, NeonBlue, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = plug.category,
                                    color = NeonBlue,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = plug.version,
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Text(
                            text = plug.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = GlowYellow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = plug.rating,
                            color = GlowYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = plug.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Installation Progress bar UI
                if (currentState == "DOWNLOADING...") {
                    CyberProgressBar(progress = currentProgress)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LICENSE: ${plug.cost}",
                        color = GlowGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            if (currentState == "DEPLOY NODE") {
                                coroutineScope.launch {
                                    installationState[plug.id] = "DOWNLOADING..."
                                    for (i in 1..10) {
                                        delay(400)
                                        installationProgress[plug.id] = (i / 10f)
                                    }
                                    installationState[plug.id] = "INSTALLED"
                                    Toast.makeText(context, "${plug.title} loaded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = currentState == "DEPLOY NODE",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentState == "INSTALLED") Color(0xFF142F1C) else NeonBlue,
                            disabledContainerColor = if (currentState == "INSTALLED") Color(0xFF142F1C) else Color.White.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentState == "INSTALLED") {
                                Icon(Icons.Filled.Check, contentDescription = "Success", tint = GlowGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COMPILED OK", color = GlowGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            } else {
                                Text(currentState, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PluginItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val version: String,
    val rating: String,
    val cost: String
)
