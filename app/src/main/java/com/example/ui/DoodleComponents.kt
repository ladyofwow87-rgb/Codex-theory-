package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.util.UUID
import kotlin.math.*
import kotlin.random.Random

// --- Mood Palette Mapping ---

data class MoodTheme(
    val mainBackground: Color,
    val cardBackground: Color,
    val primaryBrush: Color,
    val secondaryAccent: Color,
    val gradientBrush: Brush,
    val description: String
)

object MoodPalette {
    fun getTheme(mood: String, isDark: Boolean = true): MoodTheme {
        return when (mood) {
            "Calm" -> {
                val p = Color(0xFF6B8A7A) // Sage
                val s = Color(0xFFA6B1E1) // Soft Lavender
                MoodTheme(
                    mainBackground = Color(0xFF1E2522),
                    cardBackground = Color(0xFF242F2A),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.verticalGradient(listOf(Color(0xFF242F2A), Color(0xFF1A221E))),
                    description = "Soothing sage and lavender tones with ambient, gentle breeze visualizers."
                )
            }
            "Silly" -> {
                val p = Color(0xFFFF5722) // Coral Core
                val s = Color(0xFFFFEB3B) // Sunshine
                MoodTheme(
                    mainBackground = Color(0xFF2A1F1D),
                    cardBackground = Color(0xFF382622),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.radialGradient(listOf(Color(0xFF422F2A), Color(0xFF1E1715))),
                    description = "Playful primary corals and yellows. Brushes wobble and bounce."
                )
            }
            "Epic" -> {
                val p = Color(0xFFFF9800) // Lava Orange
                val s = Color(0xFFFF5252) // Volcano Red
                MoodTheme(
                    mainBackground = Color(0xFF1C1310),
                    cardBackground = Color(0xFF2B1A14),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.linearGradient(listOf(Color(0xFF381F17), Color(0xFF100A08))),
                    description = "Fierce volcanic crimsons and charcoal shadows. Bold cinematic vibe."
                )
            }
            "Mysterious" -> {
                val p = Color(0xFF9C27B0) // Violet
                val s = Color(0xFF00E5FF) // Hologram Cyan
                MoodTheme(
                    mainBackground = Color(0xFF17111C),
                    cardBackground = Color(0xFF251A30),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.verticalGradient(listOf(Color(0xFF281938), Color(0xFF0F0617))),
                    description = "Glowing iridescent violets and cyans. Hidden highlights appear."
                )
            }
            "Funny" -> {
                val p = Color(0xFF4CAF50) // Electric Lime
                val s = Color(0xFFFF4081) // Hot Pink
                MoodTheme(
                    mainBackground = Color(0xFF162518),
                    cardBackground = Color(0xFF1F3622),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.linearGradient(listOf(Color(0xFF233E27), Color(0xFF0E1A11))),
                    description = "High-energy lime and hot bubblegum pink gradients for pure cartoon laughter."
                )
            }
            "Adventurous" -> {
                val p = Color(0xFF8D6E63) // Leather Brown
                val s = Color(0xFFFFD54F) // Gold Treasure
                MoodTheme(
                    mainBackground = Color(0xFF241E1C),
                    cardBackground = Color(0xFF312825),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.verticalGradient(listOf(Color(0xFF362B28), Color(0xFF1E1816))),
                    description = "Cozy leather browns and compass golds. Ideal for wild maps and quests."
                )
            }
            "Weird" -> {
                val p = Color(0xFFEEFF41) // Lime Yellow Toxic
                val s = Color(0xFFD500F9) // Toxic Purple
                MoodTheme(
                    mainBackground = Color(0xFF0C0F0E),
                    cardBackground = Color(0xFF1A1A24),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.radialGradient(listOf(Color(0xFF142417), Color(0xFF050508))),
                    description = "Acid lime and magenta glowing glitches. Brushes behave unexpectedly."
                )
            }
            "Spooky" -> {
                val p = Color(0xFFB388FF) // Ghastly Purple
                val s = Color(0xFFE65100) // Pumpkin Amber
                MoodTheme(
                    mainBackground = Color(0xFF0E0B12),
                    cardBackground = Color(0xFF1A1521),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.verticalGradient(listOf(Color(0xFF1D1429), Color(0xFF060408))),
                    description = "Eerie twilight plum and pumpkin orange. Shadows stretch with cold mist."
                )
            }
            "Cozy" -> {
                val p = Color(0xFFD7CCC8) // Soft Vanilla
                val s = Color(0xFFFF7043) // Hearth Amber
                MoodTheme(
                    mainBackground = Color(0xFF211D1C),
                    cardBackground = Color(0xFF332A27),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.linearGradient(listOf(Color(0xFF3B2E2A), Color(0xFF1F1A19))),
                    description = "Warm hazelnut coffee and soft chimney warmth. Calm, fireside sketch loops."
                )
            }
            else -> {
                val p = Color(0xFFD0BCFF)
                val s = Color(0xFFCCC2DC)
                MoodTheme(
                    mainBackground = Color(0xFF121212),
                    cardBackground = Color(0xFF1E1E1E),
                    primaryBrush = p,
                    secondaryAccent = s,
                    gradientBrush = Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF121212))),
                    description = "Standard slate tones."
                )
            }
        }
    }
}

