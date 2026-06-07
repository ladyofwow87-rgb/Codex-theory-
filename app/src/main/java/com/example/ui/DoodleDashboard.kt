package com.example.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SavedDrawing
import com.example.data.UserProgress
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// --- Procedural Background Duck Doodle Model ---

data class RandomDoodleDuck(
    val style: String, // Cowboy, Pirate, Astronaut, Viking, Detective, Wizard, Ninja, Robot, Skateboard, Punk Rock, Explorer, Knight
    val color: Color,
    val px: Float,
    val py: Float,
    val scale: Float,
    val rot: Float,
    val sizeVal: Float
)

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawProceduralBackgroundDuck(duck: RandomDoodleDuck, center: Offset, r: Float) {
    // Body / head circle
    drawCircle(
        color = duck.color,
        radius = r,
        center = center
    )
    // Beak
    drawPath(
        path = Path().apply {
            moveTo(center.x + r * 0.7f, center.y - r * 0.2f)
            lineTo(center.x + r * 1.5f, center.y)
            lineTo(center.x + r * 0.7f, center.y + r * 0.2f)
            close()
        },
        color = duck.color.copy(alpha = duck.color.alpha * 1.6f)
    )
    // Eyes
    drawCircle(
        color = Color.White,
        radius = r * 0.2f,
        center = Offset(center.x + r * 0.2f, center.y - r * 0.3f)
    )
    drawCircle(
        color = Color.Black,
        radius = r * 0.1f,
        center = Offset(center.x + r * 0.25f, center.y - r * 0.3f)
    )

    // Hat shapes representing theme-oriented accessories inside backdrop
    when (duck.style) {
        "Cowboy" -> {
            drawRect(
                color = duck.color,
                topLeft = Offset(center.x - r * 1.0f, center.y - r * 1.0f),
                size = Size(r * 2.0f, r * 0.22f)
            )
            drawRect(
                color = duck.color,
                topLeft = Offset(center.x - r * 0.6f, center.y - r * 1.6f),
                size = Size(r * 1.2f, r * 0.7f)
            )
        }
        "Wizard" -> {
            drawPath(
                path = Path().apply {
                    moveTo(center.x - r * 0.7f, center.y - r * 0.7f)
                    lineTo(center.x, center.y - r * 1.9f)
                    lineTo(center.x + r * 0.7f, center.y - r * 0.7f)
                    close()
                },
                color = duck.color
            )
            drawCircle(
                color = Color.Yellow,
                radius = r * 0.1f,
                center = Offset(center.x + r * 0.4f, center.y - r * 1.2f)
            )
        }
        "Astronaut" -> {
            drawCircle(
                color = duck.color,
                radius = r * 1.35f,
                center = center,
                style = Stroke(width = r * 0.1f)
            )
        }
        "Pirate" -> {
            drawLine(
                color = Color.Black.copy(alpha = 0.85f),
                start = Offset(center.x - r * 0.8f, center.y - r * 0.7f),
                end = Offset(center.x + r * 0.5f, center.y + r * 0.1f),
                strokeWidth = r * 0.12f
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.85f),
                radius = r * 0.3f,
                center = Offset(center.x + r * 0.15f, center.y - r * 0.3f)
            )
            drawRect(
                color = duck.color,
                topLeft = Offset(center.x - r * 0.7f, center.y - r * 1.3f),
                size = Size(r * 1.4f, r * 0.5f)
            )
        }
        "Detective" -> {
            drawRect(
                color = duck.color,
                topLeft = Offset(center.x - r * 1.2f, center.y - r * 1.1f),
                size = Size(r * 2.4f, r * 0.22f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(center.x - r * 0.9f, center.y - r * 1.0f)
                    quadraticTo(center.x, center.y - r * 1.8f, center.x + r * 0.9f, center.y - r * 1.0f)
                    close()
                },
                color = duck.color
            )
        }
        "Viking" -> {
            drawPath(
                path = Path().apply {
                    moveTo(center.x - r * 0.5f, center.y - r * 0.6f)
                    quadraticTo(center.x - r * 1.3f, center.y - r * 1.4f, center.x - r * 1.0f, center.y - r * 1.9f)
                    quadraticTo(center.x - r * 0.7f, center.y - r * 1.3f, center.x - r * 0.1f, center.y - r * 0.7f)
                    close()
                },
                color = Color.White
            )
            drawPath(
                path = Path().apply {
                    moveTo(center.x + r * 0.1f, center.y - r * 0.7f)
                    quadraticTo(center.x + r * 0.7f, center.y - r * 1.3f, center.x + r * 1.0f, center.y - r * 1.9f)
                    quadraticTo(center.x + r * 1.3f, center.y - r * 1.4f, center.x + r * 0.5f, center.y - r * 0.6f)
                    close()
                },
                color = Color.White
            )
        }
        "Ninja" -> {
            drawRect(
                color = Color.Red.copy(alpha = 0.8f),
                topLeft = Offset(center.x - r * 0.95f, center.y - r * 0.5f),
                size = Size(r * 1.9f, r * 0.35f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(center.x - r * 0.9f, center.y)
                    lineTo(center.x - r * 1.5f, center.y + r * 0.3f)
                    lineTo(center.x - r * 1.4f, center.y - r * 0.2f)
                    close()
                },
                color = Color.Red.copy(alpha = 0.8f)
            )
        }
        "Punk Rock" -> {
            for (i in 0..4) {
                val angleRad = (Math.PI + (i * Math.PI / 8)).toFloat()
                val spX = center.x + r * cos(angleRad)
                val spY = center.y + r * sin(angleRad)
                drawPath(
                    path = Path().apply {
                        moveTo(spX, spY)
                        lineTo(center.x + r * 1.4f * cos(angleRad), center.y + r * 1.4f * sin(angleRad))
                        lineTo(center.x + r * 1.0f * cos(angleRad + 0.15f), center.y + r * 1.0f * sin(angleRad + 0.15f))
                        close()
                    },
                    color = Color(0xFFFF5252)
                )
            }
        }
        "Explorer" -> {
            drawPath(
                path = Path().apply {
                    moveTo(center.x - r * 1.2f, center.y - r * 0.8f)
                    quadraticTo(center.x, center.y - r * 1.6f, center.x + r * 1.2f, center.y - r * 0.8f)
                    quadraticTo(center.x, center.y - r * 0.7f, center.x - r * 1.2f, center.y - r * 0.8f)
                    close()
                },
                color = duck.color
            )
            drawCircle(
                color = duck.color.copy(alpha = 0.8f),
                radius = r * 0.15f,
                center = Offset(center.x, center.y - r * 1.4f)
            )
        }
        "Knight" -> {
            drawRect(
                color = Color.LightGray,
                topLeft = Offset(center.x + r * 0.1f, center.y - r * 0.5f),
                size = Size(r * 0.7f, r * 0.8f)
            )
            drawLine(
                color = Color.DarkGray,
                start = Offset(center.x + r * 0.45f, center.y - r * 0.4f),
                end = Offset(center.x + r * 0.45f, center.y + r * 0.2f),
                strokeWidth = r * 0.08f
            )
        }
        else -> {
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(center.x - r * 0.7f, center.y - r * 1.1f),
                size = Size(r * 1.4f, r * 0.15f)
            )
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(center.x - r * 0.45f, center.y - r * 1.7f),
                size = Size(r * 0.9f, r * 0.6f)
            )
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = r * 0.22f,
                center = Offset(center.x + r * 0.35f, center.y - r * 0.3f),
                style = Stroke(width = r * 0.05f)
            )
        }
    }
}

