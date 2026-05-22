package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ParticleBackground
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfinityViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: InfinityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        FuturisticBottomNavigation(viewModel)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        // Cyber custom canvas drawings floating behind all panels
                        ParticleBackground()

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top administrative dashboard header
                            CommandShellHeader()

                            // Display pane switching based on active workspace tab State
                            val activeTab by viewModel.activeTab.collectAsState()

                            Box(modifier = Modifier.weight(1f)) {
                                AnimatedContent(
                                    targetState = activeTab,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                    },
                                    label = "active_tab_nav"
                                ) { tab ->
                                    when (tab) {
                                        "CHATS" -> ChatsTab(viewModel)
                                        "GENERATORS" -> GeneratorsTab(viewModel)
                                        "AGENTS" -> AgentsTab(viewModel)
                                        "APIS" -> ApisTab(viewModel)
                                        "STORE" -> StoreTab()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// OS Navigation Command Terminal Header
// ==========================================
@Composable
fun CommandShellHeader() {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
            currentTime = formatter.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF050508))
            .border(width = 0.8.dp, color = Color.White.copy(alpha = 0.05f))
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SYSTEM PROTOCOL V4.0.2",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = NeonBlue,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "InfinityMind AI",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(NeonBlue, NeonPurple)
                    )
                )
            )
        }

        // Live Diagnostic System metrics
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live pulsing lights / bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(3.dp, 10.dp).clip(CircleShape).background(NeonBlue))
                Box(modifier = Modifier.size(3.dp, 10.dp).clip(CircleShape).background(NeonBlue))
                Box(modifier = Modifier.size(3.dp, 10.dp).clip(CircleShape).background(NeonBlue.copy(alpha = 0.25f)))
            }

            // Diagnostic core tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE: $currentTime",
                    color = NeonBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// Glassmorphic Interactive OS Toolbar (Bottom)
// ==========================================
@Composable
fun FuturisticBottomNavigation(viewModel: InfinityViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Respect Android Navigation bar safe boundaries!
            .background(Color(0xFF030306).copy(alpha = 0.95f))
            .border(width = 0.8.dp, color = Color.White.copy(alpha = 0.08f))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            MenuNavItem("CHATS", Icons.Filled.BlurCircular, "Workspace"),
            MenuNavItem("GENERATORS", Icons.Filled.Brush, "Generators"),
            MenuNavItem("AGENTS", Icons.Filled.Superscript, "Agents"),
            MenuNavItem("APIS", Icons.Filled.Security, "APIs Core"),
            MenuNavItem("STORE", Icons.Filled.Storefront, "Market")
        )

        navItems.forEach { item ->
            val isSelected = activeTab == item.id
            val accentColor = if (item.id == "CHATS") NeonBlue else NeonPurple

            if (item.id == "AGENTS") {
                // Central highlighted action button style
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .offset(y = (-12).dp)
                        .clickable { viewModel.setActiveTab(item.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(NeonBlue, NeonPurple)
                                )
                            )
                            .padding(1.5.dp) // border thickness
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF050508)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.setActiveTab(item.id) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label.uppercase(),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Active status indicators glowing dot
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(12.dp, 2.5.dp)
                                .clip(RoundedCornerShape(1.2.dp))
                                .background(accentColor)
                        )
                    } else {
                        Box(modifier = Modifier.size(12.dp, 2.5.dp))
                    }
                }
            }
        }
    }
}

private data class MenuNavItem(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