// --- App Mascot Cover Art Component ---
@Composable
fun DetectiveDuckCoverArt(
    modifier: Modifier = Modifier,
    drawTitle: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFE5800E).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F111A))
                    .border(2.dp, Color(0xFF242730), RoundedCornerShape(18.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // 1. Rainy Cobblestone alleyway vignette backdrop with soft bokeh circles
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF283655), Color(0xFF0F111A)),
                            center = Offset(w / 2f, h / 2f),
                            radius = w * 0.95f
                        )
                    )
                    
                    // Soft warm bokeh depth-of-field ambient lighting triggers
                    drawCircle(
                        color = Color(0x18FF9800),
                        radius = w * 0.22f,
                        center = Offset(w * 0.25f, h * 0.3f)
                    )
                    drawCircle(
                        color = Color(0x12FFD54F),
                        radius = w * 0.15f,
                        center = Offset(w * 0.78f, h * 0.22f)
                    )
                    drawCircle(
                        color = Color(0x1E8EA2D2),
                        radius = w * 0.18f,
                        center = Offset(w * 0.82f, h * 0.58f)
                    )
                    drawCircle(
                        color = Color(0x10FFFFFF),
                        radius = w * 0.1f,
                        center = Offset(w * 0.38f, h * 0.65f)
                    )
                    
                    // Gentle glowing rain drizzle overlay (diagonal soft strands)
                    for (i in 0..12) {
                        val rx = (i * 0.08f) * w
                        val ry = ((i * 7) % 10) * 0.1f * h
                        drawLine(
                            color = Color(0x1C8EA2D2),
                            start = Offset(rx, ry),
                            end = Offset(rx - 8f, ry + 22f),
                            strokeWidth = 2f
                        )
                    }
                    
                    // Wet cobblestone path at the bottom
                    val cobbleColor = Color(0x288EA2D2)
                    drawRoundRect(
                        color = cobbleColor,
                        topLeft = Offset(w * 0.1f, h * 0.72f),
                        size = Size(w * 0.28f, h * 0.08f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = cobbleColor,
                        topLeft = Offset(w * 0.46f, h * 0.78f),
                        size = Size(w * 0.42f, h * 0.08f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = cobbleColor,
                        topLeft = Offset(w * 0.22f, h * 0.86f),
                        size = Size(w * 0.55f, h * 0.08f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    
                    // 2. Soft creamy white feathered neck & chest
                    val baseCream = Color(0xFFFFFFFF)
                    val shadowCream = Color(0xFFEFECE0)
                    val billOrange = Color(0xFFFF9F1C)
                    val hatBrown = Color(0xFF7E5B3D)
                    val hatPlaidDark = Color(0xFF4A311D)
                    val hatPlaidLight = Color(0xFFA67D5B)
                    
                    // Body / Chest oval base
                    drawCircle(
                        color = shadowCream,
                        radius = w * 0.36f,
                        center = Offset(w * 0.5f, h * 0.92f)
                    )
                    drawCircle(
                        color = baseCream,
                        radius = w * 0.33f,
                        center = Offset(w * 0.5f, h * 0.95f)
                    )
                    
                    // Soft neck transition
                    drawRoundRect(
                        color = shadowCream,
                        topLeft = Offset(w * 0.37f, h * 0.56f),
                        size = Size(w * 0.26f, h * 0.32f),
                        cornerRadius = CornerRadius(35f, 35f)
                    )
                    drawRoundRect(
                        color = baseCream,
                        topLeft = Offset(w * 0.4f, h * 0.56f),
                        size = Size(w * 0.2f, h * 0.32f),
                        cornerRadius = CornerRadius(35f, 35f)
                    )
                    
                    // Extra fluffy cheek/chest feather tufted lines (adding rich textures)
                    val fluffColor = Color(0xFFECEBE3)
                    for (i in 0..7) {
                        val fx = w * (0.39f + (i * 0.03f))
                        val fy = h * (0.58f + ((i * 3) % 4) * 0.02f)
                        drawArc(
                            color = fluffColor,
                            startAngle = 135f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(fx, fy),
                            size = Size(14f, 8f),
                            style = Stroke(width = 2f)
                        )
                    }

                    // 3. Fluffy Head
                    val headCenter = Offset(w * 0.52f, h * 0.43f)
                    val headRadius = w * 0.24f
                    drawCircle(
                        color = shadowCream,
                        radius = headRadius,
                        center = headCenter
                    )
                    drawCircle(
                        color = baseCream,
                        radius = headRadius * 0.96f,
                        center = Offset(w * 0.51f, h * 0.42f)
                    )
                    
                    // 4. Detailed Beak/Bill pointing left, custom curved profile drawing
                    val beakPath = Path().apply {
                        moveTo(w * 0.34f, h * 0.43f)
                        quadraticTo(w * 0.18f, h * 0.41f, w * 0.14f, h * 0.46f)
                        quadraticTo(w * 0.2f, h * 0.56f, w * 0.38f, h * 0.53f)
                        quadraticTo(w * 0.48f, h * 0.52f, w * 0.5f, h * 0.46f)
                        close()
                    }
                    drawPath(path = beakPath, color = Color(0xFFE5800E))
                    
                    val upperBeakPath = Path().apply {
                        moveTo(w * 0.36f, h * 0.43f)
                        quadraticTo(w * 0.2f, h * 0.42f, w * 0.15f, h * 0.45f)
                        quadraticTo(w * 0.22f, h * 0.53f, w * 0.38f, h * 0.51f)
                        quadraticTo(w * 0.46f, h * 0.5f, w * 0.48f, h * 0.46f)
                        close()
                    }
                    drawPath(path = upperBeakPath, color = billOrange)
                    
                    // Nostril
                    drawCircle(
                        color = Color(0x88331100),
                        radius = w * 0.012f,
                        center = Offset(w * 0.27f, h * 0.46f)
                    )
                    
                    // 5. Intelligent Eye with gold star reflection (matches user image perfectly)
                    val eyeCenter = Offset(w * 0.56f, h * 0.36f)
                    val eyeRadius = w * 0.052f
                    drawCircle(
                        color = Color(0xFF141210),
                        radius = eyeRadius,
                        center = eyeCenter
                    )
                    
                    // Golden star catchlight reflection 🌟
                    val starCenter = Offset(eyeCenter.x - 3f, eyeCenter.y - 3f)
                    val starPath = Path().apply {
                        val numPoints = 5
                        val innerRadius = 3f
                        val outerRadius = 7.5f
                        var angle = -Math.PI / 2
                        val increment = Math.PI / numPoints
                        for (i in 0 until numPoints * 2) {
                            val r = if (i % 2 == 0) outerRadius else innerRadius
                            val x = (starCenter.x + cos(angle) * r).toFloat()
                            val y = (starCenter.y + sin(angle) * r).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                            angle += increment
                        }
                        close()
                    }
                    drawPath(path = starPath, color = Color(0xFFFFD54F))
                    
                    // tiny soft secondary catchlight
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = Offset(eyeCenter.x + 6f, eyeCenter.y + 6f)
                    )

                    // 6. Plaid/Checkered Sherlock Holmes Hat (With Dual-Diagonal Tweed Herringbone Texture)
                    val hatPath = Path().apply {
                        moveTo(w * 0.24f, h * 0.31f)
                        quadraticTo(w * 0.28f, h * 0.13f, w * 0.52f, h * 0.13f)
                        quadraticTo(w * 0.76f, h * 0.13f, w * 0.8f, h * 0.31f)
                        close()
                    }
                    
                    clipPath(path = hatPath) {
                        drawPath(path = hatPath, color = hatBrown)
                        
                        // Dual diagonal tweed hatching thread matrix lines for tweed weave texture
                        for (i in 0..40) {
                            val lineX = (i * 0.022f) * w
                            drawLine(
                                color = Color(0x351F1004),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX + w * 0.12f, h),
                                strokeWidth = 1.5f
                            )
                            drawLine(
                                color = Color(0x351F1004),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX - w * 0.12f, h),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                    
                    // Plaid cross bands & grids
                    drawPath(
                        path = Path().apply {
                            moveTo(w * 0.25f, h * 0.24f)
                            quadraticTo(w * 0.52f, h * 0.23f, w * 0.79f, h * 0.24f)
                            lineTo(w * 0.78f, h * 0.26f)
                            quadraticTo(w * 0.52f, h * 0.25f, w * 0.26f, h * 0.26f)
                            close()
                        },
                        color = hatPlaidDark
                    )
                    drawRect(
                        color = hatPlaidDark,
                        topLeft = Offset(w * 0.5f, h * 0.13f),
                        size = Size(10f, h * 0.18f)
                    )
                    drawRect(
                        color = hatPlaidLight,
                        topLeft = Offset(w * 0.38f, h * 0.15f),
                        size = Size(5f, h * 0.16f)
                    )
                    drawRect(
                        color = hatPlaidLight,
                        topLeft = Offset(w * 0.64f, h * 0.15f),
                        size = Size(5f, h * 0.16f)
                    )

                    // Peak / Visors with clipping thread overlays
                    val frontVisor = Path().apply {
                        moveTo(w * 0.25f, h * 0.31f)
                        quadraticTo(w * 0.12f, h * 0.31f, w * 0.14f, h * 0.37f)
                        quadraticTo(w * 0.3f, h * 0.37f, w * 0.35f, h * 0.33f)
                        close()
                    }
                    clipPath(path = frontVisor) {
                        drawPath(path = frontVisor, color = hatPlaidDark)
                        for (i in 0..20) {
                            val lineX = (i * 0.05f) * w
                            drawLine(
                                color = Color(0x3E000000),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX + w * 0.1f, h),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                    
                    val backVisor = Path().apply {
                        moveTo(w * 0.75f, h * 0.31f)
                        quadraticTo(w * 0.88f, h * 0.31f, w * 0.86f, h * 0.37f)
                        quadraticTo(w * 0.7f, h * 0.37f, w * 0.65f, h * 0.33f)
                        close()
                    }
                    clipPath(path = backVisor) {
                        drawPath(path = backVisor, color = hatPlaidDark)
                        for (i in 0..20) {
                            val lineX = (i * 0.05f) * w
                            drawLine(
                                color = Color(0x3E000000),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX - w * 0.1f, h),
                                strokeWidth = 1.5f
                            )
                        }
                    }
                    
                    // Top Ribbon Knot and Bow Loops
                    val knotCenter = Offset(w * 0.52f, h * 0.13f)
                    drawCircle(color = hatPlaidLight, radius = w * 0.035f, center = knotCenter)
                    
                    val lBow = Path().apply {
                        moveTo(knotCenter.x, knotCenter.y)
                        quadraticTo(knotCenter.x - 18f, knotCenter.y - 12f, knotCenter.x - 16f, knotCenter.y - 4f)
                        close()
                    }
                    drawPath(path = lBow, color = hatPlaidDark)
                    
                    val rBow = Path().apply {
                        moveTo(knotCenter.x, knotCenter.y)
                        quadraticTo(knotCenter.x + 18f, knotCenter.y - 12f, knotCenter.x + 16f, knotCenter.y - 4f)
                        close()
                    }
                    drawPath(path = rBow, color = hatPlaidDark)

                    // 7. Plaid Bow Tie on neck (with cute fluffy rounded pillows and weaving herringbone details!)
                    val tieCenter = Offset(w * 0.51f, h * 0.69f)
                    val tieKnotRadius = w * 0.035f
                    
                    val lLoop = Path().apply {
                        moveTo(tieCenter.x, tieCenter.y)
                        cubicTo(
                            tieCenter.x - w * 0.08f, tieCenter.y - h * 0.05f,
                            tieCenter.x - w * 0.16f, tieCenter.y - h * 0.05f,
                            tieCenter.x - w * 0.16f, tieCenter.y
                        )
                        cubicTo(
                            tieCenter.x - w * 0.16f, tieCenter.y + h * 0.05f,
                            tieCenter.x - w * 0.08f, tieCenter.y + h * 0.05f,
                            tieCenter.x, tieCenter.y
                        )
                        close()
                    }
                    
                    clipPath(path = lLoop) {
                        drawPath(path = lLoop, color = hatBrown)
                        // Diagonal thread weaving for textured bowtie look
                        for (i in 0..15) {
                            val lineX = tieCenter.x - (i * 0.015f) * w
                            drawLine(color = Color(0x451F1004), start = Offset(lineX, 0f), end = Offset(lineX + w * 0.05f, h), strokeWidth = 1f)
                            drawLine(color = Color(0x451F1004), start = Offset(lineX, 0f), end = Offset(lineX - w * 0.05f, h), strokeWidth = 1f)
                        }
                    }
                    drawPath(path = lLoop, color = hatPlaidLight, style = Stroke(width = 2f))
                    
                    val rLoop = Path().apply {
                        moveTo(tieCenter.x, tieCenter.y)
                        cubicTo(
                            tieCenter.x + w * 0.08f, tieCenter.y - h * 0.05f,
                            tieCenter.x + w * 0.16f, tieCenter.y - h * 0.05f,
                            tieCenter.x + w * 0.16f, tieCenter.y
                        )
                        cubicTo(
                            tieCenter.x + w * 0.16f, tieCenter.y + h * 0.05f,
                            tieCenter.x + w * 0.08f, tieCenter.y + h * 0.05f,
                            tieCenter.x, tieCenter.y
                        )
                        close()
                    }
                    
                    clipPath(path = rLoop) {
                        drawPath(path = rLoop, color = hatBrown)
                        // Diagonal thread weaving for textured bowtie look
                        for (i in 0..15) {
                            val lineX = tieCenter.x + (i * 0.015f) * w
                            drawLine(color = Color(0x451F1004), start = Offset(lineX, 0f), end = Offset(lineX + w * 0.05f, h), strokeWidth = 1f)
                            drawLine(color = Color(0x451F1004), start = Offset(lineX, 0f), end = Offset(lineX - w * 0.05f, h), strokeWidth = 1f)
                        }
                    }
                    drawPath(path = rLoop, color = hatPlaidLight, style = Stroke(width = 2f))
                    
                    drawCircle(color = hatPlaidDark, radius = tieKnotRadius, center = tieCenter)
                    drawCircle(color = hatPlaidLight, radius = tieKnotRadius - 3f, center = tieCenter, style = Stroke(width = 1.5f))
                }
            }
            
            if (drawTitle) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🦆 Professor Quackers, Master Detective",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Official App Mascot & AI Sketch Assistant",
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// --- Procedural High-Fidelity Finder-Pattern QR Code ---
@Composable
fun MockQRCodeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Solid clean white background
        drawRect(color = Color.White)
        
        // Finder patterns
        val pSize = w * 0.24f
        drawRect(color = Color.Black, topLeft = Offset(0f, 0f), size = Size(pSize, pSize))
        drawRect(color = Color.White, topLeft = Offset(w * 0.035f, h * 0.035f), size = Size(pSize - w * 0.07f, pSize - h * 0.07f))
        drawRect(color = Color.Black, topLeft = Offset(w * 0.07f, h * 0.07f), size = Size(pSize - w * 0.14f, pSize - h * 0.14f))

        drawRect(color = Color.Black, topLeft = Offset(w - pSize, 0f), size = Size(pSize, pSize))
        drawRect(color = Color.White, topLeft = Offset(w - pSize + w * 0.035f, h * 0.035f), size = Size(pSize - w * 0.07f, pSize - h * 0.07f))
        drawRect(color = Color.Black, topLeft = Offset(w - pSize + w * 0.07f, h * 0.07f), size = Size(pSize - w * 0.14f, pSize - h * 0.14f))

        drawRect(color = Color.Black, topLeft = Offset(0f, h - pSize), size = Size(pSize, pSize))
        drawRect(color = Color.White, topLeft = Offset(w * 0.035f, h - pSize + h * 0.035f), size = Size(pSize - w * 0.07f, pSize - h * 0.07f))
        drawRect(color = Color.Black, topLeft = Offset(w * 0.07f, h - pSize + h * 0.07f), size = Size(pSize - w * 0.14f, pSize - h * 0.14f))

        val rand = java.util.Random(456)
        val gridSize = 14
        val cellW = w / gridSize
        val cellH = h / gridSize
        
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (row < 4 && col < 4) continue
                if (row < 4 && col >= gridSize - 4) continue
                if (row >= gridSize - 4 && col < 4) continue
                
                if (row >= gridSize - 4 && row < gridSize - 1 && col >= gridSize - 4 && col < gridSize - 1) {
                    val isBorder = row == gridSize - 4 || row == gridSize - 2 || col == gridSize - 4 || col == gridSize - 2
                    if (isBorder || (row == gridSize - 3 && col == gridSize - 3)) {
                        drawRect(color = Color.Black, topLeft = Offset(col * cellW, row * cellH), size = Size(cellW + 0.3f, cellH + 0.3f))
                    } else {
                        drawRect(color = Color.White, topLeft = Offset(col * cellW, row * cellH), size = Size(cellW, cellH))
                    }
                    continue
                }
                
                if (rand.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * cellW, row * cellH),
                        size = Size(cellW + 0.3f, cellH + 0.3f)
                    )
                }
            }
        }
    }
}