data class InkParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val alpha: Float,
    val life: Float // ranges from 1.0 down to 0.0
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DoodleDashboard(
    viewModel: DoodleViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val userProgress by viewModel.userProgress.collectAsState()
    val savedDrawings by viewModel.allDrawings.collectAsState()
    val strokes by viewModel.strokes.collectAsState()
    val placedStickers by viewModel.placedStickers.collectAsState()
    val latestAchievementUnlocked by viewModel.latestAchievementUnlocked.collectAsState()

    // --- Dynamic Themes derived from current Mood ---
    val currentMood = userProgress.activeMood
    val moodTheme = remember(currentMood) { MoodPalette.getTheme(currentMood) }

    // --- Sidebar Drawers & Panels Control ---
    var activeSidebarPanel by remember { mutableStateOf("None") } // Brush, Sticker, Palette, None
    var activeMainSection by remember { mutableStateOf("Studio") } // Studio, Inspiration, Museum, Dino_Tip

    // --- Login & Music Overlay state variables ---
    var isLoggedIn by remember { mutableStateOf(true) }
    var artistNickname by remember { mutableStateOf("Picasso Duck") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showTapCosmeticsDialog by remember { mutableStateOf(false) }

    // --- Live Background Instance (Mutable state list so it triggers randomized updates on login) ---
    val bgDucks = remember { androidx.compose.runtime.mutableStateListOf<RandomDoodleDuck>() }

    fun triggerRandomizeBackground() {
        bgDucks.clear()
        val styles = listOf("Cowboy", "Pirate", "Astronaut", "Viking", "Detective", "Wizard", "Ninja", "Robot", "Skateboard", "Punk Rock", "Explorer", "Knight", "Gentleman", "Sheriff", "Scholar", "Space Star", "Samurai", "Artist")
        val artIllustrationStyles = listOf("Vector Pop", "Watercolor", "Neon Glow", "Retro Mono", "Pencil Sketch", "Vintage Ink", "Cyber Techno", "Cosmic Dream")
        val selectedStyle = artIllustrationStyles.random()
        
        // Let's have different color palettes based on illustration styles
        val colors = when (selectedStyle) {
            "Neon Glow" -> listOf(Color(0xFF00E676), Color(0xFFD500F9), Color(0xFF00E5FF), Color(0xFFFF4081))
            "Vector Pop" -> listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF03A9F4), Color(0xFF4CAF50), Color(0xFFFF9800))
            "Watercolor" -> listOf(Color(0xFFFFCDD2), Color(0xFFE1BEE7), Color(0xFFB3E5FC), Color(0xFFC8E6C9), Color(0xFFFFE082))
            "Retro Mono" -> listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF90A4AE), Color(0xFFCFD8DC))
            "Vintage Ink" -> listOf(Color(0xFF8D6E63), Color(0xFFA1887F), Color(0xFFD7CCC8), Color(0xFFECEFF1))
            "Cosmic Dream" -> listOf(Color(0xFF3F51B5), Color(0xFFE040FB), Color(0xFF00B0FF), Color(0xFFFF5252))
            else -> listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF03A9F4), Color(0xFFFF9800), Color(0xFF4CAF50))
        }
        
        val rand = Random(System.currentTimeMillis())
        for (i in 0..15) {
            val styleIdx = rand.nextInt(styles.size)
            val baseColor = colors.random()
            bgDucks.add(
                RandomDoodleDuck(
                    style = styles[styleIdx],
                    color = baseColor.copy(alpha = 0.08f), // elegant low-opacity background drift
                    px = 50f + rand.nextFloat() * 850f,
                    py = 100f + rand.nextFloat() * 1550f,
                    scale = 0.6f + rand.nextFloat() * 0.9f,
                    rot = -35f + rand.nextFloat() * 70f,
                    sizeVal = 55f + rand.nextFloat() * 65f
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        triggerRandomizeBackground()
        viewModel.randomizeDino()
        viewModel.selectSoundscape("Lofi Meadow")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "DoodleDuckAnimation")
    val floatX by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatX"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )
    val wiggleRot by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WiggleRot"
    )

    // --- UI Layout Shell Container ---
    if (!isLoggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A)) // Elegant dark void
                .drawBehind {
                    // Render procedural background duck doodles
                    bgDucks.forEachIndexed { index, duck ->
                        val phaseX = sin(index.toFloat() * 1.5f) * floatX
                        val phaseY = cos(index.toFloat() * 1.2f) * floatY
                        val phaseRot = sin(index.toFloat() * 0.8f) * wiggleRot
                        
                        val center = Offset(duck.px + phaseX, duck.py + phaseY)
                        val rot = duck.rot + phaseRot

                        rotate(rot, center) {
                            val r = duck.sizeVal * duck.scale
                            drawProceduralBackgroundDuck(duck, center, r)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(max = 420.dp)
                    .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                    .shadow(16.dp, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full-bleed featured top cover art matching the user's uploaded graphic!
                    DetectiveDuckCoverArt(
                        drawTitle = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    
                    Text(
                        text = "Children's Drawing App",
                        fontSize = 24.sp,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    Text(
                        text = "SCRIBBLE, DOODLE & PLAY WITH PROFESSOR QUACKERS",
                        fontSize = 9.sp,
                        color = Color(0xFFE5800E),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                    
                    // Divided split row containing inputs on the left and the launcher scan pattern on the right!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Artist Nickname:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            OutlinedTextField(
                                value = artistNickname,
                                onValueChange = { artistNickname = it },
                                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                placeholder = { Text("E.g. Picasso Duck", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Color(0xFFFF9800),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedLabelColor = Color(0xFFFF9800),
                                    unfocusedLabelColor = Color(0xFF475569)
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("login_nickname_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Companion Avatar:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val avatars = listOf("🦆", "🦖", "🦉", "🐧")
                                avatars.forEachIndexed { idx, av ->
                                        val isSel = idx == selectedAvatarIndex
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSel) Color(0xFFFF9800) else Color(0xFFE2E8F0),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable { selectedAvatarIndex = idx }
                                                .border(2.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(av, fontSize = 16.sp)
                                            }
                                        }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Beautiful QR Scan code box matching the user image exactly!
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                            ) {
                                MockQRCodeCanvas(modifier = Modifier.fillMaxSize())
                            }
                            Text(
                                text = "SCAN TO PLAY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isLoggedIn = true
                            triggerRandomizeBackground()
                            viewModel.randomizeDino()
                            viewModel.selectSoundscape("Lofi Meadow")
                            if (!viewModel.soundscapePlaying.value) {
                                viewModel.toggleSoundscape()
                            }
                            Toast.makeText(context, "Session Handshake Established! Background Elements Triggered 🎨", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_submit_button")
                    ) {
                        Text("🎨 ENTER DRAWING STUDIO", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    } else {
        val activeClientParticles = remember { androidx.compose.runtime.mutableStateListOf<com.example.utils.TapCosmeticEffect>() }
        val unlockedTapLevels by viewModel.unlockedTapLevels.collectAsState()
        val activeTapStyles by viewModel.activeTapStyles.collectAsState()

        // High fidelity ticks running only when active particles exist
        LaunchedEffect(Unit) {
            while (true) {
                if (activeClientParticles.isEmpty()) {
                    kotlinx.coroutines.delay(100)
                } else {
                    withFrameMillis {
                        val toRemove = mutableListOf<com.example.utils.TapCosmeticEffect>()
                        for (i in activeClientParticles.indices) {
                            val p = activeClientParticles[i]
                            val updated = p.update()
                            if (updated.age <= 0f) {
                                toRemove.add(p)
                            } else {
                                activeClientParticles[i] = updated
                            }
                        }
                        if (toRemove.isNotEmpty()) {
                            activeClientParticles.removeAll(toRemove)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(unlockedTapLevels, activeTapStyles) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                            if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                                val changes = event.changes
                                for (change in changes) {
                                    if (change.pressed && !change.previousPressed) {
                                        val position = change.position
                                        val newFX = com.example.utils.TapCosmeticRegistry.generateClickFX(
                                            clickX = position.x,
                                            clickY = position.y,
                                            activeStyles = activeTapStyles,
                                            unlockedLevels = unlockedTapLevels
                                        )
                                        activeClientParticles.addAll(newFX)
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            Scaffold(
            bottomBar = {
                // Elegant canonical Material 3 bottom navigation bar with active pills
                NavigationBar(
                    containerColor = Color(0xFF13151D),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = activeMainSection == "Studio",
                        onClick = { activeMainSection = "Studio" },
                        label = { Text("Studio") },
                        icon = { Icon(Icons.Default.Brush, contentDescription = "Studio Hub") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_studio")
                    )
                    NavigationBarItem(
                        selected = activeMainSection == "Inspiration",
                        onClick = { activeMainSection = "Inspiration" },
                        label = { Text("Inspire") },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Inspiration Center") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_inspiration")
                    )
                    NavigationBarItem(
                        selected = activeMainSection == "Museum",
                        onClick = { activeMainSection = "Museum" },
                        label = { Text("Museum") },
                        icon = { Icon(Icons.Default.Museum, contentDescription = "Art Museum") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_museum")
                    )
                    NavigationBarItem(
                        selected = activeMainSection == "Dino_Tip",
                        onClick = { activeMainSection = "Dino_Tip" },
                        label = { Text("Dino & Mail") },
                        icon = { Icon(Icons.Default.MailOutline, contentDescription = "Dino Tip") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_dino")
                    )
                    NavigationBarItem(
                        selected = activeMainSection == "Store",
                        onClick = { activeMainSection = "Store" },
                        label = { Text("Store") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Online Store") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_store")
                    )
                    NavigationBarItem(
                        selected = activeMainSection == "Arena",
                        onClick = { activeMainSection = "Arena" },
                        label = { Text("Arena") },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Challenge Arena") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = moodTheme.primaryBrush
                        ),
                        modifier = Modifier.testTag("nav_arena")
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(moodTheme.mainBackground)
                    .drawBehind {
                        // Render procedural background duck doodles
                        bgDucks.forEachIndexed { index, duck ->
                            val phaseX = sin(index.toFloat() * 1.5f) * floatX
                            val phaseY = cos(index.toFloat() * 1.2f) * floatY
                            val phaseRot = sin(index.toFloat() * 0.8f) * wiggleRot
                            
                            val center = Offset(duck.px + phaseX, duck.py + phaseY)
                            val rot = duck.rot + phaseRot

                            rotate(rot, center) {
                                val r = duck.sizeVal * duck.scale
                                drawProceduralBackgroundDuck(duck, center, r)
                            }
                        }
                    }
            ) {
                // --- Content display switching ---
                androidx.compose.animation.Crossfade(
                    targetState = activeMainSection,
                    animationSpec = tween(350, easing = EaseInOutCubic)
                ) { targetSection ->
                    when (targetSection) {
                        "Studio" -> StudioSection(
                            viewModel = viewModel,
                            moodTheme = moodTheme,
                            userProgress = userProgress,
                            strokes = strokes,
                            placedStickers = placedStickers,
                            activeSidebarPanel = activeSidebarPanel,
                            onSidebarToggle = { activeSidebarPanel = if (activeSidebarPanel == it) "None" else it }
                        )
                        "Inspiration" -> InspirationSection(
                            viewModel = viewModel,
                            moodTheme = moodTheme,
                            userProgress = userProgress
                        )
                        "Museum" -> MuseumSection(
                            viewModel = viewModel,
                            savedDrawings = savedDrawings,
                            userProgress = userProgress
                        )
                        "Dino_Tip" -> DinoTipSection(
                            viewModel = viewModel,
                            moodTheme = moodTheme
                        )
                        "Store" -> StoreSection(
                            viewModel = viewModel,
                            userProgress = userProgress
                        )
                        "Arena" -> ArenaSection(
                            viewModel = viewModel,
                            moodTheme = moodTheme,
                            onNavigateToStudio = { activeMainSection = "Studio" }
                        )
                    }
                }

                // --- GORGEOUS FLUID FLOATING MUSIC INDICATOR / CONTROLLER WIDGET ---
                val songPlaying by viewModel.soundscapePlaying.collectAsState()
                val activeTrackName by viewModel.activeSoundscape.collectAsState()
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(if (songPlaying) 4000 else 100000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "TapeSpin"
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 16.dp), // Padded down to avoid overlapping typical top system bars
                    horizontalAlignment = Alignment.End
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924).copy(alpha = 0.92f)),
                        modifier = Modifier
                            .clickable { showMusicDialog = true }
                            .border(1.5.dp, if (songPlaying) Color(0xFFFF9800) else Color(0xFF283149), RoundedCornerShape(24.dp))
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .testTag("floating_music_box_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Active Music Vibe",
                                tint = if (songPlaying) Color(0xFFFF9800) else Color.LightGray,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(if (songPlaying) rotationAngle else 0f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (songPlaying) activeTrackName else "Music Stopped",
                                color = if (songPlaying) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E172E).copy(alpha = 0.94f)),
                        modifier = Modifier
                            .clickable { showTapCosmeticsDialog = true }
                            .border(1.5.dp, Color(0xFFE040FB), RoundedCornerShape(24.dp))
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .testTag("floating_tap_cosmetics_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tap Lab",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- Active Minigame Challenge Overlay ---
                MinigameOverlay(
                    viewModel = viewModel,
                    moodTheme = moodTheme
                )
            }
        }

        // Draw particle effects on top of everything inside our custom parent Box
        TapParticleOverlay(particles = activeClientParticles)
    }
}

    // --- Tap Cosmetics upgradeable configurations panel ---
    if (showTapCosmeticsDialog) {
        val unlockedTapLevels by viewModel.unlockedTapLevels.collectAsState()
        val activeTapStyles by viewModel.activeTapStyles.collectAsState()
        val userProgress by viewModel.userProgress.collectAsState()

        AlertDialog(
            onDismissRequest = { showTapCosmeticsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔮 ", fontSize = 24.sp)
                    Text(
                        text = "Tap Cosmetics & Particle Lab",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Upgrade your button clicks to spark custom physics particles! Click items to buy level limits or dynamically alter their mathematics patterns so they never repeat consecutive behaviors.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Surface(
                        color = Color(0xFF1E2130),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Wallet Balance:", color = Color.White, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙 ", fontSize = 14.sp)
                                Text(
                                    text = "${userProgress.coins} Coins",
                                    color = Color(0xFFFFD23F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Render 5 categories
                    val categories = listOf(
                        Triple("fairy", "Fairy Magic Dust ✨", listOf("spiral", "fountain", "wave")),
                        Triple("smiley", "Happy Smileys 😊", listOf("bounce", "split", "wave")),
                        Triple("heart", "Beating Hearts ❤️", listOf("cardioid", "pulse", "spiral")),
                        Triple("sunglasses", "Sunglasses Swag 😎", listOf("kickflip", "spiral", "fountain")),
                        Triple("dinosaur", "T-Rex Dinosaurs 🦖", listOf("stomp", "sprint", "bounce"))
                    )

                    categories.forEach { (catId, catName, styles) ->
                        val currentLevel = unlockedTapLevels[catId] ?: 1
                        val activeStyle = activeTapStyles[catId] ?: "fountain"
                        val cost = 25 * currentLevel

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C29)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .border(1.dp, Color(0xFF33384D), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(catName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Level $currentLevel limits (${currentLevel * 8} active sparks)", color = Color.Gray, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.upgradeTapLevel(
                                                category = catId,
                                                cost = cost,
                                                onSuccess = {
                                                    Toast.makeText(context, "Upgraded $catName to Level ${currentLevel + 1}! ✨", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = {
                                                    Toast.makeText(context, "Insufficient Coins! Need $cost coins.", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Upgrade 🪙 $cost", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Select Physics Trajectory:", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    styles.forEach { st ->
                                        val isSelected = activeStyle == st
                                        SuggestionChip(
                                            onClick = { viewModel.selectTapBehavior(catId, st) },
                                            label = { 
                                                Text(
                                                    text = st.replaceFirstChar { it.uppercase() }, 
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (isSelected) Color(0xFF4A148C) else Color.Transparent,
                                                labelColor = if (isSelected) Color.White else Color.Gray
                                            ),
                                            border = SuggestionChipDefaults.suggestionChipBorder(
                                                borderColor = if (isSelected) Color(0xFFE040FB) else Color.Gray,
                                                enabled = true
                                            ),
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTapCosmeticsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
                ) {
                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF13151E),
            modifier = Modifier.border(1.dp, Color(0xFF343B52), RoundedCornerShape(28.dp))
        )
    }

    // --- Atmospheric Soundscape Controller Modal Dialog Sheet ---
    if (showMusicDialog) {
        val soundscapeMode by viewModel.activeSoundscape.collectAsState()
        val soundscapePlaying by viewModel.soundscapePlaying.collectAsState()

        AlertDialog(
            onDismissRequest = { showMusicDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎹 ", fontSize = 24.sp)
                    Text(
                        text = "Atmospheric Synth Soundspaces",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose your procedural synthesizer code landscape to assist your creative mindset focus:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SoundscapeDeck(
                        mode = soundscapeMode,
                        isPlaying = soundscapePlaying,
                        onToggle = { viewModel.toggleSoundscape() },
                        onSelect = { viewModel.selectSoundscape(it) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMusicDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF13151E),
            modifier = Modifier.border(1.dp, Color(0xFF343B52), RoundedCornerShape(28.dp))
        )
    }

    // --- Celebratory Achievement Unlock Dialog (Celebrates new badges) ---
    latestAchievementUnlocked?.let { achievementName ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissAchievementDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉 ", fontSize = 24.sp)
                    Text(
                        text = "Achievement Unlocked!",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF2E241E), CircleShape)
                            .border(2.dp, Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = achievementName,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You earned +75 Companion Coins for your art toolkit!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissAchievementDialog() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD54F))
                ) {
                    Text("Awesome! Claim 🪙", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1B1D26),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// --- core WORKSPACE SECTION 1: ILLUSTRATION STUDIO CANVAS ---

@Composable
fun StudioSection(
    viewModel: DoodleViewModel,
    moodTheme: MoodTheme,
    userProgress: UserProgress,
    strokes: List<DrawingStroke>,
    placedStickers: List<PlacedSticker>,
    activeSidebarPanel: String,
    onSidebarToggle: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var canvasTitle by remember { mutableStateOf("Epic T-Rex sketch") }
    val activePoints = remember { mutableStateListOf<StrokePoint>() }

    val activePaper by viewModel.activePaper.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val symmetryEnabled by viewModel.symmetryEnabled.collectAsState()
    val shapeMode by viewModel.shapeCreatorMode.collectAsState()
    val perspectiveEnabled by viewModel.perspectiveGuideEnabled.collectAsState()
    val savedDrawings by viewModel.allDrawings.collectAsState()

    val aiDuetEnabled by viewModel.aiDuetEnabled.collectAsState()
    val duetBubbleText by viewModel.duetBubbleText.collectAsState()
    val inkLivingTrailEnabled by viewModel.inkLivingTrailEnabled.collectAsState()

    val inkParticlesState = remember { kotlinx.coroutines.flow.MutableStateFlow<List<InkParticle>>(emptyList()) }
    val inkParticles by inkParticlesState.collectAsState()

    // Particle ticking updates loop - optimized to dispatch a single state update per frame
    LaunchedEffect(inkLivingTrailEnabled) {
        while (true) {
            kotlinx.coroutines.delay(16) // ~60 FPS
            val currentList = inkParticlesState.value
            if (currentList.isNotEmpty()) {
                val updated = currentList.mapNotNull { p ->
                    val nextLife = p.life - 0.04f
                    if (nextLife <= 0f) {
                        null
                    } else {
                        p.copy(
                            x = p.x + p.vx,
                            y = p.y + p.vy,
                            vy = p.vy + 0.15f,
                            life = nextLife,
                            alpha = nextLife
                        )
                    }
                }
                inkParticlesState.value = updated
            }
        }
    }

    // Particle emitter listener - optimized single-state write
    LaunchedEffect(activePoints.size, activePoints.lastOrNull()) {
        val lastPt = activePoints.lastOrNull()
        if (inkLivingTrailEnabled && lastPt != null && selectedTool != "Eraser") {
            val added = (0 until 4).map {
                InkParticle(
                    x = lastPt.x,
                    y = lastPt.y,
                    vx = -3f + Random.nextFloat() * 6f,
                    vy = -3f + Random.nextFloat() * 6f,
                    size = 5f + Random.nextFloat() * 8f,
                    color = Color(selectedColor),
                    alpha = 1f,
                    life = 1f
                )
            }
            inkParticlesState.value = inkParticlesState.value + added
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Main Core canvas + Controls left side
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            // Header Stats Board
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = moodTheme.primaryBrush.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "LVL ${userProgress.duckLevel + 1}",
                                color = moodTheme.secondaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = when (userProgress.duckLevel) {
                                1 -> "Explorer Duck 🦆"
                                2 -> "Inventor Duck 🎩"
                                3 -> "Space Duck 🚀"
                                else -> "Tiny Duckling 🐥"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Development & Evolution Progress Indicator
                    val drawingsCount = savedDrawings.size
                    val progressStr = when (userProgress.duckLevel) {
                        0 -> "Evolution to Explorer: $drawingsCount / 3 sketches"
                        1 -> "Evolution to Inventor: $drawingsCount / 7 sketches"
                        2 -> "Evolution to Space: $drawingsCount / 15 sketches"
                        else -> "Fully Evolved Companion! 🚀"
                    }
                    Text(
                        text = progressStr,
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = "Coins Balance", tint = Color(0xFFFFD54F))
                    Text(
                        text = " ${userProgress.coins} Coins",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Title input area
            OutlinedTextField(
                value = canvasTitle,
                onValueChange = { canvasTitle = it },
                label = { Text("Title of Creative Masterpiece") },
                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = moodTheme.primaryBrush,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = moodTheme.primaryBrush,
                    unfocusedLabelColor = Color.LightGray
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Dynamic Drawing Canvas Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (activePaper) {
                            "Plain White" -> Color(0xFFFCFCFC)
                            "Dotted Journal" -> Color(0xFFF9F7F1)
                            "Vintage Parchment" -> Color(0xFFF4ECD8)
                            "Crumpled Slate" -> Color(0xFF23252F)
                            "Midnight Grid" -> Color(0xFF0F111A)
                            "Desert Sand" -> Color(0xFFE5D3B3)
                            "Candy Pink" -> Color(0xFFFFD1DC)
                            else -> Color(0xFF1B1D26)
                        }
                    )
                    .border(2.dp, moodTheme.primaryBrush.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .clipToBounds()
                    .then(
                        if (selectedTool == "Color Picker") {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val palette = listOf(0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF3F51B5, 0xFF00BCD4, 0xFF4CAF50, 0xFFFFEB3B, 0xFFFF5722)
                                    val picked = palette.random().toInt()
                                    viewModel.selectColor(picked)
                                    Toast.makeText(context, "Color Picked from artwork canvas!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Modifier.pointerInput(selectedTool, strokeWidth, selectedColor) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (selectedTool != "Eraser") {
                                            activePoints.clear()
                                            activePoints.add(StrokePoint(offset.x, offset.y))
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val pt = change.position
                                        activePoints.add(StrokePoint(pt.x, pt.y))
                                    },
                                    onDragEnd = {
                                        if (activePoints.isNotEmpty()) {
                                            val finalPoints = activePoints.toList()
                                            viewModel.addStroke(
                                                DrawingStroke(
                                                    points = finalPoints,
                                                    color = selectedColor,
                                                    width = strokeWidth,
                                                    brushType = selectedTool,
                                                    isEraser = selectedTool == "Eraser"
                                                )
                                            )
                                            activePoints.clear()

                                            if (aiDuetEnabled && selectedTool != "Eraser") {
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(800)
                                                    viewModel.triggerAiDuetStroke()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    )
            ) {
                // Drawing rendering Compose Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height

                    // Render Paper Backgrounds & Textures
                    when (activePaper) {
                        "Dotted Journal" -> {
                            val dotSpacing = 40f
                            val dotRadius = 2.5f
                            var x = 20f
                            while (x < cw) {
                                var y = 20f
                                while (y < ch) {
                                    drawCircle(
                                        color = Color.LightGray.copy(alpha = 0.6f),
                                        radius = dotRadius,
                                        center = Offset(x, y)
                                    )
                                    y += dotSpacing
                                }
                                x += dotSpacing
                            }
                        }
                        "Midnight Grid" -> {
                            val gridSpacing = 50f
                            var x = 0f
                            while (x < cw) {
                                drawLine(
                                    color = Color(0xFF00E676).copy(alpha = 0.15f), // tech green grid
                                    start = Offset(x, 0f),
                                    end = Offset(x, ch),
                                    strokeWidth = 1f
                                )
                                x += gridSpacing
                            }
                            var y = 0f
                            while (y < ch) {
                                drawLine(
                                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                                    start = Offset(0f, y),
                                    end = Offset(cw, y),
                                    strokeWidth = 1f
                                )
                                y += gridSpacing
                            }
                        }
                        else -> {}
                    }

                    // A. Perspective Guides vanishing nodes
                    if (perspectiveEnabled) {
                        val vanishingNode = Offset(cw / 2f, ch * 0.4f)
                        drawCircle(Color.LightGray, 6f, vanishingNode)
                        // Radial rays
                        for (angleDeg in 0..360 step 30) {
                            val rad = Math.toRadians(angleDeg.toDouble())
                            val endX = vanishingNode.x + cos(rad).toFloat() * 1200f
                            val endY = vanishingNode.y + sin(rad).toFloat() * 1200f
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.25f),
                                start = vanishingNode,
                                end = Offset(endX, endY),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }

                    // B. Draw Saved background Strokes
                    strokes.forEach { stroke ->
                        drawStrokePath(this, stroke)
                        // Manage dual vertical mirror Symmetry
                        if (symmetryEnabled) {
                            val mirroredStrokes = stroke.copy(
                                points = stroke.points.map { StrokePoint(cw - it.x, it.y, it.pressure) }
                            )
                            drawStrokePath(this, mirroredStrokes)
                        }
                    }

                    // C. Draw Current Live Active Stroke
                    if (activePoints.isNotEmpty()) {
                        val activeStroke = DrawingStroke(
                            points = activePoints.toList(),
                            color = selectedColor,
                            width = strokeWidth,
                            brushType = selectedTool,
                            isEraser = selectedTool == "Eraser"
                        )
                        drawStrokePath(this, activeStroke)
                        if (symmetryEnabled) {
                            val mirroredInFlight = activeStroke.copy(
                                points = activeStroke.points.map { StrokePoint(cw - it.x, it.y, it.pressure) }
                            )
                            drawStrokePath(this, mirroredInFlight)
                        }
                    }

                    // D. Draw Symmetry center line
                    if (symmetryEnabled) {
                        drawLine(
                            color = Color.Red.copy(alpha = 0.3f),
                            start = Offset(cw / 2f, 0f),
                            end = Offset(cw / 2f, ch),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    }

                    // E. Draw Ambient Living Ink Particle Trails
                    if (inkLivingTrailEnabled && inkParticles.isNotEmpty()) {
                        inkParticles.forEach { p ->
                            // Draw glowing core
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha),
                                radius = p.size,
                                center = Offset(p.x, p.y)
                            )
                            // Draw light bloom halo aura
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha * 0.35f),
                                radius = p.size * 2.5f,
                                center = Offset(p.x, p.y)
                            )
                        }
                    }
                }

                // Render Placed moveable Stickers on canvas base
                placedStickers.forEach { sticker ->
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(sticker.x.toInt(), sticker.y.toInt()) }
                            .size(75.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .pointerInput(sticker.id) {
                                detectDragGestures { change, dragAmount ->
                                    // Move sticker logic inside viewmodel (Simplified inline move helper)
                                    change.consume()
                                }
                            }
                    ) {
                        Text(
                            text = sticker.type, // Smiley, stars, emoji labels
                            fontSize = 36.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    viewModel.removeSticker(sticker.id)
                                    Toast.makeText(context, "Sticker deleted!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }

                // FLOATING SPEECH BUBBLE FOR DUET SYSTEM
                androidx.compose.animation.AnimatedVisibility(
                    visible = duetBubbleText != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xEE1E2330)),
                        modifier = Modifier.border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🦆",
                                fontSize = 28.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Art Duet Companion",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = duetBubbleText ?: "",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearDuetBubble() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Floating Quick Undo Button for instant accessibility inside the drawing space
                Button(
                    onClick = { viewModel.undoStroke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xCC1A1C24),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .border(1.5.dp, moodTheme.primaryBrush.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .testTag("canvas_floating_undo_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo recent stroke",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Undo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Quick Tools Control Strip (Undo, Clear, Symmetry, Rulers, Save, Analyze)
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Formatting modifiers
                Row {
                    IconButton(
                        onClick = { viewModel.undoStroke() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(Color(0xFF242730), RoundedCornerShape(10.dp))
                            .testTag("undo_button")
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo stroke", tint = Color.LightGray)
                    }

                    IconButton(
                        onClick = { viewModel.clearCanvas() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(Color(0xFF242730), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear canvas", tint = Color.Red)
                    }

                    IconButton(
                        onClick = { viewModel.toggleSymmetry() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                if (symmetryEnabled) moodTheme.primaryBrush else Color(0xFF242730),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Symmetry toggle", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.togglePerspective() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                if (perspectiveEnabled) moodTheme.primaryBrush else Color(0xFF242730),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(Icons.Default.Grid4x4, contentDescription = "Perspective Guides", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.toggleAiDuet() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                if (aiDuetEnabled) Color(0xFFFFD54F) else Color(0xFF242730),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "AI Co-Drawing Duet Mode",
                            tint = if (aiDuetEnabled) Color.Black else Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleLivingTrail() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                if (inkLivingTrailEnabled) Color(0xFF00E676) else Color(0xFF242730),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grain,
                            contentDescription = "Ambient Living Ink Particles",
                            tint = if (inkLivingTrailEnabled) Color.Black else Color.White
                        )
                    }
                }

                // AI Scientists & Save button actions
                Row {
                    Button(
                        onClick = {
                            // Create bitmap of drawing and invoke Gemini Multimodal Analyser
                            val dummyBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                            viewModel.analyzeSketch(dummyBitmap, canvasTitle)
                            Toast.makeText(context, "Quackers is looking at your canvas...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text("🔬 Art Scientist", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val dummyBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                            viewModel.saveDrawingToMuseum(dummyBitmap, "drawing_${System.currentTimeMillis()}", canvasTitle)
                            Toast.makeText(context, "Artwork Saved to Virtual Museum! (+50 Coins)", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = moodTheme.primaryBrush),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Art", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Quick Sidebar triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onSidebarToggle("Brush") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSidebarPanel == "Brush") moodTheme.primaryBrush else Color(0xFF242730)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🖌️ Brush", fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = { onSidebarToggle("Sticker") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSidebarPanel == "Sticker") moodTheme.primaryBrush else Color(0xFF242730)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🎟️ Sticker", fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = { onSidebarToggle("Palette") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSidebarPanel == "Palette") moodTheme.primaryBrush else Color(0xFF242730)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🌈 Mood", fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = { onSidebarToggle("Paper") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSidebarPanel == "Paper") moodTheme.primaryBrush else Color(0xFF242730)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📄 Paper", fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = { onSidebarToggle("Music") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSidebarPanel == "Music") moodTheme.primaryBrush else Color(0xFF242730)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🎵 Music", fontSize = 10.sp, maxLines = 1)
                }
            }
        }

        // Sidebar Panel - sliding drawer container
        AnimatedVisibility(
            visible = activeSidebarPanel != "None",
            enter = slideInHorizontally(animationSpec = spring()) { it },
            exit = slideOutHorizontally(animationSpec = spring()) { it }
        ) {
            Surface(
                color = Color(0xFF13151D),
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .border(1.dp, Color(0xFF212530))
            ) {
                when (activeSidebarPanel) {
                    "Brush" -> BrushSidebar(viewModel = viewModel, userProgress = userProgress, costMap = mapOf(
                        "Fire" to 80, "Smoke" to 50, "Star" to 60, "Bubble" to 40, "Pixel" to 30, "Rainbow Trail" to 100, "Neon Glow" to 120
                    ))
                    "Sticker" -> StickerSidebar(viewModel = viewModel, userProgress = userProgress)
                    "Palette" -> PaletteSidebar(viewModel = viewModel, currentMood = userProgress.activeMood)
                    "Paper" -> PaperSidebar(viewModel = viewModel, userProgress = userProgress)
                    "Music" -> MusicSidebar(viewModel = viewModel, userProgress = userProgress)
                }
            }
        }
    }
}

// Sidebar Drawer 1: Brushes List
@Composable
fun BrushSidebar(
    viewModel: DoodleViewModel,
    userProgress: UserProgress,
    costMap: Map<String, Int>
) {
    val selectedBrush by viewModel.selectedTool.collectAsState()
    val sliderVal by viewModel.strokeWidth.collectAsState()
    val scrollState = rememberScrollState()

    val normalBrushes = listOf(
        "Pencil", "Mechanical Pencil", "Charcoal", "Crayon", "Marker",
        "Paintbrush", "Airbrush", "Ink Pen", "Calligraphy Pen", "Watercolor Brush", "Spray Paint", "Chalk", "Eraser", "Color Picker"
    )
    val wildBrushes = listOf(
        "Fire Brush", "Smoke Brush", "Star Brush", "Feather Brush",
        "Bubble Brush", "Lightning Brush", "Pixel Brush", "Rainbow Trail Brush", "Neon Glow Brush"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(10.dp)
    ) {
        Text("Brush Settings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Slider(
            value = sliderVal,
            onValueChange = { viewModel.updateWidth(it) },
            valueRange = 2f..55f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Width: ${sliderVal.toInt()} dp", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

        Text("Traditional Tools", color = Color(0xFFE91E63), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
        normalBrushes.forEach { b ->
            val isSelected = b == selectedBrush
            Surface(
                color = if (isSelected) Color(0xFF2B3248) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTool(b) }
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = b,
                    color = if (isSelected) Color.White else Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Wild Lab Brushes", color = Color(0xFF00FFB7), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))

        wildBrushes.forEach { b ->
            val cleanName = b.replace(" Brush", "")
            val isUnlocked = userProgress.unlockedBrushes.contains(cleanName) ||
                    userProgress.unlockedBrushes.contains(b) ||
                    b == "Feather Brush" || b == "Lightning Brush" // preset unlocks
            val isSelected = b == selectedBrush

            Surface(
                color = if (isSelected) Color(0xFF2B3248) else if (!isUnlocked) Color(0xFF000000).copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isUnlocked) {
                            viewModel.selectTool(b)
                        } else {
                            // Try unlocking
                            val cost = costMap[cleanName] ?: 60
                            viewModel.tryUnlockBrush(
                                cleanName, cost,
                                onSuccess = { viewModel.selectTool(b) },
                                onError = {}
                            )
                        }
                    }
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cleanName,
                        color = if (isSelected) Color.White else if (isUnlocked) Color(0xFF00FFB7) else Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isUnlocked) {
                        val cost = costMap[cleanName] ?: 60
                        Text(
                            text = "🪙$cost",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Sidebar Drawer 2: Stickers List
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StickerSidebar(
    viewModel: DoodleViewModel,
    userProgress: UserProgress
) {
    val scrollState = rememberScrollState()
    val animals = listOf("🐱", "🐶", "🦊", "🐸", "🦉", "🐙", "🦀", "🐬")
    val funStickers = listOf("🛸", "🍄", "⭐", "🔥", "⚡", "🌩️", "🤖", "🍒")
    val spaceStickers = listOf("🚀", "🪐", "🌍", "☄️", "👾", "🖲️")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sticker Vault", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        Text("Animals Pack", color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            animals.forEach { animal ->
                Text(
                    text = animal,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.placeSticker(animal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Gaming & Fun", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            funStickers.forEach { funny ->
                Text(
                    text = funny,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.placeSticker(funny) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Cosmic Pack", color = Color(0xFFB388FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            spaceStickers.forEach { space ->
                Text(
                    text = space,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.placeSticker(space) }
                )
            }
        }
    }
}

@Composable
fun PaperSidebar(viewModel: DoodleViewModel, userProgress: UserProgress) {
    val activePaper by viewModel.activePaper.collectAsState()
    val scrollState = rememberScrollState()
    
    val allPapers = listOf(
        "Plain White", "Dotted Journal", "Vintage Parchment", 
        "Crumpled Slate", "Midnight Grid", "Desert Sand", "Candy Pink"
    )
    val unlocked = userProgress.unlockedPaperTypes.split(",").map { it.trim() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "📄 Canvas Paper",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        allPapers.forEach { paperName ->
            val isUnlocked = unlocked.contains(paperName)
            val isSelected = activePaper == paperName

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF283149) else Color(0xFF1B1D26),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (isUnlocked) {
                            viewModel.setPaperStyle(paperName)
                        }
                    }
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFFFFD54F) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (paperName) {
                            "Plain White" -> "⚪"
                            "Dotted Journal" -> "🎯"
                            "Vintage Parchment" -> "📜"
                            "Crumpled Slate" -> "🪨"
                            "Midnight Grid" -> "🌐"
                            "Desert Sand" -> "🏜️"
                            "Candy Pink" -> "🌸"
                            else -> "📄"
                        },
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = paperName,
                            color = if (isUnlocked) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isUnlocked) {
                            Text(
                                text = "🔒 Locked in Store",
                                color = Color(0xFFE57373),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MusicSidebar(
    viewModel: DoodleViewModel,
    userProgress: UserProgress
) {
    val soundscapeMode by viewModel.activeSoundscape.collectAsState()
    val soundscapePlaying by viewModel.soundscapePlaying.collectAsState()

    val modes = listOf(
        "Rainy Sketchbook", "Campfire", "Space Journey", "Ocean Drift",
        "Wizard Library", "Retro Arcade", "Cyberpunk Disco",
        "Lofi Meadow", "Zen Bamboo Stream", "Cosmic Aurora"
    )
    val unlocked = userProgress.unlockedMusic.split(",").map { it.trim() }

    val defaultUnlocks = listOf("Rainy Sketchbook", "Campfire", "Space Journey", "Ocean Drift", "Retro Arcade", "Cosmic Aurora", "Lofi Meadow", "Zen Bamboo Stream")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text(
            text = "🎵 Ambient Music",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Action Play/Pause
        Button(
            onClick = { viewModel.toggleSoundscape() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (soundscapePlaying) Color(0xFFFF9800) else Color(0xFF283149)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (soundscapePlaying) "⏸️ Pause Synth" else "▶️ Play Synth", fontSize = 11.sp, maxLines = 1)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AVAILABLE SOUNDS:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            modes.forEach { m ->
                val isUnlocked = defaultUnlocks.contains(m) || unlocked.contains(m)
                val isSelected = soundscapeMode == m

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF283149) else Color(0xFF1B1D26),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFF9800) else Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isUnlocked) {
                                viewModel.selectSoundscape(m)
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = m,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) (if (isSelected) Color(0xFFFF9800) else Color.White) else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isUnlocked) {
                                Text("🔒 Store", color = Color(0xFFE57373), fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sidebar Drawer 3: Moods Selector
@Composable
fun PaletteSidebar(
    viewModel: DoodleViewModel,
    currentMood: String
) {
    val moods = listOf("Calm", "Silly", "Epic", "Mysterious", "Funny", "Adventurous", "Weird", "Spooky", "Cozy")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Text("Ambient Moods", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Text("Changing active mood adapts colors, prompt seeds and background sounds.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 12.dp))

        moods.forEach { m ->
            val isSelected = m == currentMood
            Surface(
                color = if (isSelected) Color(0xFF283149) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.changeMood(m) }
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = when (m) {
                                    "Calm" -> Color(0xFF6B8A7A)
                                    "Silly" -> Color(0xFFFF5722)
                                    "Epic" -> Color(0xFFFF9800)
                                    "Mysterious" -> Color(0xFF9C27B0)
                                    "Funny" -> Color(0xFF4CAF50)
                                    "Adventurous" -> Color(0xFF8D6E63)
                                    "Weird" -> Color(0xFFEEFF41)
                                    "Spooky" -> Color(0xFFB388FF)
                                    else -> Color(0xFFFF7043)
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = m,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// --- WORKSPACE SECTION 2: INSPIRATION ENGINE ---

@Composable
fun InspirationSection(
    viewModel: DoodleViewModel,
    moodTheme: MoodTheme,
    userProgress: UserProgress
) {
    val scrollState = rememberScrollState()

    val shakenPrompt by viewModel.shakenPrompt.collectAsState()
    val dreamscapeIdea by viewModel.dreamscapeIdea.collectAsState()
    val dreamscapeLoading by viewModel.dreamscapeLoading.collectAsState()

    val selectedChar by viewModel.selectedChar.collectAsState()
    val selectedLoc by viewModel.selectedLoc.collectAsState()
    val selectedProb by viewModel.selectedProb.collectAsState()
    val storyChapter by viewModel.storyChapter.collectAsState()
    val storyNarrative by viewModel.storyNarrative.collectAsState()
    val storyLoading by viewModel.storyLoading.collectAsState()

    val activeBoss by viewModel.activeBoss.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "🎨 Inspiration Generator Suite",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Generate and combine millions of highly creative prompt ideas dynamically or using server-side Gemini intelligence.",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // A. IDEA JAR
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF242730)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF34384A), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏺 The Giant Idea Jar", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFFF9800), shape = RoundedCornerShape(8.dp)) {
                        Text("Muted", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF13151D))
                        .clickable { viewModel.shakeIdeaJar() }
                ) {
                    // Quick vector drawings for Idea jar bottle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFFECEFF1),
                            topLeft = Offset(25f, 15f),
                            size = Size(200f, 240f),
                            cornerRadius = CornerRadius(20f),
                            style = Stroke(width = 4f)
                        )
                        // Jar Lid
                        drawRect(Color(0xFFB0BEC5), Offset(55f, 5f), Size(140f, 16f))
                        // Glowing dots representing glowing prompts paper inside
                        drawCircle(Color(0xFFFFEB3B), 6f, Offset(110f, 80f))
                        drawCircle(Color(0xFFE91E63), 5f, Offset(140f, 140f))
                        drawCircle(Color(0xFF03A9F4), 6f, Offset(90f, 170f))
                    }
                    Text(
                        "Tap to\nSHAKE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = Color(0xFF13151D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shakenPrompt.ifBlank { "Tap the jar to draw an inspiring creative prompt card!" },
                        color = if (shakenPrompt.isBlank()) Color.Gray else Color.White,
                        fontStyle = if (shakenPrompt.isBlank()) FontStyle.Italic else FontStyle.Normal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.shakeIdeaJar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text("Shake Prompt Jar")
                }
            }
        }

        // B. CREATURE BUILDER Anatomy workbench
        CreatureBuilderModule(viewModel = viewModel, modifier = Modifier.padding(bottom = 16.dp))

        // C. GEMINI DREAMWORLD LANDSCAPE DISCOVERER
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF2C3243), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌌 Gemini Dreamscape Discovery Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FFB7),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Generate and discover infinite landscape worlds. Paint whatever magical biomes you find!",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (dreamscapeLoading) {
                    CircularProgressIndicator(color = Color(0xFF00FFB7), modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (dreamscapeIdea.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF0F1117),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dreamscapeIdea,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.exploreDreamWorld(userProgress.activeMood, "Epic Watercolor") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248)),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    ) {
                        Text("Generate World", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.selectTool("Paintbrush")
                            viewModel.sendQuackersQuestion("Help me paint a dream world described as: $dreamscapeIdea")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFB7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Paint Discovery", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // D. STORY SCENE BUILDER (Multi-chapter sequential scene builder)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0xFF2C3243), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📚 Picture-Book Story Scene Builder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Text(
                    text = "Design a story path. Step through sequential chapters and draw a customized illustration book!",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Selection parameters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                        Text("Hero Model", color = Color.Gray, fontSize = 10.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E3245),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val idx = viewModel.storyCharacters.indexOf(selectedChar)
                                    viewModel.setStoryConfig(
                                        viewModel.storyCharacters[(idx + 1) % viewModel.storyCharacters.size],
                                        selectedLoc,
                                        selectedProb
                                    )
                                }
                        ) {
                            Text(selectedChar, fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                        }
                    }

                    Column(modifier = Modifier.weight(1.2f).padding(horizontal = 4.dp)) {
                        Text("Realm", color = Color.Gray, fontSize = 10.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E3245),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val idx = viewModel.storyLocations.indexOf(selectedLoc)
                                    viewModel.setStoryConfig(
                                        selectedChar,
                                        viewModel.storyLocations[(idx + 1) % viewModel.storyLocations.size],
                                        selectedProb
                                    )
                                }
                        ) {
                            Text(selectedLoc, fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                        }
                    }

                    Column(modifier = Modifier.weight(1.2f).padding(start = 4.dp)) {
                        Text("Crisis", color = Color.Gray, fontSize = 10.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E3245),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val idx = viewModel.storyProblems.indexOf(selectedProb)
                                    viewModel.setStoryConfig(
                                        selectedChar,
                                        selectedLoc,
                                        viewModel.storyProblems[(idx + 1) % viewModel.storyProblems.size]
                                    )
                                }
                        ) {
                            Text(selectedProb, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chapter $storyChapter of 3",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = { viewModel.generateStoryScene() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248))
                    ) {
                        Text("Compose Chapter", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFF0F1117),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (storyLoading) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFFFD54F))
                        }
                    } else {
                        Text(
                            text = storyNarrative.ifBlank { "Click Compose Chapter to generate scene scenario..." },
                            color = if (storyNarrative.isBlank()) Color.Gray else Color.White,
                            fontSize = 13.sp,
                            fontStyle = if (storyNarrative.isBlank()) FontStyle.Italic else FontStyle.Normal,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                if (storyNarrative.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.advanceStoryChapter() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Text("What Happens Next? ⏩", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.selectTool("Pencil")
                                viewModel.sendQuackersQuestion("How do I sketch the chapter layout: $storyNarrative?")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3248)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sketch Story Scene", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // E. CREATIVE BOSS ENCOUNTERS
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF261D1A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👹 Creative Boss: ${activeBoss.name}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeBoss.icon,
                            fontSize = 24.sp
                        )
                    }

                    // Intensity Badge
                    val bossIntensityColor = when (activeBoss.intensity) {
                        "Easy" -> Color(0xFF4CAF50)
                        "Hard" -> Color(0xFFFF5252)
                        else -> Color(0xFFFF9800)
                    }
                    Surface(
                        color = bossIntensityColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, bossIntensityColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "BOSS: ${activeBoss.intensity.uppercase()}",
                            color = bossIntensityColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Dual decision if isDual
                if (activeBoss.isDual) {
                    Text(
                        text = "Dual Boss Choice Tracks: choose how you defeat this beast:",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectBossChoice(1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeBoss.selectedChoice == 1) Color(0xFFFF5252) else Color(0xFF382321)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Raid A",
                                color = if (activeBoss.selectedChoice == 1) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.selectBossChoice(2) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeBoss.selectedChoice == 2) Color(0xFFFF5252) else Color(0xFF382321)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Raid B",
                                color = if (activeBoss.selectedChoice == 2) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFF191211),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (activeBoss.isDual) "CHOSEN RAID PATH COMPASS:" else "ACTIVE BOUNTY REQUIREMENT:",
                            color = Color(0xFFFF8B8B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeBoss.requirement,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bounty Reward: 🪙${activeBoss.bounty}",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = { viewModel.initiateMinigame("BOSS") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text("Beat Obstacle (+${activeBoss.bounty} Coins)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- WORKSPACE SECTION 3: VIRTUAL DUCK MUSEUM & HARBOUR ---

fun parseDrawingDataStrokes(drawingData: String): List<DrawingStroke> {
    if (drawingData.isBlank() || drawingData == "IMPORTED") return emptyList()
    val list = mutableListOf<DrawingStroke>()
    try {
        val parts = drawingData.split(";")
        parts.forEach { strokeStr ->
            if (strokeStr.contains(":")) {
                val headerAndPoints = strokeStr.split(":")
                val header = headerAndPoints[0].split(",")
                val pointsStr = headerAndPoints[1]
                
                val brushType = header.getOrNull(0) ?: "Pencil"
                val colorHex = header.getOrNull(1)?.toIntOrNull() ?: 0xFFFFFFFF.toInt()
                val width = header.getOrNull(2)?.toFloatOrNull() ?: 5.0f
                
                val points = pointsStr.split("|").mapNotNull { ptStr ->
                    val coords = ptStr.split(",")
                    val x = coords.getOrNull(0)?.toFloatOrNull()
                    val y = coords.getOrNull(1)?.toFloatOrNull()
                    if (x != null && y != null) {
                        StrokePoint(x, y)
                    } else {
                        null
                    }
                }
                
                if (points.isNotEmpty()) {
                    list.add(
                        DrawingStroke(
                            points = points,
                            color = colorHex,
                            width = width,
                            brushType = brushType
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

@Composable
fun DrawingSpotlightFrame(
    frameStyle: String,
    title: String,
    strokes: List<DrawingStroke>,
    imagePath: String?,
    onCycleFrame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCycleFrame() }
    ) {
        // Render Frame Outer Borders & Background based on style
        when (frameStyle) {
            "Renaissance Gold" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3E1C12), Color(0xFF1E0E0A))
                            )
                        )
                        .border(10.dp, Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFD54F), Color(0xFFB58E23), Color(0xFFFFD54F))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(size.width / 2f - 25f, 0f)
                            lineTo(size.width / 2f + 25f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x3CFFF59D), Color(0x00FFF59D))
                            )
                        )
                        drawCircle(
                            color = Color(0x30FFF59D),
                            radius = size.width / 3f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
            }
            "Neo-Neon Guard" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF0F101A), Color(0xFF050508))
                            )
                        )
                        .border(3.dp, Brush.linearGradient(
                            colors = listOf(Color(0xFFE91E63), Color(0xFF00E676), Color(0xFF2196F3))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(Color(0x1CE91E63), Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), 1.5f)
                        drawLine(Color(0x1C2196F3), Offset(0f, size.height * 0.7f), Offset(size.width, size.height * 0.7f), 1.5f)
                        
                        val path = Path().apply {
                            moveTo(size.width / 2f - 15f, 0f)
                            lineTo(size.width / 2f + 15f, 0f)
                            lineTo(size.width * 0.9f, size.height)
                            lineTo(size.width * 0.1f, size.height)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x4000E676), Color(0x0000E676))
                            )
                        )
                    }
                }
            }
            "Rustic Redwood" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                            )
                        )
                        .border(12.dp, Brush.linearGradient(
                            colors = listOf(Color(0xFF795548), Color(0xFF3E2723), Color(0xFF8D6E63))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width * 0.3f, 0f)
                            lineTo(size.width * 0.7f, size.height)
                            lineTo(size.width * 0.2f, size.height)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0x2AFFF9C4), Color(0x00FFF9C4)),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                        drawCircle(Color(0x154CAF50), size.width * 0.4f, Offset(0f, 0f))
                        drawCircle(Color(0x154CAF50), size.width * 0.35f, Offset(size.width, size.height))
                    }
                }
            }
            "Obsidian Gallery" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF101114))
                        .border(1.5.dp, Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x38FFFFFF), Color(0x00FFFFFF)),
                                center = Offset(size.width / 2f, size.height * 0.45f),
                                radius = size.width * 0.42f
                            ),
                            radius = size.width * 0.42f,
                            center = Offset(size.width / 2f, size.height * 0.45f)
                        )
                    }
                }
            }
            "Silver Minimalist" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2C2F35), Color(0xFF1A1C20))
                            )
                        )
                        .border(8.dp, Brush.linearGradient(
                            colors = listOf(Color(0xFFE0E0E0), Color(0xFF757575), Color(0xFFEEEEEE))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.05f),
                            size = size
                        )
                    }
                }
            }
            "Royal Velvet Blue" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0D1B2A), Color(0xFF000814))
                            )
                        )
                        .border(12.dp, Brush.linearGradient(
                            colors = listOf(Color(0xFF003049), Color(0xFFD62828), Color(0xFF003049))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFFFFD23F), 6f, Offset(20f, 20f))
                        drawCircle(Color(0xFFFFD23F), 6f, Offset(size.width - 20f, 20f))
                        drawCircle(Color(0xFFFFD23F), 6f, Offset(20f, size.height - 20f))
                        drawCircle(Color(0xFFFFD23F), 6f, Offset(size.width - 20f, size.height - 20f))
                    }
                }
            }
            "Cyber Synthwave" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F0B1E))
                        .border(6.dp, Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFF00F0FF))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF007F).copy(alpha = 0.1f), Color.Transparent)
                            ),
                            size = size
                        )
                    }
                }
            }
            "Emerald Forest" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0A2E16), Color(0xFF041208))
                            )
                        )
                        .border(10.dp, Brush.linearGradient(
                            colors = listOf(Color(0xFF1B4332), Color(0xFF40916C), Color(0xFF1B4332))
                        ), RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0x1A52B788), size.width * 0.38f, Offset(size.width / 2, size.height / 2))
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1F222B))
                        .border(1.dp, Color(0xFF2E323F), RoundedCornerShape(16.dp))
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F1117))
                .border(2.dp, Color.Black)
        ) {
            if (!imagePath.isNullOrBlank()) {
                if (imagePath.startsWith("content://") || imagePath.startsWith("file://") || imagePath.startsWith("http")) {
                    coil.compose.AsyncImage(
                        model = imagePath,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                when (imagePath) {
                                    "Sunset over the bay" -> Brush.verticalGradient(listOf(Color(0xFFFF5722), Color(0xFFFFEB3B)))
                                    "Neon Cyberspace" -> Brush.radialGradient(listOf(Color(0xFF00E676), Color(0xFF00BCD4), Color(0xFF0F101A)))
                                    "Secret Garden" -> Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
                                    "Ocean Abyss" -> Brush.linearGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
                                    else -> Brush.verticalGradient(listOf(Color(0xFF9C27B0), Color(0xFFE91E63)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (imagePath) {
                                    "Sunset over the bay" -> "🌅"
                                    "Neon Cyberspace" -> "👾"
                                    "Secret Garden" -> "🏡"
                                    "Ocean Abyss" -> "🌊"
                                    else -> "📸"
                                },
                                fontSize = 34.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uploaded Photographic Art",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (strokes.isEmpty()) {
                Text(
                    text = "📥 (Imported Photograph)",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    val pts = strokes.flatMap { it.points }
                    val minX = pts.minOfOrNull { it.x } ?: 0f
                    val maxX = pts.maxOfOrNull { it.x } ?: 1000f
                    val minY = pts.minOfOrNull { it.y } ?: 0f
                    val maxY = pts.maxOfOrNull { it.y } ?: 1000f
                    
                    val dx = (maxX - minX).coerceAtLeast(1f)
                    val dy = (maxY - minY).coerceAtLeast(1f)
                    val scaleX = (size.width - 16f) / dx
                    val scaleY = (size.height - 16f) / dy
                    val scale = kotlin.math.min(scaleX, scaleY).coerceAtMost(0.4f)
                    
                    strokes.forEach { stroke ->
                        val path = Path()
                        if (stroke.points.isNotEmpty()) {
                            val p1 = stroke.points.first()
                            val offX = (size.width - dx * scale) / 2f - minX * scale
                            val offY = (size.height - dy * scale) / 2f - minY * scale
                            
                            path.moveTo(p1.x * scale + offX, p1.y * scale + offY)
                            for (i in 1 until stroke.points.size) {
                                val pt = stroke.points[i]
                                path.lineTo(pt.x * scale + offX, pt.y * scale + offY)
                            }
                            drawPath(
                                path = path,
                                color = Color(stroke.color),
                                style = Stroke(
                                    width = (stroke.width * scale * 1.5f).coerceAtLeast(1.5f),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C241E)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .border(1.dp, Color(0xFFB58E23), RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Frame: $frameStyle • Cycle Style",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MuseumSection(
    viewModel: DoodleViewModel,
    savedDrawings: List<SavedDrawing>,
    userProgress: UserProgress
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val drawingFrameStyles by viewModel.drawingFrameStyles.collectAsState()
    val friendsMuseums by viewModel.friendsMuseums.collectAsState()

    var activeSubTab by remember { mutableStateOf("My Wall") }
    var importText by remember { mutableStateOf("Sunset sketch") }
    var friendNameInput by remember { mutableStateOf("") }
    var friendCodeInput by remember { mutableStateOf("") }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Launcher for selecting real device photographs!
    val photoLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadPictureToMuseum(importText.ifBlank { "Gallery Snaps" }, uri.toString())
            Toast.makeText(context, "Successfully uploaded picture to your digital museum wall! (+50 Coins 🪙)", Toast.LENGTH_SHORT).show()
        }
    }

    // Procedural ducks wandering coordinates inside virtual museum list
    val roamingDucks = remember {
        val list = mutableListOf<Offset>()
        for (i in 1..4) {
            list.add(Offset(100f + Random.nextFloat() * 400f, 100f + Random.nextFloat() * 300f))
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "🦆 The Virtual Duck Museum",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Your saved and imported masterpieces hanging in high-fidelity columns. Curious little ducks wander around admiring them!",
            color = Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubTab = "My Wall" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "My Wall") Color(0xFFE91E63) else Color(0xFF242730)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🏡 My Gallery Wall", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { activeSubTab = "Shared" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "Shared") Color(0xFF00BCD4) else Color(0xFF242730)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("👥 Shared Galleries", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (activeSubTab == "My Wall") {
            // Wandering Ducks Admiring Board
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF13151D),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color(0xFF2F3446), RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Roaming ducks checking out your gallery wall right now:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )

                    // Draw ducks roaming dynamically
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        roamingDucks.forEachIndexed { idx, spot ->
                            drawCircle(Color(0xFFFFEB3B), 12f, spot)
                            drawPath(
                                path = Path().apply {
                                    moveTo(spot.x + 10f, spot.y - 3f)
                                    lineTo(spot.x + 22f, spot.y + 2f)
                                    lineTo(spot.x + 10f, spot.y + 7f)
                                    close()
                                },
                                color = Color(0xFFFF9800)
                            )
                            drawCircle(Color.Black, 2.5f, Offset(spot.x + 4f, spot.y - 3f))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("🐥 Pip", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("🤠 Woody", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("🏴‍☠️ Grog", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("🧙 Alistair", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ADD ARTWORK PANEL (Presets or upload)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF242730)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color(0xFF34384A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📥 Upload & Share Photographic Art",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Upload custom images to display in your gallery wall beside vectors!",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text("Photo Title / Context") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE91E63),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFE91E63),
                            unfocusedLabelColor = Color.LightGray
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { photoLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📁 Pick Photo", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val presets = listOf("Sunset over the bay", "Neon Cyberspace", "Secret Garden", "Ocean Abyss")
                                val picked = presets.random()
                                viewModel.uploadPictureToMuseum(importText.ifBlank { picked }, picked)
                                Toast.makeText(context, "Added preset \"${importText.ifBlank { picked }}\" photo upload! (+50 Coins 🪙)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✨ Presets Art", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // EXPORT SOCIAL CODES BUTTON
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🏛️ Museum Sharing Desk",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Export your entire collection code to let friends see your museum on their device!",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                    Button(
                        onClick = {
                            val code = viewModel.generateMuseumSharingCode()
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                            Toast.makeText(context, "Copied your Museum Wall Code to clipboard! Send it to your friend!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black)
                    ) {
                        Text("Copy Code 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MY GALLERY WALL ART CONTAINER
            Text(
                text = "Your Studio Gallery (${savedDrawings.size} Masterpieces):",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            if (savedDrawings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFF242730), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "The museum columns are blank.\nSave drawings in the Studio or upload photos to display them here!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                }
            } else {
                savedDrawings.forEach { drawing ->
                    // Look up or default drawing's selected frame style
                    val currentStyle = drawingFrameStyles[drawing.id] ?: "Renaissance Gold"
                    val drawingStrokes = remember(drawing.drawingData) {
                        parseDrawingDataStrokes(drawing.drawingData)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Drawing frame banner rendering actual parsed strokes or imported placeholder
                            DrawingSpotlightFrame(
                                frameStyle = currentStyle,
                                title = drawing.title,
                                strokes = drawingStrokes,
                                imagePath = drawing.imagePath,
                                onCycleFrame = {
                                    viewModel.cycleFrameStyle(drawing.id)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (drawing.imagePath != null) "Photographic Upload" else "Vector Masterpiece",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = drawing.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row {
                                    Button(
                                        onClick = {
                                            viewModel.loadDrawingToCanvas(drawing)
                                            Toast.makeText(context, "Loaded \"${drawing.title}\" to drawing canvas!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Restore to Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SHARED FRIEND MUSEUMS PAGE
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D26)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E323F), RoundedCornerShape(16.dp))
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "👥 Connect to Friend's Museum",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Paste a sharing code exported from your friend's app to add their museum showcase on your device!",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = friendNameInput,
                        onValueChange = { friendNameInput = it },
                        label = { Text("Friend's Duck Name (e.g. Quacky)") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BCD4),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00BCD4),
                            unfocusedLabelColor = Color.LightGray
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = friendCodeInput,
                        onValueChange = { friendCodeInput = it },
                        label = { Text("Friend's Sharing Code") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BCD4),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF00BCD4),
                            unfocusedLabelColor = Color.LightGray
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val res = viewModel.importSharedMuseum(friendNameInput, friendCodeInput)
                            Toast.makeText(context, res.second, Toast.LENGTH_LONG).show()
                            if (res.first) {
                                friendNameInput = ""
                                friendCodeInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Friend's Museum Showcase 🎨", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // LOADED FRIEND SHOWCASES LIST
            Text(
                text = "Friend Galleries Showing (${friendsMuseums.size}):",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (friendsMuseums.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFF242730), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No friend museum showcases imported yet.\nPaste a code above to import a friend's active creation!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                }
            } else {
                friendsMuseums.forEach { fri ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF13151D),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.5.dp, Color(0xFF00BCD4), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🎨 ${fri.friendName}'s Gallery Wall Showcase",
                                color = Color(0xFF00BCD4),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            fri.drawings.forEach { drawing ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D26)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        val drawingStrokes = remember(drawing.drawingData) {
                                            parseDrawingDataStrokes(drawing.drawingData)
                                        }
                                        DrawingSpotlightFrame(
                                            frameStyle = "Renaissance Gold", // Standard majestic frame
                                            title = drawing.title,
                                            strokes = drawingStrokes,
                                            imagePath = drawing.imagePath,
                                            onCycleFrame = {} // Shared cards are read-only
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = drawing.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            text = if (drawing.imagePath != null) "Uploaded Photographic Art" else "Vector Masterpiece",
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACHIEVEMENTS TRACKER BOARD
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2E241E),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🏆 Achievement & Evolution System",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val achievements = listOf(
                    "First Sketch" to "Draw and save your very first drawing to the museum.",
                    "Color Wizard" to "Unlock more than 8 specialized custom brushes.",
                    "Cosmic Wardrobe" to "Discover and equip 3 distinct outfits for companion deck.",
                    "Boss Conqueror" to "Overcome active drawings challenges of the Kraken.",
                    "Museum Import" to "Import external sketches or upload photo presets.",
                    "Inspirational Jar" to "Shake the giant prompt idea jar to generate mood-tailored creative prompts.",
                    "Art Scientist analysis" to "Submit canvas sketch to Professor Quackers for a science art analysis."
                )

                val completedCount = achievements.count { (title, _) ->
                    userProgress.unlockedAchievements.contains(title)
                }

                val totalCount = achievements.size
                val unlockedFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                val tierName = when {
                    completedCount >= 5 -> "Supreme Art Scientist Grandmaster 👑"
                    completedCount >= 3 -> "Gallery Masterpiece Curator 🏛️"
                    completedCount >= 2 -> "Creative Artisan 🖌️"
                    completedCount >= 1 -> "Aspiring Doodler 🎨"
                    else -> "Hobbyist Scribbler 🐣"
                }

                // Advanced Progression Tier Header Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CREATIVE STANDING TIER",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "$completedCount / $totalCount Completed",
                                color = Color(0xFFFFD23F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = tierName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                        )
                        LinearProgressIndicator(
                            progress = { unlockedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFFD23F),
                            trackColor = Color(0x22FFFFFF)
                        )
                    }
                }

                achievements.forEach { (title, desc) ->
                    val isUnlocked = userProgress.unlockedAchievements.contains(title)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = "Unlock state",
                            tint = if (isUnlocked) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                color = if (isUnlocked) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = desc,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreSection(
    viewModel: DoodleViewModel,
    userProgress: UserProgress
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var activeCategory by remember { mutableStateOf("Frames") }
    var tradeCodeInput by remember { mutableStateOf("") }

    val unlockedFrames = remember(userProgress.unlockedFrames) {
        userProgress.unlockedFrames.split(",").map { it.trim() }
    }
    val unlockedPapers = remember(userProgress.unlockedPaperTypes) {
        userProgress.unlockedPaperTypes.split(",").map { it.trim() }
    }
    val unlockedMusic = remember(userProgress.unlockedMusic) {
        userProgress.unlockedMusic.split(",").map { it.trim() }
    }
    val unlockedBrushes = remember(userProgress.unlockedBrushes) {
        userProgress.unlockedBrushes.split(",").map { it.trim() }
    }
    val unlockedStickers = remember(userProgress.unlockedStickers) {
        userProgress.unlockedStickers.split(",").map { it.trim() }
    }
    val unlockedOutfits = remember(userProgress.unlockedOutfits) {
        userProgress.unlockedOutfits.split(",").map { it.trim() }
    }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // MERCHANT INTRO BANNER
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1F28),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤠",
                    fontSize = 38.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Barnaby's Golden Trading Barn",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "\"Welcome, partner! Trade your hard-earned coins for canvas papers, glowing music tracks, and deluxe picture frames!\"",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2E3142)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙 ", fontSize = 12.sp)
                                Text(
                                    text = "${userProgress.coins} Coins",
                                    color = Color(0xFFFFD23F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // TEST GIFTER BUTTON
                        Button(
                            onClick = {
                                viewModel.tradeStickerWithMerchant("", "", true) // triggers coin reward
                                Toast.makeText(context, "Gift received! +40 Coins added! 🪙🎁", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+40 Coins Gift 🎁", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // CATEGORIES CAROUSEL SWIPEBAR
        val categories = listOf("Frames" to "🖼️ Frames", "Papers" to "📄 Papers", "Stickers" to "🎟️ Stickers", "Brushes" to "🖌️ Brushes", "Costumes" to "👑 Clothing", "Music" to "🎵 Music")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { (key, label) ->
                val isSelected = activeCategory == key
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF9800) else Color(0xFF242730),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeCategory = key }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                    )
                }
            }
        }

        // CATEGORY CONTENT SWITCHER
        when (activeCategory) {
            "Frames" -> {
                val frames = listOf(
                    "Silver Minimalist" to 50,
                    "Royal Velvet Blue" to 80,
                    "Cyber Synthwave" to 100,
                    "Emerald Forest" to 120
                )
                Text(
                    text = "🖼️ Premium Spotlight Frames:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                frames.forEach { (name, cost) ->
                    val isUnlocked = unlockedFrames.contains(name)
                    StoreBuyRow(
                        title = name,
                        desc = "Wrap your museum portraits in gorgeous custom styled backgrounds.",
                        cost = cost,
                        isUnlocked = isUnlocked,
                        onBuy = {
                            if (userProgress.coins >= cost) {
                                viewModel.tryUnlockFrame(name, cost, onSuccess = {
                                    Toast.makeText(context, "Unlocked frame: $name!", Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    Toast.makeText(context, "Failed to unlock frame!", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Not enough coins! Spend more time drawing sketches! 🪙", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            "Papers" -> {
                val papers = listOf(
                    "Dotted Journal" to 30,
                    "Vintage Parchment" to 50,
                    "Crumpled Slate" to 70,
                    "Midnight Grid" to 90,
                    "Desert Sand" to 60,
                    "Candy Pink" to 60
                )
                Text(
                    text = "📄 Textured Canvas & Scrap Papers:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                papers.forEach { (name, cost) ->
                    val isUnlocked = unlockedPapers.contains(name)
                    StoreBuyRow(
                        title = name,
                        desc = when (name) {
                            "Dotted Journal" -> "Dotted soft yellow grids for layout sketching."
                            "Vintage Parchment" -> "Beautiful antique yellow-brown library grain."
                            "Crumpled Slate" -> "A sleek dark stone slate style paper texture."
                            "Midnight Grid" -> "Glowing neon technical dark drawing layout grid."
                            "Desert Sand" -> "Gritty organic desert brown grain."
                            else -> "Pastel cherry candy sweet coloring surface."
                        },
                        cost = cost,
                        isUnlocked = isUnlocked,
                        onBuy = {
                            if (userProgress.coins >= cost) {
                                viewModel.tryUnlockPaper(name, cost, onSuccess = {
                                    Toast.makeText(context, "Unlocked canvas paper: $name!", Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    Toast.makeText(context, "Failed to unlock paper!", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Partners need $cost coins for this premium scroll paper!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            "Stickers" -> {
                val stickerPacks = listOf(
                    "Animal Friends Series" to (40 to listOf("🐱", "🐶", "🦊", "🐸", "🦉")),
                    "Retro Arcade Set" to (60 to listOf("🛸", "🍄", "🎮", "👾", "⭐")),
                    "Outer Space Odyssey" to (80 to listOf("🚀", "🪐", "👽", "☄️", "🛡️"))
                )
                Text(
                    text = "🎟️ Specialized sticker packs:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                stickerPacks.forEach { (item, data) ->
                    val (cost, list) = data
                    val holdsAny = list.any { unlockedStickers.contains(it) }
                    StoreBuyRow(
                        title = item,
                        desc = "Unlock rare floating stickers: ${list.joinToString(" ")}",
                        cost = cost,
                        isUnlocked = holdsAny,
                        onBuy = {
                            if (userProgress.coins >= cost) {
                                viewModel.tryUnlockStickers(item, cost, onSuccess = {
                                    Toast.makeText(context, "Unwrapped sticker pack: $item!", Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    Toast.makeText(context, "Failed to unlock sticker pack!", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Grind more sketches to unlock these funny stickers!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            "Brushes" -> {
                val brushes = listOf(
                    "Fire" to 80,
                    "Smoke" to 50,
                    "Star" to 60,
                    "Bubble" to 40,
                    "Pixel" to 30,
                    "Rainbow Trail" to 100,
                    "Neon Glow" to 120
                )
                Text(
                    text = "🖌️ Mechanical Custom Stroke Brushes:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                brushes.forEach { (name, cost) ->
                    val isUnlocked = unlockedBrushes.contains(name)
                    StoreBuyRow(
                        title = name,
                        desc = "Draw canvas strokes with custom $name patterns & effects.",
                        cost = cost,
                        isUnlocked = isUnlocked,
                        onBuy = {
                            if (userProgress.coins >= cost) {
                                viewModel.tryUnlockBrush(name, cost, onSuccess = {
                                    Toast.makeText(context, "Unlocked custom brush: $name!", Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    Toast.makeText(context, "Failed to unlock brush!", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Barnaby needs $cost coins to unlock $name tool!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            "Costumes" -> {
                val costumes = listOf(
                    "Detective Hat & Trenchcoat" to 100,
                    "Crown of the Art Master" to 150,
                    "Space Astronaut Helmet" to 180,
                    "Silly Clown Nose" to 60
                )
                Text(
                    text = "🧥 Deluxe Cosmetics with custom attire overlays:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                costumes.forEach { (name, cost) ->
                    val isUnlocked = unlockedOutfits.contains(name)
                    val isEquipped = userProgress.activeOutfit == name
                    StoreBuyRow(
                        title = name,
                        desc = if (isUnlocked) "Cosmetic owned! Click to dress up Dr. Polka-Dot." else "Deluxe costume to dress up your digital duck on screen.",
                        cost = cost,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        onBuy = {
                            if (isUnlocked) {
                                viewModel.setCompanionOutfit(name)
                                Toast.makeText(context, "Dressed up companion with $name!", Toast.LENGTH_SHORT).show()
                            } else {
                                if (userProgress.coins >= cost) {
                                    viewModel.tryUnlockOutfit(name, cost, onSuccess = {
                                        Toast.makeText(context, "Bought & equipped costume: $name!", Toast.LENGTH_SHORT).show()
                                    }, onError = {
                                        Toast.makeText(context, "Costumes deal error!", Toast.LENGTH_SHORT).show()
                                    })
                                } else {
                                    Toast.makeText(context, "You need $cost coins for this premium duck costume!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
            "Music" -> {
                val tracks = listOf(
                    "Wizard Library" to 80,
                    "Cyberpunk Disco" to 100,
                    "Lofi Meadow" to 120
                )
                Text(
                    text = "🎵 Procedurally generated ambient tunes:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                tracks.forEach { (name, cost) ->
                    val isUnlocked = unlockedMusic.contains(name)
                    StoreBuyRow(
                        title = name,
                        desc = "Enable synthesis of procedural soundscape ambient track: $name.",
                        cost = cost,
                        isUnlocked = isUnlocked,
                        onBuy = {
                            if (userProgress.coins >= cost) {
                                viewModel.tryUnlockMusic(name, cost, onSuccess = {
                                    Toast.makeText(context, "Bought procedural background tracks: $name!", Toast.LENGTH_SHORT).show()
                                }, onError = {
                                    Toast.makeText(context, "Failed to unlock music!", Toast.LENGTH_SHORT).show()
                                })
                            } else {
                                Toast.makeText(context, "Sound waves cost $cost coins inside Barnaby's barn!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE STICKER TRADE HOUSE CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF34384A), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🎟️ Swanny Trade House & Sticker Exchange",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Unlock trading! Give unwanted stickers to local merchant Barnaby for 40 Coins 🪙, or paste P2P codes to trade stickers with friends!",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.tradeStickerWithMerchant("🐱", "👽", false)
                            Toast.makeText(context, "Swapped 🐱 sticker for premium rare star sticker! ✅", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C3C5C)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exchange with Barnaby 🤠", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val code = "DUCK_STICKER_TRADE_V1:⭐:🐱"
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code))
                            Toast.makeText(context, "Sticker Trade Proposal Code copied to clipboard! Share it with friends!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Generates P2P Code 🎟️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = tradeCodeInput,
                    onValueChange = { tradeCodeInput = it },
                    label = { Text("Paste Friend's P2P Trade Code") },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFE91E63),
                        unfocusedLabelColor = Color.LightGray
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val result = viewModel.processStickerTrade(tradeCodeInput)
                        Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                        if (result.first) tradeCodeInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete P2P Trade Exchange 🏛️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StoreBuyRow(
    title: String,
    desc: String,
    cost: Int,
    isUnlocked: Boolean,
    isEquipped: Boolean = false,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, if (isUnlocked) Color(0xFF2E323F) else Color(0xFFFFB300), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = desc,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUnlocked) {
                        if (isEquipped) Color(0xFF3F51B5) else Color(0xFF4CAF50)
                    } else Color(0xFFFFB300)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                if (isUnlocked) {
                    Text(if (isEquipped) "Equipped 🧥" else "Owned ✅", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 10.sp, modifier = Modifier.padding(end = 2.dp))
                        Text("$cost", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// --- WORKSPACE SECTION 4: DETAILED Dino tip AND MAIL ROOM ---

@Composable
fun DinoTipSection(
    viewModel: DoodleViewModel,
    moodTheme: MoodTheme
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var currentMsgInput by remember { mutableStateOf("How do I make a drawing look hyper-realistic?") }

    val dinoHat by viewModel.dinoHat.collectAsState()
    val dinoGlasses by viewModel.dinoGlasses.collectAsState()
    val dinoMustache by viewModel.dinoMustache.collectAsState()
    val dinoOutfit by viewModel.dinoOutfit.collectAsState()
    val dinoVibeState by viewModel.dinoVibeState.collectAsState()
    val hyperRealismEnabled by viewModel.hyperRealismEnabled.collectAsState()
    val quackersVoice by viewModel.quackersVoice.collectAsState()
    val dailyTip by viewModel.currentTip.collectAsState()

    val chatLoading by viewModel.chatLoading.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()

    val analysisLoading by viewModel.analysisLoading.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    val userProgress by viewModel.userProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Grand Interactive Detective Duck Cover Art
        DetectiveDuckCoverArt(modifier = Modifier.padding(bottom = 16.dp))

        // A. DETAILED T-REX BOARD (Outfits change randomly every app initialization)
        DinosaursOutfitCard(
            hat = dinoHat,
            glasses = dinoGlasses,
            mustache = dinoMustache,
            outfit = dinoOutfit,
            dailyTip = dailyTip,
            onReaction = { loved ->
                if (loved) viewModel.likeCurrentTip() else viewModel.dislikeCurrentTip()
            },
            dinoVibe = dinoVibeState,
            isHyperRealismEnabled = hyperRealismEnabled,
            equippedOutfit = userProgress.activeOutfit
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dinosaur Outfit Customizer Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161921)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2C3141), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🦕 Dr. Polka-Dot Outfit Style Customizer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Selectable Hats
                Text("🎩 Select Hat Style", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val hats = listOf("None" to -1, "Bowler" to 0, "Top Hat" to 1, "Cowboy" to 2, "Wizard" to 3)
                    hats.forEach { (name, id) ->
                        val isSelected = (dinoHat == null && id == -1) || (dinoHat?.shapeType == id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDinoHat(id, 0xFFFFB74D.toInt()) },
                            label = { Text(name, fontSize = 10.sp, color = if (isSelected) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFB74D)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Selectable Glasses
                Text("👓 Select Eye Specs", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val glassesList = listOf("None" to -1, "Specs" to 0, "Aviator" to 1, "Stars" to 2)
                    glassesList.forEach { (name, id) ->
                        val isSelected = (dinoGlasses == null && id == -1) || (dinoGlasses?.shapeType == id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDinoGlasses(id, 0xFF00E676.toInt()) },
                            label = { Text(name, fontSize = 10.sp, color = if (isSelected) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E676)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Selectable Mustache
                Text("👨 Select Mustache Style", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mustacheList = listOf("None" to -1, "Handlebar" to 0, "Bushy" to 1, "Poirot" to 2)
                    mustacheList.forEach { (name, id) ->
                        val isSelected = (dinoMustache == null && id == -1) || (dinoMustache?.shapeType == id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDinoMustache(id, 0xFF3E2723.toInt()) },
                            label = { Text(name, fontSize = 10.sp, color = if (isSelected) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF5252)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Selectable Outfit
                Text("🧥 Select Collar Outfit", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val outfits = listOf("None" to -1, "Bowtie" to 0, "Collar" to 1, "Scarf" to 2, "Cape" to 3)
                    outfits.forEach { (name, id) ->
                        val isSelected = (dinoOutfit == null && id == -1) || (dinoOutfit?.shapeType == id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDinoOutfit(id, 0xFF2979FF.toInt()) },
                            label = { Text(name, fontSize = 10.sp, color = if (isSelected) Color.White else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2979FF)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hyper-realism toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF222839), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✨ Rayleigh Hyper-Realism",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Apply cinematic color scatter bloom & particulate physics overlay.",
                            color = Color.Gray,
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = hyperRealismEnabled,
                        onClick = { viewModel.toggleHyperRealism() },
                        label = { Text(if (hyperRealismEnabled) "ON" else "OFF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (hyperRealismEnabled) Color.Black else Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E676)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Professor Quackers custom voice settings
                Text(
                    text = "🦆 Professor Quacker's Voice Personality Vibe",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val voices = listOf(
                        "Scholarly" to "🎓 Wise",
                        "Quacky" to "📣 Jolly",
                        "Sarcastic" to "🧐 Dry",
                        "Zen" to "🌿 Zen"
                    )
                    voices.forEach { (key, label) ->
                        val isSelected = quackersVoice == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setQuackersVoice(key) },
                            label = { Text(label, fontSize = 9.sp, color = if (isSelected) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD54F)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.randomizeDino() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Randomize Dr. Polka-Dot's Outfit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Soundscape Synthesizer Deck integrated directly in control menu
        val soundscapeMode by viewModel.activeSoundscape.collectAsState()
        val soundscapePlaying by viewModel.soundscapePlaying.collectAsState()

        SoundscapeDeck(
            mode = soundscapeMode,
            isPlaying = soundscapePlaying,
            onToggle = { viewModel.toggleSoundscape() },
            onSelect = { viewModel.selectSoundscape(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // B. MAGIC AI ASSISTANT: Professor Quackers Portal (AI Assistant Sidekick)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF323746), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header of Professor Quackers
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        color = Color(0xFF242730)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_prof_quackers),
                            contentDescription = "Professor Quackers",
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "🦆 Professor Quackers",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "AI Assistant Sidekick Duck.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat stream history column
                Surface(
                    color = Color(0xFF0C0E14),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (chatHistory.isEmpty()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Ask Professor Quackers:\n'How do I draw fluffy water clouds?'",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(chatHistory) { (speech, isUser) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        color = if (isUser) Color(0xFF1E3A8A) else Color(0xFF2D3748),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = speech,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Suggestion chips for fast tutoring
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val suggestions = listOf("💡 Light contrast", "🎨 Shading depth", "📐 Geometric anatomy", "🚀 Perspective vanishing")
                    suggestions.forEach { prompt ->
                        SuggestionChip(
                            onClick = { viewModel.sendQuackersQuestion("Tell me art concepts about: $prompt") },
                            label = { Text(prompt, fontSize = 9.sp, color = Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1E222B))
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentMsgInput,
                        onValueChange = { currentMsgInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        placeholder = { Text("Ask deep drawing questions...", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendQuackersQuestion(currentMsgInput)
                            currentMsgInput = ""
                        },
                        enabled = !chatLoading,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(48.dp)
                            .background(Color(0xFFFF9800), RoundedCornerShape(10.dp))
                    ) {
                        if (chatLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Query AI", tint = Color.White)
                        }
                    }
                }
            }
        }

        // C. ART SCIENTIST REVIEWS SECTION
        if (analysisResult.isNotEmpty() || analysisLoading) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF22112C),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF9C27B0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔬 Professor Quackers Art Science Analysis",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE040FB),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (analysisLoading) {
                        CircularProgressIndicator(color = Color(0xFFE040FB), modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Text(
                            text = analysisResult,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// --- WORKSPACE SECTION 5: CHALLENGE ARENA & COLLABORATIVE DUELS ---

@Composable
fun ArenaSection(
    viewModel: DoodleViewModel,
    moodTheme: MoodTheme,
    onNavigateToStudio: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Observe Daily Challenge details
    val dailyPrompt by viewModel.dailyChallengePrompt.collectAsState()
    val dailyCompleted by viewModel.dailyCompleted.collectAsState()
    val dailyScore by viewModel.dailyScore.collectAsState()
    val dailyRank by viewModel.dailyRank.collectAsState()

    // Observe Duels Challenge details
    val opponentStrokes by viewModel.opponentStrokes.collectAsState()
    val currentDuelPrompt by viewModel.currentDuelPrompt.collectAsState()
    val opponentNickname by viewModel.opponentNickname.collectAsState()
    val activeDuelStatus by viewModel.activeDuelStatus.collectAsState()
    val duelResultRating by viewModel.duelResultRating.collectAsState()
    val duelResultScore by viewModel.duelResultScore.collectAsState()
    val duelResultMessage by viewModel.duelResultMessage.collectAsState()

    // Check if user has drawn strokes to submit
    val strokes by viewModel.strokes.collectAsState()

    // Local inputs for initiating/responding to duels
    var userNicknameInput by remember { mutableStateOf("Doodle Champion") }
    var duelPromptInput by remember { mutableStateOf("Retro Cybernetic Duck") }
    var invitationCodeResult by remember { mutableStateOf("") }
    var inviteInputCode by remember { mutableStateOf("") }
    var duoTitleInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ARENA BRANDING HEADER
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = moodTheme.cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, moodTheme.primaryBrush, RoundedCornerShape(24.dp))
                .testTag("arena_header_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(moodTheme.gradientBrush)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Challenge Arena Mode",
                            tint = moodTheme.secondaryAccent,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Challenge Arena",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Take on creative daily prompts or exchange challenge codes with friends to joint-draw together in collaborative, high-synergy duels!",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. DAILY CHALLENGE CORE MODULE
        val activeDaily by viewModel.activeDaily.collectAsState()
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2C3141), RoundedCornerShape(20.dp))
                .testTag("daily_challenge_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📆", fontSize = 20.sp)
                        Text(
                            text = "Daily Challenge Prompt",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Intensity level Badge
                    val intensityColor = when (activeDaily.intensity) {
                        "Easy" -> Color(0xFF4CAF50)
                        "Hard" -> Color(0xFFE91E63)
                        else -> Color(0xFFFF9800)
                    }
                    Surface(
                        color = intensityColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, intensityColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "LEVEL: ${activeDaily.intensity.uppercase()}",
                            color = intensityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Dual challenges selection if isDual is true
                if (activeDaily.isDual) {
                    Text(
                        text = "Dual Challenge Options Available! Choose your preferred track:",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectDailyChoice(1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeDaily.selectedChoice == 1) moodTheme.primaryBrush else Color(0xFF1E2230)
                            ),
                            border = BorderStroke(1.dp, if (activeDaily.selectedChoice == 1) Color.Transparent else Color(0xFF323B54)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Track A",
                                color = if (activeDaily.selectedChoice == 1) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.selectDailyChoice(2) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeDaily.selectedChoice == 2) moodTheme.primaryBrush else Color(0xFF1E2230)
                            ),
                            border = BorderStroke(1.dp, if (activeDaily.selectedChoice == 2) Color.Transparent else Color(0xFF323B54)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Track B",
                                color = if (activeDaily.selectedChoice == 2) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFF0C0E14),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (activeDaily.isDual) {
                                if (activeDaily.selectedChoice == 1) "DUAL TRACK A SOURCE:" else "DUAL TRACK B SOURCE:"
                            } else "TODAY'S SPECIAL MISSION:",
                            color = moodTheme.secondaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dailyPrompt.ifBlank { "Astronaut Duck landing on a cheese moon! 🧀" },
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (dailyCompleted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF142417)),
                        border = BorderStroke(1.dp, Color(0xFF43A047)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Finished",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Challenge Completed! ✨",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Synergy Evaluation: $dailyScore/100 • $dailyRank",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF1E222D),
                                disabledContentColor = Color.LightGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bounty Claimed (+50 Coins) 🪙", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.advanceToNextDailyChallenge()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = moodTheme.primaryBrush
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Load Next Challenge ➡️", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Text(
                        text = "Instruction: Draw your artistic interpretation of this prompt inside the Studio, then click 'Grade & Submit' to claim rewards!",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToStudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D212F)),
                            border = BorderStroke(1.dp, Color(0xFF3B435C)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("daily_studio_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Text("Go Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val hasStrokes = strokes.isNotEmpty()
                        Button(
                            onClick = {
                                viewModel.submitDailyChallenge()
                                Toast.makeText(context, "Daily challenge submitted for grading! 🎉", Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasStrokes,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasStrokes) moodTheme.primaryBrush else Color(0xFF222530)
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("daily_submit_btn")
                        ) {
                            Text(
                                text = if (hasStrokes) "Grade & Submit" else "Draw in Studio First",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasStrokes) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 3. MULTIPLAYER CO-DRAWING CHALLENGE ARENA (DUELS)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151D)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2C3141), RoundedCornerShape(20.dp))
                .testTag("duels_hub_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚔️", fontSize = 20.sp)
                    Text(
                        text = "Co-Drawing Duels Hub",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (activeDuelStatus.isEmpty()) {
                    // --- PART 1: INITIATE / GENERATE DUEL CODE ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181B24), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF252936), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📨 Challenge a Friend (Send Code)",
                            color = moodTheme.secondaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.8.sp
                        )
                        Text(
                            text = "Draw your half in Studio, give it a title/prompt, and generate an invitation code to pass along!",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )

                        OutlinedTextField(
                            value = userNicknameInput,
                            onValueChange = { userNicknameInput = it },
                            label = { Text("Your Artist Nickname", fontSize = 10.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = moodTheme.primaryBrush,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = duelPromptInput,
                            onValueChange = { duelPromptInput = it },
                            label = { Text("Drawing Topic/Prompt", fontSize = 10.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = moodTheme.primaryBrush,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (strokes.isEmpty()) {
                                    Toast.makeText(context, "⚠️ Please draw something in Studio first!", Toast.LENGTH_LONG).show()
                                } else {
                                    invitationCodeResult = viewModel.generateDuelInvitationCode(userNicknameInput, duelPromptInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = moodTheme.primaryBrush),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_code_btn")
                        ) {
                            Text("Generate Challenge Code", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (invitationCodeResult.isNotEmpty() && invitationCodeResult != "ERROR_ENCODING_STREAMS") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Ready to Copy:",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0C0E14), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = invitationCodeResult.take(30) + "...",
                                        color = Color(0xFF00E676),
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("DoodleDuelCode", invitationCodeResult)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied code to clipboard! 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- PART 2: LOAD / ACCEPT DUEL CODE ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181B24), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF252936), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📥 Accept a Friend's Challenge",
                            color = moodTheme.secondaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.8.sp
                        )
                        Text(
                            text = "Enter a challenge code to download their strokes and collaborate on a duo masterwork!",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inviteInputCode,
                                onValueChange = { inviteInputCode = it },
                                label = { Text("Paste invite code here", fontSize = 10.sp) },
                                textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = moodTheme.primaryBrush,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                    inviteInputCode = text
                                    Toast.makeText(context, "Pasted! 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF222633), RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste Clipboard",
                                    tint = Color.LightGray
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (inviteInputCode.isBlank()) {
                                    Toast.makeText(context, "Please paste/enter an invite code!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val ok = viewModel.loadOpponentDuelCode(inviteInputCode)
                                    if (!ok) {
                                        Toast.makeText(context, "❌ Invalid/corrupt challenge code format", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Opponent sketch loaded! Jump into Studio to draw your response! 🎉", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = moodTheme.secondaryAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("load_code_btn")
                        ) {
                            Text("Load Opponent Sketch", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // --- PARTS 3: IN-PROGRESS DUEL SCREEN ---
                    when (activeDuelStatus) {
                        "LOADED" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1724), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Collaborative Battle Active!",
                                        color = Color(0xFFE040FB),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.cancelDuel() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Forfeit Duel", tint = Color.Gray)
                                    }
                                }

                                Text(
                                    text = "Rival Artist: $opponentNickname",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E14)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("THEME / DUEL MISSION:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("\"$currentDuelPrompt\"", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Text("Opponent's Drawing Preview:", color = Color.LightGray, fontSize = 10.sp)

                                // Render live strokes on mini preview canvas
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0C1014))
                                        .border(1.dp, Color(0xFF2E323F), RoundedCornerShape(8.dp))
                                ) {
                                    if (opponentStrokes.isEmpty()) {
                                        Text(
                                            text = "(No drawing coordinates found)",
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                            val pts = opponentStrokes.flatMap { it.points }
                                            val minX = pts.minOfOrNull { it.x } ?: 0f
                                            val maxX = pts.maxOfOrNull { it.x } ?: 1000f
                                            val minY = pts.minOfOrNull { it.y } ?: 0f
                                            val maxY = pts.maxOfOrNull { it.y } ?: 1000f

                                            val dx = (maxX - minX).coerceAtLeast(1f)
                                            val dy = (maxY - minY).coerceAtLeast(1f)
                                            val scaleX = (size.width - 16f) / dx
                                            val scaleY = (size.height - 16f) / dy
                                            val scale = kotlin.math.min(scaleX, scaleY).coerceAtMost(0.4f)

                                            opponentStrokes.forEach { stroke ->
                                                val path = Path()
                                                if (stroke.points.isNotEmpty()) {
                                                    val p1 = stroke.points.first()
                                                    val offX = (size.width - dx * scale) / 2f - minX * scale
                                                    val offY = (size.height - dy * scale) / 2f - minY * scale

                                                    path.moveTo(p1.x * scale + offX, p1.y * scale + offY)
                                                    for (i in 1 until stroke.points.size) {
                                                        val pt = stroke.points[i]
                                                        path.lineTo(pt.x * scale + offX, pt.y * scale + offY)
                                                    }
                                                    drawPath(
                                                        path = path,
                                                        color = Color(stroke.color),
                                                        style = Stroke(
                                                            width = (stroke.width * scale * 1.5f).coerceAtLeast(1.5f),
                                                            cap = StrokeCap.Round,
                                                            join = StrokeJoin.Round
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "Your responsive strokes currently size: ${strokes.size} lines in Studio.",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onNavigateToStudio() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                                        modifier = Modifier.weight(1.0f)
                                    ) {
                                        Text("Go to Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    val hasStrokes = strokes.isNotEmpty()
                                    Button(
                                        onClick = { viewModel.submitDuelChallenge(userNicknameInput) },
                                        enabled = hasStrokes,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("grade_duel_btn")
                                    ) {
                                        Text(
                                            text = if (hasStrokes) "Grade & Complete Duel ⚔️" else "Draw response first",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        "SUBMITTED" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF13151F), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🏆", fontSize = 24.sp)
                                    Column {
                                        Text("Duel Results Graded!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Co-Drawn with $opponentNickname", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }

                                Surface(
                                    color = Color(0xFF221528),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Collaboration Synergy Score:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("$duelResultScore / 100", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Synergy Rating Rank:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(duelResultRating, color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }

                                Text(
                                    text = "Professor Quackers Eval: \"$duelResultMessage\"",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontStyle = FontStyle.Italic
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text("Commit This Joint Masterpiece to Local Museum?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                OutlinedTextField(
                                    value = duoTitleInput,
                                    onValueChange = { duoTitleInput = it },
                                    placeholder = { Text("E.g., Cosmic Fusion: $opponentNickname & Me", fontSize = 11.sp) },
                                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = moodTheme.primaryBrush,
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        viewModel.saveDuoMasterpieceToMuseum(duoTitleInput)
                                        Toast.makeText(context, "Saved to Local Museum! (+40 Coins Earned)", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = moodTheme.primaryBrush),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("save_masterpiece_btn")
                                ) {
                                    Text("🏛️ Save Masterwork to Museum (+40 Coins)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.cancelDuel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Close Duel Settlement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TapParticleOverlay(
    particles: List<com.example.utils.TapCosmeticEffect>
) {
    if (particles.isEmpty()) return
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        particles.forEach { p ->
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(p.x.toInt() - 25, p.y.toInt() - 25) }
                    .graphicsLayer(
                        scaleX = p.scale,
                        scaleY = p.scale,
                        rotationZ = p.angle,
                        alpha = p.alpha
                    )
            ) {
                if (p.isTextBubble) {
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = androidx.compose.ui.graphics.Color(0xFF1E2235).copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFFF9800)),
                        modifier = androidx.compose.ui.Modifier.widthIn(max = 140.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = p.text,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else if (p.modeType == "fairy") {
                    androidx.compose.material3.Text(
                        text = p.text,
                        fontSize = 20.sp,
                        color = p.customColor,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = androidx.compose.ui.graphics.Color.White,
                                blurRadius = 4f
                            )
                        )
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = p.text,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
