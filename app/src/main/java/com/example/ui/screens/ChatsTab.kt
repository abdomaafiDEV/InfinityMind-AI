package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.ChatMessage
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfinityViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatsTab(viewModel: InfinityViewModel) {
    val chatInput by viewModel.chatInput.collectAsState()
    val isLeftLoading by viewModel.isChatLoadingLeft.collectAsState()
    val isRightLoading by viewModel.isChatLoadingRight.collectAsState()
    
    val selectedModelLeft by viewModel.selectedModelLeft.collectAsState()
    val selectedModelRight by viewModel.selectedModelRight.collectAsState()
    val comparisonMode by viewModel.comparisonMode.collectAsState()

    val chatMessagesLeft by viewModel.chatMessagesLeft.collectAsState()
    val chatMessagesRight by viewModel.chatMessagesRight.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listStateLeft = rememberLazyListState()
    val listStateRight = rememberLazyListState()

    var showModelSelectLeft by remember { mutableStateOf(false) }
    var showModelSelectRight by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // AutoScroll on message list updates
    LaunchedEffect(chatMessagesLeft.size, isLeftLoading) {
        if (chatMessagesLeft.isNotEmpty()) {
            listStateLeft.animateScrollToItem(chatMessagesLeft.size - 1)
        }
    }
    LaunchedEffect(chatMessagesRight.size, isRightLoading) {
        if (chatMessagesRight.isNotEmpty() && comparisonMode) {
            listStateRight.animateScrollToItem(chatMessagesRight.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Model Selection Command Console
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model Selector Left
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceGlow.copy(alpha = 0.6f))
                    .border(1.dp, NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { showModelSelectLeft = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "PRIMARY COGNITIVE ENGINE",
                            fontSize = 8.sp,
                            color = NeonBlue,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = selectedModelLeft,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown Left",
                        tint = NeonBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Comparison Switch Toggle Button
            IconButton(
                onClick = { viewModel.setComparisonMode(!comparisonMode) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (comparisonMode) NeonPurple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                    .border(
                        1.dp,
                        if (comparisonMode) NeonPurple else Color.Gray.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .size(44.dp)
            ) {
                Icon(
                    Icons.Filled.Compare,
                    contentDescription = "Toggle Comparison",
                    tint = if (comparisonMode) NeonPurple else Color.White
                )
            }

            if (comparisonMode) {
                Spacer(modifier = Modifier.width(10.dp))
                
                // Model Selector Right Helper
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceGlow.copy(alpha = 0.6f))
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { showModelSelectRight = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "SECONDARY ENGINE",
                                fontSize = 8.sp,
                                color = NeonPurple,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = selectedModelRight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown Right",
                            tint = NeonPurple
                        )
                    }
                }
            }
        }

        // Active Chat Space Area
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Left Workspace Chat Panel
            Column(modifier = Modifier.weight(1f)) {
                if (comparisonMode) {
                    Text(
                        text = "CONSOLE A: $selectedModelLeft",
                        fontSize = 10.sp,
                        color = NeonBlue,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF060613))
                        .border(1.dp, NeonBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                ) {
                    if (chatMessagesLeft.isEmpty()) {
                        EmptyChatState(selectedModelLeft, viewModel::setChatInput)
                    } else {
                        LazyColumn(
                            state = listStateLeft,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chatMessagesLeft) { msg ->
                                MessageBubble(
                                    msg = msg,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(msg.text))
                                        Toast.makeText(context, "Copied statement to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            if (isLeftLoading) {
                                item { LoadingResponseIndicator(selectedModelLeft) }
                            }
                        }
                    }
                }
            }

            // Right Workspace Comparison Panel (Only rendered if comparison Mode is ON)
            if (comparisonMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONSOLE B: $selectedModelRight",
                        fontSize = 10.sp,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF060613))
                            .border(1.dp, NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    ) {
                        if (chatMessagesRight.isEmpty()) {
                            EmptyChatState(selectedModelRight, viewModel::setChatInput)
                        } else {
                            LazyColumn(
                                state = listStateRight,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(chatMessagesRight) { msg ->
                                    MessageBubble(
                                        msg = msg,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "Copied alternative to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                if (isRightLoading) {
                                    item { LoadingResponseIndicator(selectedModelRight) }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Unified Cyber Input Deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New Session Action
            IconButton(
                onClick = { viewModel.createNewChatSession() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .size(46.dp)
                    .testTag("reset_chat_button")
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Restart Stream Session",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Glassmorphic Interactive Core Text Field
            TextField(
                value = chatInput,
                onValueChange = { viewModel.setChatInput(it) },
                placeholder = {
                    Text(
                        text = "Enter query descriptor code...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkGlass)
                    .border(
                        1.dp,
                        Brush.linearGradient(colors = listOf(NeonBlue.copy(alpha = 0.8f), NeonPurple.copy(alpha = 0.4f))),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("chat_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Quantum Core Sent Request Launcher
            IconButton(
                onClick = { viewModel.sendChatMessage() },
                enabled = chatInput.isNotBlank() && !isLeftLoading && !isRightLoading,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (chatInput.isNotBlank()) {
                            Brush.linearGradient(colors = listOf(NeonBlue, NeonPurple))
                        } else {
                            Brush.linearGradient(colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.2f)))
                        }
                    )
                    .size(46.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Transmit Stream Data",
                    tint = Color.White
                )
            }
        }
    }

    // ==========================================
    // Dialogue Selection Models Sheets
    // ==========================================
    val modelOptions = listOf(
        "Gemini 3.5 Flash", "DeepSeek-R1", "GPT-4o (OpenAI)", "Claude 3.5 Sonnet", "Grok 2 (xAI)", "Mistral Large"
    )

    if (showModelSelectLeft) {
        ModelSelectionDialog(
            title = "Select Primary Engine Node",
            options = modelOptions,
            selected = selectedModelLeft,
            onSelect = {
                viewModel.setModelLeft(it)
                showModelSelectLeft = false
            },
            onDismiss = { showModelSelectLeft = false }
        )
    }

    if (showModelSelectRight) {
        ModelSelectionDialog(
            title = "Select Secondary Engine Node",
            options = modelOptions,
            selected = selectedModelRight,
            onSelect = {
                viewModel.setModelRight(it)
                showModelSelectRight = false
            },
            onDismiss = { showModelSelectRight = false }
        )
    }
}

@Composable
fun MessageBubble(
    msg: ChatMessage,
    onCopy: () -> Unit
) {
    val isUser = msg.sender == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (msg.provider.contains("DeepSeek")) CyberPink else NeonBlue)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = if (isUser) "OPERATOR [YOU]" else "${msg.modelName.uppercase()}",
                fontSize = 9.sp,
                color = if (isUser) NeonBlue.copy(alpha = 0.8f) else NeonPurple.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(colors = listOf(Color(0xFF1E1035), Color(0xFF0F0A2B)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFF0C132C), Color(0xFF05081A)))
                    }
                )
                .border(
                    width = 0.8.dp,
                    color = if (isUser) NeonPurple.copy(alpha = 0.4f) else NeonBlue.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .clickable { onCopy() }
                .padding(10.dp)
        ) {
            Column {
                FormattedMessageText(text = msg.text)
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy text",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TAP TO COPY",
                        fontSize = 7.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedMessageText(text: String) {
    // Process thinking blocks (<think>...</think>) or code segments (`...`)
    val hasThinking = text.contains("<think>") && text.contains("</think>")
    
    if (hasThinking) {
        val parts = text.split("<think>", "</think>")
        Column {
            if (parts.size >= 2) {
                // Think text
                Text(
                    text = "THINKING ROUTES ENABLED:",
                    color = CyberPink,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x35FF007F), RoundedCornerShape(4.dp))
                        .border(1.dp, CyberPink.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = parts[1].trim(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Remaining text
                val mainText = if (parts.size > 2) parts[2] else ""
                Text(
                    text = mainText.trim(),
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    } else {
        // Detect standard console code blocks in markdown: ``` ... ```
        val hasCodeBlock = text.contains("```")
        if (hasCodeBlock) {
            val segments = text.split("```")
            Column {
                segments.forEachIndexed { index, seg ->
                    if (index % 2 == 1) {
                        // This is code
                        Spacer(modifier = Modifier.height(4.dp))
                        GlowingTerminal(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "COMPILED STRUCT SNIPPET:",
                                color = GlowGreen,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = seg.trim(),
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.background(Color.Black).fillMaxWidth().padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        if (seg.isNotBlank()) {
                            Text(
                                text = seg.trim(),
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }
        } else {
            // General clean text
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
fun LoadingResponseIndicator(modelName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "terminal_loader")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(NeonBlue)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "LINKING TO ${modelName.uppercase()} DEEP NEURAL WORKSPACE RELAY...",
            fontSize = 9.sp,
            color = NeonBlue.copy(alpha = alphaAnim),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyChatState(modelName: String, onPillClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.BlurOn,
            contentDescription = "Portal Core",
            tint = NeonBlue.copy(alpha = 0.3f),
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "COG COMPUTE PORTAL READY",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Workspace is synced with $modelName cloud nodes. Issue logical queries or deploy specific task commands down below.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Suggestion Chips
        Text(
            text = "READY PRESETS:",
            color = NeonPurple,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        val pills = listOf(
            "Synthesize quantum physics theory",
            "Generate Kotlin mock database class",
            "Analyze global market trends of 2026"
        )
        pills.forEach { pill ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(0.8.dp, PaintStrokeGlow(index = 1), RoundedCornerShape(6.dp))
                    .clickable { onPillClick(pill) }
                    .padding(8.dp)
            ) {
                Text(
                    text = pill,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PaintStrokeGlow(index: Int): Color {
    return if (index % 2 == 0) NeonBlue.copy(alpha = 0.3f) else NeonPurple.copy(alpha = 0.3f)
}

@Composable
fun ModelSelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            borderColor = NeonBlue
        ) {
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            options.forEach { opt ->
                val isSelected = opt == selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) NeonBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) NeonBlue else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelect(opt) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt,
                            color = if (isSelected) NeonBlue else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = NeonBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "CLOSE CONNECTION",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
