package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AiAgent
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfinityViewModel

@Composable
fun AgentsTab(viewModel: InfinityViewModel) {
    val agents by viewModel.aiAgentsList.collectAsState()
    val selectedId by viewModel.selectedAgentId.collectAsState()
    val inputTask by viewModel.agentInputTask.collectAsState()

    val currentAgent = agents.find { it.agentId == selectedId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        // Profiles selector Row
        Text(
            text = "COGNITIVE SUB-AGENT CLUSTERS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agents.forEach { agent ->
                val isSelected = agent.agentId == selectedId
                val icon = when (agent.agentId) {
                    "agent_web_search" -> Icons.Filled.Language
                    "agent_coder" -> Icons.Filled.AutoFixHigh
                    "agent_orchestrator" -> Icons.Filled.Hub
                    else -> Icons.Filled.Android
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            if (isSelected) NeonPurple else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectAgent(agent.agentId) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            icon,
                            contentDescription = agent.agentName,
                            tint = if (isSelected) NeonPurple else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = agent.agentName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = agent.status.uppercase(),
                            color = if (agent.status == "Idle") Color.Gray else GlowGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Selected Agent parameters and task deployment
        currentAgent?.let { agent ->
            CyberCard(borderColor = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = agent.agentName.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = agent.category,
                            color = NeonPurple,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    // Reset agent controller
                    IconButton(
                        onClick = { viewModel.resetAgentState(agent.agentId) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Filled.PowerSettingsNew, contentDescription = "Kill process", tint = CyberPink, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status segment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STATUS PROFILE:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = agent.status.uppercase(),
                        color = if (agent.status == "Idle") Color.Gray else GlowYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "OBJECTIVE CORRELATION: ${agent.currentTask}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (agent.status != "Idle") {
                    CyberProgressBar(progress = agent.progress)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Objectives entry box
                if (agent.status == "Idle") {
                    Text(
                        text = "DISPATCH AUTONOMOUS CORE MISSION",
                        color = NeonBlue,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    TextField(
                        value = inputTask,
                        onValueChange = { viewModel.setAgentInputTask(it) },
                        placeholder = {
                            Text(
                                "Define deep cognitive mission...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .testTag("agent_input_field"),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Suggestion pills
                    val presets = when (agent.agentId) {
                        "agent_web_search" -> listOf("Scrape autonomous blockchain structures of 2026", "Study Quantum Computing memory caches")
                        "agent_coder" -> listOf("Optimize multi-file Gradle compilation system", "Review Android architectural state bugs")
                        else -> listOf("Benchmark deep-seek-r1 vs gpt-4o benchmarks", "Orchestrate agent synergy data pools")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.forEach { text ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setAgentInputTask(text) }
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = text,
                                    color = Color.LightGray,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlowButton(
                        text = "DEPLOY AUTONOMOUS INSTANTIATOR",
                        onClick = { viewModel.triggerAgentAutomation() },
                        glowColor = NeonPurple,
                        enabled = inputTask.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("deploy_agent_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Holographic Logger Terminal
            Text(
                text = "NEURO-LOGGER TELEMETRY COMPILER",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF020205))
                    .border(1.dp, NeonBlue.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text(
                            text = "PORT TERMINAL IO SYNC ACTIVE - ADAPTER 1A OK",
                            color = GlowGreen,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Text(
                            text = agent.logs,
                            color = Color.Green,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
