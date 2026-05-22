package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.GeneratedAsset
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfinityViewModel
import kotlin.math.sin

@Composable
fun GeneratorsTab(viewModel: InfinityViewModel) {
    val prompt by viewModel.generatorPrompt.collectAsState()
    val isGenerating by viewModel.isGeneratingAsset.collectAsState()
    val assetType by viewModel.generatorType.collectAsState()
    val activeAsset by viewModel.activeGeneratedAsset.collectAsState()
    val modelSelected by viewModel.generatorModel.collectAsState()
    val allAssets by viewModel.generatedAssetsList.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val filteredAssets = allAssets.filter { it.assetType == assetType }

    // Dropdown models lists
    val imageModels = listOf("Stability AI - Flux.1", "Midjourney Pro v6", "Dall-E 3 (OpenAI)", "Gemini Image Core")
    val videoModels = listOf("Runway Gen-3 Alpha", "Sora fast (OpenAI)", "Luma Dream Machine", "Veo Cinematic")
    val musicModels = listOf("Suno AI v4", "Udio Beta", "Lyria Symphony", "Riffusion Synth")
    val codeModels = listOf("InfinityMind - Coder v2", "Claude 3.5 Compiler", "OpenAI Codex Core")

    val activeModels = when (assetType) {
        "IMAGE" -> imageModels
        "VIDEO" -> videoModels
        "MUSIC" -> musicModels
        "CODE" -> codeModels
        else -> imageModels
    }

    var showModelDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        // Multi-media structural asset selector bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("IMAGE", "VIDEO", "MUSIC", "CODE").forEach { type ->
                val isSelected = assetType == type
                val icon = when (type) {
                    "IMAGE" -> Icons.Filled.Image
                    "VIDEO" -> Icons.Filled.Videocam
                    "MUSIC" -> Icons.Filled.MusicNote
                    "CODE" -> Icons.Filled.Code
                    else -> Icons.Filled.Add
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            if (isSelected) NeonBlue else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setGeneratorType(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon,
                            contentDescription = type,
                            tint = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = type,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active parameter selection deck
        CyberCard(borderColor = NeonBlue) {
            Text(
                text = "NEURAL MODULATOR CONFIGURATION",
                color = NeonBlue,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Selector Model dropdown trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .clickable { showModelDropdown = true }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SettingsInputHdmi, contentDescription = "Engine Connection", tint = NeonPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE MODEL: $modelSelected",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
            }

            if (showModelDropdown) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeModels) { model ->
                        val isMatched = model == modelSelected
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isMatched) NeonPurple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isMatched) NeonPurple else Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.setGeneratorModel(model)
                                    showModelDropdown = false
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = model,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main prompt field
            Text(
                text = "COGNITIVE SYNTHESIZER PROMPT INPUT",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            TextField(
                value = prompt,
                onValueChange = { viewModel.setGeneratorPrompt(it) },
                placeholder = {
                    Text(
                        "Input detailed visual layout, BPM mapping, or function spec...",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .testTag("generator_input_field"),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Manifest Launcher Action Button
            GlowButton(
                text = if (isGenerating) "Synthesizing Asset Nodes..." else "TRIGGER MANIFESTATION NETWORK",
                onClick = { viewModel.generateAsset() },
                glowColor = NeonBlue,
                enabled = prompt.isNotBlank() && !isGenerating,
                modifier = Modifier.fillMaxWidth().testTag("generate_asset_button")
            )
        }

        // Processing Hologram Loader
        if (isGenerating) {
            GlowingTerminal(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = ">>> INITIATING LOCAL QUANTUM COMPILING PIPELINE...",
                    color = GlowGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                CyberProgressBar(progress = 0.55f)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Status: Fetching vector parameters from remote GPU array...",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Active Manifestation Preview Stage
        activeAsset?.let { asset ->
            CyberCard(borderColor = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANIFESTED PREVIEW CORE",
                        color = NeonPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { viewModel.deleteAsset(asset.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Purge Asset", tint = CyberPink, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    when (asset.assetType) {
                        "IMAGE" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Landscape, contentDescription = "Asset Graphic", tint = NeonBlue, modifier = Modifier.size(50.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = asset.resultUrlOrLocal,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        "VIDEO" -> {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsRun, contentDescription = "Motion Control", tint = GlowYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CINEMATIC FRAMES DIRECTIVE:",
                                        color = GlowYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = asset.resultUrlOrLocal,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        "MUSIC" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SYNTHESIZED HARMONIC SCORES",
                                    color = CyberPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Beautiful Synthesizer live animation waves drawing canvas
                                WaveformSimulationCanvas()

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = asset.resultUrlOrLocal,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        "CODE" -> {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LIVE COMPILER VIEW",
                                        color = GlowGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(asset.resultUrlOrLocal))
                                            Toast.makeText(context, "Copied compiler code to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy code", tint = GlowGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = asset.resultUrlOrLocal,
                                    color = Color.Green,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(Color(0xFF020205))
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prompt parameters: \"${asset.prompt}\"",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Historical Asset Archive Gallery
        Text(
            text = "HISTORICAL INTEGRATION ARCHIVE (${filteredAssets.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No manifested assets discovered on this system node.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredAssets.forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.02f))
                            .border(0.8.dp, PaintStrokeGlow(index = asset.id.toInt()), RoundedCornerShape(6.dp))
                            .clickable { viewModel.generateAsset() /* Quick simulate reloading asset */ }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val leadingIcon = when (asset.assetType) {
                            "IMAGE" -> Icons.Filled.Image
                            "VIDEO" -> Icons.Filled.Videocam
                            "MUSIC" -> Icons.Filled.MusicNote
                            "CODE" -> Icons.Filled.Code
                            else -> Icons.Filled.InsertDriveFile
                        }
                        Icon(leadingIcon, contentDescription = "Doc", tint = NeonBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (asset.prompt.length > 30) "${asset.prompt.take(30)}..." else asset.prompt,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Model: ${asset.modelUsed} | Node ${asset.id}",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = { viewModel.deleteAsset(asset.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CyberPink, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaveformSimulationCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "music_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(Color.Black)
    ) {
        val width = size.width
        val height = size.height
        val points = 100
        val step = width / points

        val pathBrush = Brush.horizontalGradient(
            colors = listOf(NeonBlue, CyberPink, NeonPurple)
        )

        for (i in 0 until points - 1) {
            val x1 = i * step
            // Multi-sine wave formulas representing active synthesizer
            val y1 = height / 2 + 18 * sin((i * 0.12f) + waveOffset) * sin((i * 0.05f) + waveOffset * 0.5f)
            
            val x2 = (i + 1) * step
            val y2 = height / 2 + 18 * sin(((i + 1) * 0.12f) + waveOffset) * sin(((i + 1) * 0.05f) + waveOffset * 0.5f)

            drawLine(
                brush = pathBrush,
                start = Offset(x1, y1.toFloat()),
                end = Offset(x2, y2.toFloat()),
                strokeWidth = 3f
            )
        }
    }
}
