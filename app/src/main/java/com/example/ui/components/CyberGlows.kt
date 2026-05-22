package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.random.Random

// ==========================================
// Geometric & Ambient Glow Background Effect
// ==========================================
@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    
    // Slow pulsation for background glow circles
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val particles = remember {
        List(12) {
            MutableParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.0006f,
                vy = (Random.nextFloat() - 0.5f) * 0.0006f,
                radius = Random.nextFloat() * 4f + 2f
            )
        }
    }

    // Drive the particle position updates smoothly coordinate with hardware refresh rates using withFrameMillis.
    // Reading this state inside the Canvas scope will only trigger redrawing and NOT recompose the parent composable function!
    var frameTime by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                frameTime = time
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy

                    if (p.x < 0f || p.x > 1f) p.vx = -p.vx
                    if (p.y < 0f || p.y > 1f) p.vy = -p.vy
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        // Redraw is triggered purely within the Canvas phase when frameTime updates
        val t = frameTime

        // 1. Top-Right Radial Glow (Blue)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonBlue.copy(alpha = 0.18f * glowPulse), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.1f),
                radius = size.width * 0.65f
            ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.9f, size.height * 0.1f)
        )

        // 2. Bottom-Left Radial Glow (Purple)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonPurple.copy(alpha = 0.18f * glowPulse), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.9f),
                radius = size.width * 0.65f
            ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.1f, size.height * 0.9f)
        )

        // 3. Ultra subtle background technical grid lines (Geometric layout)
        val gridStep = 80f
        val cols = (size.width / gridStep).toInt()
        val rows = (size.height / gridStep).toInt()
        
        for (i in 0..cols) {
            val lx = i * gridStep
            drawLine(
                color = Color.White.copy(alpha = 0.015f),
                start = Offset(lx, 0f),
                end = Offset(lx, size.height),
                strokeWidth = 1f
            )
        }
        for (i in 0..rows) {
            val ly = i * gridStep
            drawLine(
                color = Color.White.copy(alpha = 0.015f),
                start = Offset(0f, ly),
                end = Offset(size.width, ly),
                strokeWidth = 1f
            )
        }

        // 4. Draw gentle floating particles
        particles.forEach { p ->
            val px = p.x * size.width
            val py = p.y * size.height

            drawCircle(
                color = if (p.radius > 4) NeonBlue.copy(alpha = 0.15f) else NeonPurple.copy(alpha = 0.15f),
                radius = p.radius * 2.5f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = p.radius,
                center = Offset(px, py)
            )
        }
    }
}

private class MutableParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float
)

// ==========================================
// Geometric Glassmorphic Card (Rounded 3xl / 24dp)
// ==========================================
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonBlue,
    glowWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DarkGlass)
            .border(
                width = glowWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GeometricBorder,
                        borderColor.copy(alpha = pulseAlpha * 0.35f),
                        NeonPurple.copy(alpha = pulseAlpha * 0.15f),
                        GeometricBorder
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}

// ==========================================
// Command Bar Title Headers
// ==========================================
@Composable
fun HologramHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val cursorBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (cursorBlink > 0.5f) {
                    Box(
                        modifier = Modifier
                            .size(8.dp, 16.dp)
                            .background(NeonBlue)
                    )
                }
            }
            Text(
                text = subtitle,
                color = NeonBlue.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
        
        // Dynamic protocol status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF0C1D15), RoundedCornerShape(8.dp))
                .border(1.dp, GlowGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GlowGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SYS: OK",
                color = GlowGreen,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// Scan-line Geometric Terminal Box
// ==========================================
@Composable
fun GlowingTerminal(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    // Beautiful glassy dark slate background with rounded-2xl corner properties
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF09090F))
            .border(1.dp, GeometricBorder, RoundedCornerShape(16.dp))
            .drawBehind {
                drawRect(
                    color = Color.White.copy(alpha = 0.005f)
                )
            }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = verticalArrangement) {
            content()
        }
    }
}

// ==========================================
// Segmental Neon Progress Bar (Rounded-Full)
// ==========================================
@Composable
fun CyberProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SYSTEM ENGINE CONTEXT ALLOCATION",
                color = NeonBlue.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = NeonBlue,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF090912), CircleShape)
                .border(1.dp, GeometricBorder, CircleShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonBlue, NeonPurple)
                        )
                    )
            )
        }
    }
}

// ==========================================
// Modern Geometrical Button Control
// ==========================================
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonBlue,
    enabled: Boolean = true
) {
    val isPrimary = glowColor == NeonBlue
    
    val containerBg = if (!enabled) {
        Color.White.copy(alpha = 0.05f)
    } else if (isPrimary) {
        Color.White // High-contrast crisp white button as in the HTML Spec
    } else {
        glowColor.copy(alpha = 0.12f)
    }

    val textColor = if (!enabled) {
        Color.Gray
    } else if (isPrimary) {
        Color.Black // Black text on white background
    } else {
        Color.White
    }

    val borderStroke = if (!enabled) {
        Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
    } else if (isPrimary) {
        Modifier // no border needed for solid white primary button
    } else {
        Modifier.border(1.2.dp, glowColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .then(borderStroke)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}