// --- Procedural Dino Outfitter Component ---

@Composable
fun DinosaursOutfitCard(
    hat: DinosaurAccessory?,
    glasses: DinosaurAccessory?,
    mustache: DinosaurAccessory?,
    outfit: DinosaurAccessory?,
    dailyTip: DrawingTip,
    onReaction: (Boolean) -> Unit, // True = Loved, False = Dislike
    modifier: Modifier = Modifier,
    dinoVibe: String = "Classic Polka-Dinosaur 🦖",
    isHyperRealismEnabled: Boolean = false,
    equippedOutfit: String? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242730)),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF4A4E5D), RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🦕 Dr. Polka-Dot ($dinoVibe)",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = "Visual Engine: ${if (isHyperRealismEnabled) "✨ Hyper-realism shader enabled" else "Flat Vector"}",
                color = if (isHyperRealismEnabled) Color(0xFF00E676) else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Procedural Drawing Box
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF15181F))
                        .border(1.dp, Color(0xFF34384A), RoundedCornerShape(18.dp))
                ) {
                    // Base Dino picture
                    Image(
                        painter = painterResource(id = R.drawable.img_dino_base),
                        contentDescription = "Hyper-realistic base dino with polka dots",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isHyperRealismEnabled) {
                        // Cinematic lighting & rayleigh scattering overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xDDFFB74D), // warm gold bloom
                                        Color(0x55E91E63), // sunset neon hallow magenta
                                        Color(0xAA0B0D13)  // dark vignette
                                    ),
                                    center = Offset(size.width, 0f),
                                    radius = size.width * 1.4f
                                ),
                                alpha = 0.75f
                            )

                            // Volumetric ambient gold particulate nodes
                            val sparkPoints = listOf(
                                Offset(20f, 30f), Offset(140f, 50f), Offset(40f, 110f),
                                Offset(150f, 120f), Offset(80f, 135f), Offset(100f, 25f),
                                Offset(50f, 80f), Offset(115f, 95f), Offset(25f, 65f)
                            )
                            sparkPoints.forEachIndexed { idx, point ->
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White, Color(0x00FFFFFF)),
                                        center = point,
                                        radius = 6f + (idx % 3) * 5f
                                    ),
                                    radius = 6f + (idx % 3) * 5f,
                                    center = point,
                                    alpha = 0.65f
                                )
                            }
                        }
                    }

                    // Draw accessories dynamically layered on top of the image
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // 1. OUTFIT ( Waistcoat, bow tie )
                        outfit?.let {
                            val paint = Paint().apply {
                                color = it.color
                                style = PaintingStyle.Fill
                            }
                            // Custom bow-tie or neck ribbon at cy + 18
                            val drawCy = cy + 25f
                            when (it.shapeType) {
                                0 -> { // Bowtie
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 30f, drawCy - 15f)
                                            lineTo(cx - 5f, drawCy)
                                            lineTo(cx - 30f, drawCy + 15f)
                                            close()
                                        },
                                        color = it.color
                                    )
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx + 30f, drawCy - 15f)
                                            lineTo(cx + 5f, drawCy)
                                            lineTo(cx + 30f, drawCy + 15f)
                                            close()
                                        },
                                        color = it.color
                                    )
                                    drawCircle(color = Color.White, radius = 5f, center = Offset(cx, drawCy))
                                }
                                1 -> { // Star dapper waistcoat collar
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 40f, drawCy - 5f)
                                            lineTo(cx + 40f, drawCy - 5f)
                                            lineTo(cx + 30f, drawCy + 40f)
                                            lineTo(cx - 30f, drawCy + 40f)
                                            close()
                                        },
                                        color = it.color
                                    )
                                    // Yellow gold collar dots
                                    drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx - 15f, drawCy + 15f))
                                    drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx + 15f, drawCy + 15f))
                                }
                                2 -> { // Ribbon/Dapper Scarf
                                    drawRect(
                                        color = it.color,
                                        topLeft = Offset(cx - 35f, drawCy - 10f),
                                        size = Size(70f, 15f)
                                    )
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 20f, drawCy + 5f)
                                            lineTo(cx - 30f, drawCy + 45f)
                                            lineTo(cx - 10f, drawCy + 45f)
                                            close()
                                        },
                                        color = it.color
                                    )
                                }
                                else -> { // Cape vest
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 45f, drawCy)
                                            lineTo(cx + 45f, drawCy)
                                            lineTo(cx + 20f, drawCy + 50f)
                                            lineTo(cx - 20f, drawCy + 50f)
                                            close()
                                        },
                                        color = it.color
                                    )
                                }
                            }
                        }

                        // 2. MUSTACHE
                        mustache?.let {
                            val mY = cy - 5f
                            // Drawn as a handlebar curling path
                            val mustachePath = Path().apply {
                                moveTo(cx, mY)
                                cubicTo(cx - 20f, mY - 15f, cx - 40f, mY - 5f, cx - 45f, mY + 5f)
                                cubicTo(cx - 30f, mY + 15f, cx - 10f, mY, cx, mY)
                                cubicTo(cx + 10f, mY, cx + 30f, mY + 15f, cx + 45f, mY + 5f)
                                cubicTo(cx + 40f, mY - 5f, cx + 20f, mY - 15f, cx, mY)
                            }
                            drawPath(mustachePath, color = Color(0xFF2C1E1B))
                        }

                        // 3. GLASSES
                        glasses?.let {
                            val gY = cy - 25f
                            val glassColor = it.color
                            val widthScale = it.scale
                            when (it.shapeType) {
                                0 -> { // Circle/Professor specs
                                    val r = 18f * widthScale
                                    drawCircle(
                                        color = glassColor,
                                        radius = r,
                                        center = Offset(cx - 22f, gY),
                                        style = Stroke(width = 3.5f)
                                    )
                                    drawCircle(
                                        color = glassColor,
                                        radius = r,
                                        center = Offset(cx + 22f, gY),
                                        style = Stroke(width = 3.5f)
                                    )
                                    drawLine(
                                        color = glassColor,
                                        start = Offset(cx - 4f, gY),
                                        end = Offset(cx + 4f, gY),
                                        strokeWidth = 3f
                                    )
                                }
                                1 -> { // Aviators
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 40f, gY - 10f)
                                            lineTo(cx - 10f, gY - 10f)
                                            lineTo(cx - 15f, gY + 15f)
                                            lineTo(cx - 35f, gY + 15f)
                                            close()
                                        },
                                        color = glassColor,
                                        style = Stroke(width = 4f)
                                    )
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx + 10f, gY - 10f)
                                            lineTo(cx + 40f, gY - 10f)
                                            lineTo(cx + 35f, gY + 15f)
                                            lineTo(cx + 15f, gY + 15f)
                                            close()
                                        },
                                        color = glassColor,
                                        style = Stroke(width = 4f)
                                    )
                                    drawLine(
                                        color = glassColor,
                                        start = Offset(cx - 10f, gY - 5f),
                                        end = Offset(cx + 10f, gY - 5f),
                                        strokeWidth = 4f
                                    )
                                }
                                else -> { // Star specs!
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 20f, gY - 20f)
                                            lineTo(cx - 10f, gY + 10f)
                                            lineTo(cx - 35f, gY - 5f)
                                            lineTo(cx - 5f, gY - 5f)
                                            lineTo(cx - 30f, gY + 10f)
                                            close()
                                        },
                                        color = glassColor,
                                        style = Stroke(width = 3f)
                                    )
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx + 20f, gY - 20f)
                                            lineTo(cx + 10f, gY + 10f)
                                            lineTo(cx + 35f, gY - 5f)
                                            lineTo(cx + 5f, gY - 5f)
                                            lineTo(cx + 30f, gY + 10f)
                                            close()
                                        },
                                        color = glassColor,
                                        style = Stroke(width = 3f)
                                    )
                                }
                            }
                        }

                        // 4. HAT
                        hat?.let {
                            val hY = cy - 50f
                            val hatColor = it.color
                            when (it.shapeType) {
                                0 -> { // Top Hat / Magician
                                    drawRect(
                                        color = hatColor,
                                        topLeft = Offset(cx - 25f, hY - 45f),
                                        size = Size(50f, 45f)
                                    )
                                    drawRoundRect(
                                        color = hatColor,
                                        topLeft = Offset(cx - 40f, hY - 5f),
                                        size = Size(80f, 10f),
                                        cornerRadius = CornerRadius(4f)
                                    )
                                    // Red hat ribbon
                                    drawRect(
                                        color = Color.Red,
                                        topLeft = Offset(cx - 25f, hY - 14f),
                                        size = Size(50f, 10f)
                                    )
                                }
                                1 -> { // Detective Bowler hat
                                    drawArc(
                                        color = hatColor,
                                        startAngle = 180f,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        topLeft = Offset(cx - 30f, hY - 32f),
                                        size = Size(60f, 60f)
                                    )
                                    drawRoundRect(
                                        color = hatColor,
                                        topLeft = Offset(cx - 45f, hY - 4f),
                                        size = Size(90f, 8f),
                                        cornerRadius = CornerRadius(4f)
                                    )
                                }
                                2 -> { // Crown of Gold
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 35f, hY)
                                            lineTo(cx - 35f, hY - 30f)
                                            lineTo(cx - 18f, hY - 10f)
                                            lineTo(cx, hY - 40f)
                                            lineTo(cx + 18f, hY - 10f)
                                            lineTo(cx + 35f, hY - 30f)
                                            lineTo(cx + 35f, hY)
                                            close()
                                        },
                                        color = Color(0xFFFFD54F) // Gold
                                    )
                                    drawCircle(color = Color.Red, radius = 4f, center = Offset(cx, hY - 40f))
                                    drawCircle(color = Color.Blue, radius = 3f, center = Offset(cx - 35f, hY - 30f))
                                    drawCircle(color = Color.Blue, radius = 3f, center = Offset(cx + 35f, hY - 30f))
                                }
                                else -> { // Wizard high cone hat
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 30f, hY)
                                            lineTo(cx, hY - 60f)
                                            lineTo(cx + 30f, hY)
                                            close()
                                        },
                                        color = hatColor
                                    )
                                    // Golden star points on wizard hat
                                    drawCircle(color = Color(0xFFFFD54F), radius = 3f, center = Offset(cx, hY - 35f))
                                    drawCircle(color = Color(0xFFFFD54F), radius = 2f, center = Offset(cx - 10f, hY - 15f))
                                    drawCircle(color = Color(0xFFFFD54F), radius = 2f, center = Offset(cx + 10f, hY - 15f))
                                }
                            }
                        }

                        // Layer Premium Shop Outfits directly on top of base elements!
                        equippedOutfit?.let { outfitName ->
                            val drawCy = cy + 25f
                            val hY = cy - 50f
                            when {
                                outfitName.contains("Detective") -> {
                                    // Deerstalker Hat
                                    drawArc(
                                        color = Color(0xFF6D4C41),
                                        startAngle = 180f,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        topLeft = Offset(cx - 32f, hY - 35f),
                                        size = Size(64f, 64f)
                                    )
                                    drawRoundRect(
                                        color = Color(0xFF5D4037),
                                        topLeft = Offset(cx - 42f, hY - 4f),
                                        size = Size(84f, 10f),
                                        cornerRadius = CornerRadius(4f)
                                    )
                                    // Trenchcoat collar
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 40f, drawCy)
                                            lineTo(cx + 40f, drawCy)
                                            lineTo(cx + 25f, drawCy + 55f)
                                            lineTo(cx - 25f, drawCy + 55f)
                                            close()
                                        },
                                        color = Color(0xFF6D4C41)
                                    )
                                    drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx - 15f, drawCy + 25f))
                                    drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(cx + 15f, drawCy + 25f))
                                }
                                outfitName.contains("Crown") -> {
                                    // Golden Spired Crown
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(cx - 30f, hY)
                                            lineTo(cx - 30f, hY - 28f)
                                            lineTo(cx - 15f, hY - 10f)
                                            lineTo(cx, hY - 38f)
                                            lineTo(cx + 15f, hY - 10f)
                                            lineTo(cx + 30f, hY - 28f)
                                            lineTo(cx + 30f, hY)
                                            close()
                                        },
                                        color = Color(0xFFFFD54F)
                                    )
                                    drawCircle(color = Color.Red, radius = 5f, center = Offset(cx, hY - 38f))
                                    drawCircle(color = Color.Blue, radius = 4.5f, center = Offset(cx - 30f, hY - 28f))
                                    drawCircle(color = Color.Blue, radius = 4.5f, center = Offset(cx + 30f, hY - 28f))
                                }
                                outfitName.contains("Astronaut") -> {
                                    // Astronaut Glass Bubble Helm
                                    drawCircle(
                                        color = Color(0x664FC3F7),
                                        radius = 62f,
                                        center = Offset(cx, cy - 10f)
                                    )
                                    drawCircle(
                                        color = Color(0xB3FFFFFF),
                                        radius = 62f,
                                        center = Offset(cx, cy - 10f),
                                        style = Stroke(width = 4f)
                                    )
                                }
                                outfitName.contains("Clown") -> {
                                    // Fluffy Silly Red Nose
                                    drawCircle(
                                        color = Color(0xFFE53935),
                                        radius = 14f,
                                        center = Offset(cx + 25f, cy + 5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Artist Lesson Tip Area
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFF2C303F),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "[${dailyTip.category}]\n${dailyTip.text}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onReaction(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("👍 Loved", color = Color.White, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onReaction(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("👎 Not for me", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- Soundscape Visualizer (Tape/Synth bouncing deck) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundscapeDeck(
    mode: String,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        "Rainy Sketchbook",
        "Campfire",
        "Space Journey",
        "Ocean Drift",
        "Wizard Library",
        "Retro Arcade",
        "Cyberpunk Disco",
        "Lofi Meadow",
        "Zen Bamboo Stream",
        "Cosmic Aurora"
    )
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D212E)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF343B52), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isPlaying) Color(0xFFFF9800) else Color(0xFF283149),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Playback Controller",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎹 Atmospheric Synth Simulator",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = mode,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248))
                ) {
                    Text("Soundscapes", fontSize = 12.sp)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Select Ambient Theme (Procedural Code Synth):",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = 3
                    ) {
                        modes.forEach { m ->
                            val isSelected = m == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF3B486B) else Color(0xFF242A3D),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {
                                        onSelect(m)
                                        expanded = false
                                    }
                            ) {
                                Text(
                                    text = m,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pulse Synth Waveform Simulation
            val waveCount = 14
            val infiniteTransition = rememberInfiniteTransition()
            val phaseOffsets = List(waveCount) { index ->
                infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + index * 80, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(Color(0xFF0F121C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until waveCount) {
                    val heightMultiplier = if (isPlaying) phaseOffsets[i].value else 0.15f
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(0.8f * heightMultiplier)
                            .background(
                                color = when (mode) {
                                    "Rainy Sketchbook" -> Color(0xFF03A9F4)
                                    "Campfire" -> Color(0xFFFF5722)
                                    "Space Journey" -> Color(0xFF9C27B0)
                                    "Ocean Drift" -> Color(0xFF00E5FF)
                                    "Wizard Library" -> Color(0xFFE040FB)
                                    else -> Color(0xFF00E676)
                                },
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

// --- Brushes Drawing Core Helper ---

fun drawStrokePath(drawScope: DrawScope, stroke: DrawingStroke) {
    if (stroke.points.size < 2) return

    val path = Path()
    path.moveTo(stroke.points.first().x, stroke.points.first().y)
    for (i in 1 until stroke.points.size) {
        val pt = stroke.points[i]
        path.lineTo(pt.x, pt.y)
    }

    val drawColor = if (stroke.isEraser) Color(0xFF1B1D26) else Color(stroke.color)

    // Customize Brush Rendering Style
    when (stroke.brushType) {
        "Pencil" -> {
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.5f),
                style = Stroke(width = stroke.width * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "Mechanical Pencil" -> {
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.9f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Square)
            )
        }
        "Charcoal" -> {
            // Textured scratch look: duplicate path slightly offset with randomized alpha dots
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.35f),
                style = Stroke(width = stroke.width * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Offset spots
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 3 == 0) {
                    drawScope.drawCircle(
                        color = drawColor.copy(alpha = 0.25f),
                        radius = stroke.width * (0.8f + Random.nextFloat() * 0.6f),
                        center = Offset(pt.x + (Random.nextFloat() * 6f - 3f), pt.y + (Random.nextFloat() * 6f - 3f))
                    )
                }
            }
        }
        "Crayon" -> {
            // Coarse textured overlapping strokes
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.7f),
                style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 2 == 0) {
                    drawScope.drawCircle(
                        color = drawColor.copy(alpha = 0.35f),
                        radius = stroke.width * 0.6f,
                        center = Offset(pt.x + (Random.nextFloat() * 4f - 2f), pt.y + (Random.nextFloat() * 4f - 2f))
                    )
                }
            }
        }
        "Marker" -> {
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.65f),
                style = Stroke(width = stroke.width * 1.8f, cap = StrokeCap.Square, join = StrokeJoin.Bevel)
            )
        }
        "Paintbrush" -> {
            drawScope.drawPath(
                path = path,
                color = drawColor.copy(alpha = 0.85f),
                style = Stroke(width = stroke.width * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "Airbrush" -> {
            // Extremely soft, fuzzy. Drawn using nested overlays with expanding stroke width and decaying alpha
            for (w in 4 downTo 1) {
                drawScope.drawPath(
                    path = path,
                    color = drawColor.copy(alpha = 0.08f),
                    style = Stroke(width = stroke.width * w * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        "Calligraphy Pen" -> {
            // Chisel stroke - draw line segments offset by flat rectangle angles
            for (i in 0 until stroke.points.size - 1) {
                val p1 = stroke.points[i]
                val p2 = stroke.points[i + 1]
                drawScope.drawLine(
                    color = drawColor,
                    start = Offset(p1.x - stroke.width, p1.y - stroke.width),
                    end = Offset(p2.x + stroke.width, p2.y + stroke.width),
                    strokeWidth = stroke.width * 0.5f,
                    cap = StrokeCap.Square
                )
            }
        }
        "Spray Paint" -> {
            // Splattered dots
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 2 == 0) {
                    for (k in 0..4) {
                        val angle = Random.nextFloat() * 2f * Math.PI
                        val dist = Random.nextFloat() * stroke.width * 3f
                        val splatX = pt.x + cos(angle).toFloat() * dist
                        val splatY = pt.y + sin(angle).toFloat() * dist
                        drawScope.drawCircle(
                            color = drawColor.copy(alpha = 0.6f),
                            radius = 1.5f + Random.nextFloat() * 2f,
                            center = Offset(splatX, splatY)
                        )
                    }
                }
            }
        }
        "Chalk" -> {
            // Intermittent lines with dropouts
            for (i in 0 until stroke.points.size - 1) {
                val p1 = stroke.points[i]
                val p2 = stroke.points[i + 1]
                if (Random.nextFloat() > 0.2f) { // dropout
                    drawScope.drawLine(
                        color = drawColor.copy(alpha = 0.8f),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = stroke.width * 1.2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        "Neon Glow Brush" -> {
            // Thick extremely transparent base + crisp thin white core
            val glowColor = Color(stroke.color)
            drawScope.drawPath(
                path = path,
                color = glowColor.copy(alpha = 0.15f),
                style = Stroke(width = stroke.width * 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawScope.drawPath(
                path = path,
                color = glowColor.copy(alpha = 0.40f),
                style = Stroke(width = stroke.width * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawScope.drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = stroke.width * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "Rainbow Trail Brush" -> {
            // Modulate hue dynamically along stroke segments
            for (i in 0 until stroke.points.size - 1) {
                val p1 = stroke.points[i]
                val p2 = stroke.points[i + 1]
                val hue = (i * 12f) % 360f
                val rainbowColor = Color.hsv(hue, 1f, 1f)
                drawScope.drawLine(
                    color = rainbowColor,
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
        }
        "Star Brush" -> {
            // Draw colorful starry polygons
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 5 == 0) {
                    val sizeStar = stroke.width * 1.5f
                    val starPath = Path()
                    val starColor = Color.hsv((idx * 27f) % 360f, 0.9f, 1f)
                    
                    for (k in 0..9) {
                        val r = if (k % 2 == 0) sizeStar else sizeStar / 2.2f
                        val angle = k * Math.PI / 5 + idx
                        val sx = pt.x + cos(angle).toFloat() * r
                        val sy = pt.y + sin(angle).toFloat() * r
                        if (k == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
                    }
                    starPath.close()
                    drawScope.drawPath(starPath, color = starColor)
                }
            }
        }
        "Bubble Brush" -> {
            // Translucent rounded sphere bubbles
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 4 == 0) {
                    val bubbleRad = stroke.width * 1.6f
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = bubbleRad,
                        center = Offset(pt.x, pt.y)
                    )
                    drawScope.drawCircle(
                        color = drawColor.copy(alpha = 0.8f),
                        radius = bubbleRad,
                        center = Offset(pt.x, pt.y),
                        style = Stroke(width = 2f)
                    )
                    // Highlighting dot
                    drawScope.drawCircle(
                        color = Color.White,
                        radius = bubbleRad * 0.2f,
                        center = Offset(pt.x - bubbleRad * 0.3f, pt.y - bubbleRad * 0.3f)
                    )
                }
            }
        }
        "Fire Brush" -> {
            // Draw flame vectors rising upwards
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 3 == 0) {
                    val flameH = stroke.width * (2f + Random.nextFloat() * 2.5f)
                    val flameW = stroke.width * (1f + Random.nextFloat() * 1.5f)
                    val firePath = Path().apply {
                        moveTo(pt.x - flameW, pt.y)
                        quadraticTo(pt.x - flameW * 0.2f, pt.y - flameH * 0.5f, pt.x, pt.y - flameH)
                        quadraticTo(pt.x + flameW * 0.2f, pt.y - flameH * 0.3f, pt.x + flameW, pt.y)
                        close()
                    }
                    drawScope.drawPath(
                        path = firePath,
                        color = if (Random.nextBoolean()) Color(0xFFFF5722) else Color(0xFFFF9800)
                    )
                    // Core flame
                    drawScope.drawCircle(
                        color = Color(0xFFFFEB3B),
                        radius = flameW * 0.4f,
                        center = Offset(pt.x, pt.y - flameH * 0.3f)
                    )
                }
            }
        }
        "Smoke Brush" -> {
            stroke.points.forEachIndexed { idx, pt ->
                if (idx % 4 == 0) {
                    val r = stroke.width * (1.2f + Random.nextFloat() * 1.8f)
                    drawScope.drawCircle(
                        color = Color(0xFFB0BEC5).copy(alpha = 0.12f),
                        radius = r,
                        center = Offset(pt.x, pt.y - idx * 0.1f)
                    )
                }
            }
        }
        "Pixel Brush" -> {
            // Square steps
            stroke.points.forEach { pt ->
                val sizePx = stroke.width * 1.4f
                val gridX = floor(pt.x / sizePx) * sizePx
                val gridY = floor(pt.y / sizePx) * sizePx
                drawScope.drawRect(
                    color = drawColor,
                    topLeft = Offset(gridX, gridY),
                    size = Size(sizePx - 1f, sizePx - 1f)
                )
            }
        }
        "Lightning Brush" -> {
            // Connected electrical steps
            if (stroke.points.isNotEmpty()) {
                val electricalPath = Path()
                electricalPath.moveTo(stroke.points.first().x, stroke.points.first().y)
                for (i in 1 until stroke.points.size) {
                    val pt1 = stroke.points[i - 1]
                    val pt2 = stroke.points[i]
                    val midX = (pt1.x + pt2.x) / 2f
                    val midY = (pt1.y + pt2.y) / 2f
                    // Add jitter
                    val jitterX = midX + (Random.nextFloat() * 20f - 10f)
                    val jitterY = midY + (Random.nextFloat() * 20f - 10f)
                    electricalPath.lineTo(jitterX, jitterY)
                    electricalPath.lineTo(pt2.x, pt2.y)
                }
                drawScope.drawPath(
                    path = electricalPath,
                    color = Color(0xFFFFEB3B),
                    style = Stroke(width = 3.5f)
                )
                drawScope.drawPath(
                    path = electricalPath,
                    color = Color.White,
                    style = Stroke(width = 1.2f)
                )
            }
        }
        else -> { // Default Ink outline pen
            drawScope.drawPath(
                path = path,
                color = drawColor,
                style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

// --- Interactive Creature Builder Component ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatureBuilderModule(
    viewModel: DoodleViewModel,
    modifier: Modifier = Modifier
) {
    val heads = listOf("Tiger", "Dragon", "Robot", "Eagle", "Quack-Duck")
    val bodies = listOf("Lion Core", "Galaxy Serpent", "Metallic Gears", "Feather Wings", "Dino Shell")
    val legs = listOf("Titan Paws", "Mechanical Tank Tread", "Webbed Duck Feet", "Dragon Claws")
    val wings = listOf("None", "Pegasus Feathers", "Cyberspace Neon Gliders", "Demon Spikes")
    val tails = listOf("None", "Fox Dust Brush", "Scorpion Needle", "Dino Spikes")

    var selectedHead by remember { mutableStateOf("Quack-Duck") }
    var selectedBody by remember { mutableStateOf("Dino Shell") }
    var selectedLeg by remember { mutableStateOf("Webbed Duck Feet") }
    var selectedWing by remember { mutableStateOf("Pegasus Feathers") }
    var selectedTail by remember { mutableStateOf("Fox Dust Brush") }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF2C3243), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "👾 Chimera Creature Builder Studio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4081)
            )
            Text(
                text = "Pick anatomies to construct and render a unique, hyper-realistic fantasy creature!",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Canvas Output of our Constructed beast
            Surface(
                color = Color(0xFF0F1117),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color(0xFF2A2E3D), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f + 20f

                    // 1. Draw Tail first (At the back )
                    if (selectedTail != "None") {
                        val tailColor = when (selectedTail) {
                            "Dino Spikes" -> Color(0xFF4CAF50)
                            "Scorpion Needle" -> Color(0xFFE91E63)
                            else -> Color(0xFFFFB74D) // Fox
                        }
                        drawPath(
                            path = Path().apply {
                                moveTo(cx - 50f, cy - 10f)
                                quadraticTo(cx - 120f, cy - 70f, cx - 110f, cy - 10f)
                                quadraticTo(cx - 70f, cy + 20f, cx - 50f, cy)
                            },
                            color = tailColor
                        )
                        // Details on Tail
                        if (selectedTail == "Dino Spikes") {
                            drawCircle(Color.White, 5f, Offset(cx - 90f, cy - 25f))
                            drawCircle(Color.White, 5f, Offset(cx - 75f, cy - 12f))
                        }
                    }

                    // 2. Draw Wings
                    if (selectedWing != "None") {
                        val wingColor = when (selectedWing) {
                            "Cyberspace Neon Gliders" -> Color(0xFF00E5FF)
                            "Demon Spikes" -> Color(0xFF2E1A47)
                            else -> Color(0xFFE0E0E0) // Pegasus white
                        }
                        drawPath(
                            path = Path().apply {
                                moveTo(cx + 10f, cy - 20f)
                                cubicTo(cx + 40f, cy - 90f, cx + 110f, cy - 90f, cx + 80f, cy - 10f)
                                cubicTo(cx + 60f, cy + 10f, cx + 25f, cy - 5f, cx + 10f, cy - 20f)
                            },
                            color = wingColor
                        )
                        if (selectedWing == "Cyberspace Neon Gliders") {
                            // Bright cyber stripes
                            drawLine(Color.White, Offset(cx + 25f, cy - 30f), Offset(cx + 70f, cy - 50f), strokeWidth = 3f)
                        }
                    }

                    // 3. Draw Body
                    val bodyColor = when (selectedBody) {
                        "Lion Core" -> Color(0xFFFFB74D)
                        "Galaxy Serpent" -> Color(0xFF7E57C2)
                        "Metallic Gears" -> Color(0xFF78909C)
                        "Feather Wings" -> Color(0xFFB0BEC5)
                        else -> Color(0xFF4CAF50) // Dino Shell
                    }
                    drawRoundRect(
                        color = bodyColor,
                        topLeft = Offset(cx - 55f, cy - 35f),
                        size = Size(110f, 70f),
                        cornerRadius = CornerRadius(28f)
                    )
                    // If Dino Shell, draw ridges
                    if (selectedBody == "Dino Shell") {
                        drawCircle(Color(0xFF388E3C), 12f, Offset(cx - 20f, cy - 30f))
                        drawCircle(Color(0xFF388E3C), 10f, Offset(cx + 15f, cy - 30f))
                    } else if (selectedBody == "Metallic Gears") {
                        drawCircle(Color.DarkGray, 15f, Offset(cx, cy), style = Stroke(width = 4f))
                    }

                    // 4. Draw Legs
                    val legColor = when (selectedLeg) {
                        "Titan Paws" -> Color(0xFFFFB74D)
                        "Mechanical Tank Tread" -> Color(0xFF37474F)
                        "Dragon Claws" -> Color(0xFFD84315)
                        else -> Color(0xFFFF9800) // Webbed Duck Feet
                    }
                    if (selectedLeg == "Mechanical Tank Tread") {
                        drawRoundRect(
                            color = legColor,
                            topLeft = Offset(cx - 45f, cy + 32f),
                            size = Size(35f, 18f),
                            cornerRadius = CornerRadius(4f)
                        )
                        drawRoundRect(
                            color = legColor,
                            topLeft = Offset(cx + 10f, cy + 32f),
                            size = Size(35f, 18f),
                            cornerRadius = CornerRadius(4f)
                        )
                    } else { // Draw standard dual limbs
                        drawRect(legColor, Offset(cx - 30f, cy + 25f), Size(15f, 25f))
                        drawRect(legColor, Offset(cx + 15f, cy + 25f), Size(15f, 25f))
                        // Duck/Dragon claws base
                        drawOval(legColor, Offset(cx - 38f, cy + 45f), Size(26f, 10f))
                        drawOval(legColor, Offset(cx + 7f, cy + 45f), Size(26f, 10f))
                    }

                    // 5. Draw Head + Neck connection
                    val headColor = when (selectedHead) {
                        "Tiger" -> Color(0xFFEF6C00)
                        "Dragon" -> Color(0xFFC62828)
                        "Robot" -> Color(0xFF90A4AE)
                        "Eagle" -> Color(0xFFECEFF1)
                        else -> Color(0xFFFFEB3B) // Quack Duck
                    }
                    // Neck connection rising up from body cylinder
                    drawRect(headColor, Offset(cx + 18f, cy - 60f), Size(20f, 40f))

                    // Draw skull oval
                    drawCircle(headColor, 25f, Offset(cx + 28f, cy - 65f))

                    // Details based on Head style
                    when (selectedHead) {
                        "Quack-Duck" -> {
                            // Orange bill beak!
                            drawPath(
                                path = Path().apply {
                                    moveTo(cx + 38f, cy - 68f)
                                    lineTo(cx + 65f, cy - 63f)
                                    lineTo(cx + 38f, cy - 58f)
                                    close()
                                },
                                color = Color(0xFFFF9800)
                            )
                            // Eye
                            drawCircle(Color.Black, 3f, Offset(cx + 35f, cy - 70f))
                        }
                        "Dragon" -> {
                            // Horns and red nostrils
                            drawPath(
                                path = Path().apply {
                                    moveTo(cx + 25f, cy - 85f)
                                    lineTo(cx + 15f, cy - 105f)
                                    lineTo(cx + 18f, cy - 85f)
                                    close()
                                },
                                color = Color(0xFF616161)
                            )
                            drawCircle(Color.Yellow, 4f, Offset(cx + 35f, cy - 70f))
                        }
                        "Robot" -> {
                            // Antennas and glowing blue laser eyes
                            drawLine(Color.Red, Offset(cx + 28f, cy - 85f), Offset(cx + 28f, cy - 98f), strokeWidth = 3f)
                            drawCircle(Color.Green, 4f, Offset(cx + 28f, cy - 98f))
                            drawCircle(Color(0xFF00E5FF), 3f, Offset(cx + 35f, cy - 68f))
                        }
                        "Tiger" -> {
                            // Ears and black stripes
                            drawCircle(headColor, 6f, Offset(cx + 12f, cy - 78f))
                            drawLine(Color.Black, Offset(cx + 22f, cy - 75f), Offset(cx + 30f, cy - 75f), strokeWidth = 2f)
                            drawCircle(Color.Black, 3f, Offset(cx + 32f, cy - 68f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selection pickers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedCard(
                    onClick = {
                        val idx = heads.indexOf(selectedHead)
                        selectedHead = heads[(idx + 1) % heads.size]
                    },
                    modifier = Modifier.weight(1f).padding(3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Head", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedHead, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
                    }
                }

                OutlinedCard(
                    onClick = {
                        val idx = bodies.indexOf(selectedBody)
                        selectedBody = bodies[(idx + 1) % bodies.size]
                    },
                    modifier = Modifier.weight(1f).padding(3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Body", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedBody, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
                    }
                }

                OutlinedCard(
                    onClick = {
                        val idx = legs.indexOf(selectedLeg)
                        selectedLeg = legs[(idx + 1) % legs.size]
                    },
                    modifier = Modifier.weight(1f).padding(3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Limb", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedLeg, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedCard(
                    onClick = {
                        val idx = wings.indexOf(selectedWing)
                        selectedWing = wings[(idx + 1) % wings.size]
                    },
                    modifier = Modifier.weight(1f).padding(3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wings", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedWing, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
                    }
                }

                OutlinedCard(
                    onClick = {
                        val idx = tails.indexOf(selectedTail)
                        selectedTail = tails[(idx + 1) % tails.size]
                    },
                    modifier = Modifier.weight(1f).padding(3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tail", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedTail, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        viewModel.selectTool("Pencil")
                        val complexPrompt = "A epic hyper-realistic portrait of a $selectedHead beast with the chest of a $selectedBody, supported by $selectedLeg, carrying $selectedWing wings and a $selectedTail tail."
                        viewModel.sendQuackersQuestion("Tell me dynamic tips on how to draw a chimera with $selectedHead head and $selectedBody body!")
                    },
                    modifier = Modifier.weight(1.2f).padding(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                ) {
                    Text("Sketch It!", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
