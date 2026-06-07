package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

// ==========================================
// ====== DYNAMIC INTERACTIVE MINIGAMES =====
// ==========================================

@Composable
fun MinigameOverlay(
    viewModel: DoodleViewModel,
    moodTheme: MoodTheme
) {
    val isMinigameActive by viewModel.isMinigameActive.collectAsState()
    if (!isMinigameActive) return

    val initiator by viewModel.minigameInitiator.collectAsState()
    val selectedGame by viewModel.selectedMinigame.collectAsState()
    val isPlayingLive by viewModel.isPlayingMinigameLive.collectAsState()
    val winOrLoss by viewModel.minigameWinOrLoss.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) {} // block clickthrough
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111A)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .border(2.dp, Color(0xFFE040FB), RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (winOrLoss.isNotEmpty()) {
                    MinigameResultScreen(
                        winOrLoss = winOrLoss,
                        initiator = initiator,
                        onDismiss = { viewModel.exitMinigame() },
                        onRetry = { viewModel.startPlayingMinigameLive() }
                    )
                } else if (isPlayingLive) {
                    ActiveLiveGameplay(
                        gameType = selectedGame,
                        onSuccess = { viewModel.completeMinigameWithSuccess() },
                        onFailure = { viewModel.completeMinigameWithFailure() }
                    )
                } else {
                    MinigameInitiationScreen(
                        initiator = initiator,
                        selectedGame = selectedGame,
                        onSelectGame = { viewModel.selectMinigameType(it) },
                        onStart = { viewModel.startPlayingMinigameLive() },
                        onCancel = { viewModel.exitMinigame() }
                    )
                }
            }
        }
    }
}

@Composable
fun MinigameInitiationScreen(
    initiator: String,
    selectedGame: MinigameType,
    onSelectGame: (MinigameType) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = "⚡ CHALLENGE PORTAL ⚡",
        color = Color(0xFFFF5252),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        letterSpacing = 1.sp
    )

    Text(
        text = if (initiator == "BOSS") {
            "Conquer the active Boss Obstacle by scoring victory in this randomized live minigame! You can switch games below if desired."
        } else {
            "Accept/Load your friend's Duo Challenge with a special +15 Synergy bonus by conquering this live minigame first!"
        },
        color = Color.LightGray,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161924)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(selectedGame.icon, fontSize = 28.sp)
                Text(
                    text = selectedGame.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Text(
                text = selectedGame.description,
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = "🎯 Select/Update Weapon Choice:",
        color = Color(0xFFFFD54F),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MinigameType.values().forEach { game ->
            val isSelected = game == selectedGame
            val borderBrush = if (isSelected) Color(0xFF00E676) else Color.Transparent
            val cardBg = if (isSelected) Color(0xFF1D2924) else Color(0xFF13151D)

            Surface(
                color = cardBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderBrush),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectGame(game) }
                    .height(54.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(game.icon, fontSize = 15.sp)
                    Text(
                        text = if (game == MinigameType.DOODLE_CATCHER) "Catcher" else if (game == MinigameType.INK_DODGER) "Dodger" else "Memory",
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onStart,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text("Start Challenging Game 🎮", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }

    OutlinedButton(
        onClick = onCancel,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text("Retreat / Dismiss ✖️", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActiveLiveGameplay(
    gameType: MinigameType,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    when (gameType) {
        MinigameType.DOODLE_CATCHER -> DoodleCatcherGame(onSuccess, onFailure)
        MinigameType.INK_DODGER -> InkDodgerGame(onSuccess, onFailure)
        MinigameType.MEMORY_MATCHER -> MemoryMatcherGame(onSuccess, onFailure)
    }
}

@Composable
fun DoodleCatcherGame(onSuccess: () -> Unit, onFail: () -> Unit) {
    var bucketX by remember { mutableStateOf(0.5f) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var targetList by remember { mutableStateOf(emptyList<FallingObject>()) }

    LaunchedEffect(Unit) {
        var iterations = 0
        while (lives > 0 && score < 10) {
            delay(20)
            iterations++

            targetList = targetList.map { obj ->
                obj.copy(y = obj.y + obj.speed)
            }.filter { obj ->
                var hit = false
                if (obj.y >= 330f && obj.y <= 360f) {
                    val dist = abs(obj.x - bucketX)
                    if (dist < 0.14f) {
                        hit = true
                        if (obj.isBomb) {
                            lives--
                        } else {
                            score++
                        }
                    }
                }
                !hit && obj.y < 430f
            }

            if (iterations % 35 == 0) {
                val isBomb = Random.nextFloat() < 0.25f
                val newObj = FallingObject(
                    id = iterations,
                    x = Random.nextFloat().coerceIn(0.05f, 0.95f),
                    y = 0f,
                    speed = Random.nextFloat() * 4f + 5.5f,
                    isBomb = isBomb,
                    color = if (isBomb) Color.Red else Color(0xFF00E676)
                )
                targetList = targetList + newObj
            }
        }

        if (score >= 10) {
            onSuccess()
        } else {
            onFail()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Score: $score/10 🎨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Lives: ${"❤️".repeat(lives)}", color = Color.Red, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF070913), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1E2942), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasH = size.height
                val canvasW = size.width

                targetList.forEach { obj ->
                    val cx = obj.x * canvasW
                    val cy = (obj.y / 400f) * canvasH
                    if (obj.isBomb) {
                        drawCircle(color = Color(0xFFD50000), radius = 10.dp.toPx(), center = Offset(cx, cy))
                        drawCircle(color = Color.Black, radius = 5.dp.toPx(), center = Offset(cx, cy))
                    } else {
                        drawCircle(color = obj.color, radius = 8.dp.toPx(), center = Offset(cx, cy))
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx, cy - 9.dp.toPx())
                            lineTo(cx - 7.dp.toPx(), cy)
                            lineTo(cx + 7.dp.toPx(), cy)
                            close()
                        }
                        drawPath(path = path, color = obj.color)
                    }
                }

                val bx = bucketX * canvasW
                val by = (345f / 400f) * canvasH
                val rectW = 44.dp.toPx()
                val rectH = 14.dp.toPx()

                drawRoundRect(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(bx - rectW / 2, by),
                    size = androidx.compose.ui.geometry.Size(rectW, rectH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRect(
                    color = Color(0xFFE040FB),
                    topLeft = Offset(bx - rectW / 2 + 2.dp.toPx(), by + 2.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(rectW - 4.dp.toPx(), 2.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { bucketX = (bucketX - 0.15f).coerceIn(0.05f, 0.95f) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212638)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("◀ Left", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Slider(
                value = bucketX,
                onValueChange = { bucketX = it },
                modifier = Modifier.weight(1.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = { bucketX = (bucketX + 0.15f).coerceIn(0.05f, 0.95f) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212638)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Right ▶", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class FallingObject(
    val id: Int,
    val x: Float,
    val y: Float,
    val speed: Float,
    val isBomb: Boolean,
    val color: Color
)

@Composable
fun InkDodgerGame(onSuccess: () -> Unit, onFail: () -> Unit) {
    var duckLocalOffset by remember { mutableStateOf(Offset(150f, 100f)) }
    var secondsRemaining by remember { mutableStateOf(15.0f) }
    var lives by remember { mutableStateOf(3) }
    var inkStains by remember { mutableStateOf(emptyList<InkStain>()) }

    LaunchedEffect(Unit) {
        var msCount = 0
        while (secondsRemaining > 0f && lives > 0) {
            delay(20)
            msCount += 20
            secondsRemaining = (secondsRemaining - 0.02f).coerceAtLeast(0f)

            inkStains = inkStains.map { stain ->
                var nx = stain.x + stain.vx
                var ny = stain.y + stain.vy
                var vx = stain.vx
                var vy = stain.vy
                if (nx < 8f || nx > 312f) {
                    vx = -vx
                    nx = nx.coerceIn(8f, 312f)
                }
                if (ny < 8f || ny > 172f) {
                    vy = -vy
                    ny = ny.coerceIn(8f, 172f)
                }
                stain.copy(x = nx, y = ny, vx = vx, vy = vy)
            }.filter { stain ->
                val dx = stain.x - duckLocalOffset.x
                val dy = stain.y - duckLocalOffset.y
                val dist = sqrt(dx * dx + dy * dy)
                val hit = dist < (stain.radius + 12f)
                if (hit) {
                    lives--
                }
                !hit
            }

            if (msCount % 600 == 0) {
                val side = Random.nextInt(4)
                val sx = when (side) {
                    2 -> 2f
                    3 -> 318f
                    else -> Random.nextFloat() * 320f
                }
                val sy = when (side) {
                    0 -> 2f
                    1 -> 180f
                    else -> Random.nextFloat() * 182f
                }
                val tdx = duckLocalOffset.x - sx
                val tdy = duckLocalOffset.y - sy
                val length = sqrt(tdx * tdx + tdy * tdy).coerceAtLeast(1f)
                val inkSpeed = Random.nextFloat() * 2f + 3f

                inkStains = inkStains + InkStain(
                    id = msCount,
                    x = sx,
                    y = sy,
                    vx = (tdx / length) * inkSpeed,
                    vy = (tdy / length) * inkSpeed,
                    radius = Random.nextFloat() * 4f + 8f,
                    color = Color(0xFFFF5252)
                )
            }
        }

        if (secondsRemaining <= 0f && lives > 0) {
            onSuccess()
        } else {
            onFail()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Survive: ${String.format("%.1f", secondsRemaining)}s ⏱️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Shields: ${"❤️".repeat(lives)}", color = Color.Red, fontSize = 12.sp)
        }

        Text(
            text = "Drag/slide within the field below to dodge falling stains! 🦆",
            color = Color.LightGray,
            fontSize = 9.sp
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF05060D), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF2C3141), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        duckLocalOffset = Offset(
                            x = (duckLocalOffset.x + dragAmount.x).coerceIn(12f, 308f),
                            y = (duckLocalOffset.y + dragAmount.y).coerceIn(12f, 168f)
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cw = size.width
                val ch = size.height

                for (i in 0..10) {
                    val progressW = (i.toFloat() / 10f) * cw
                    drawLine(Color(0xFF151928), Offset(progressW, 0f), Offset(progressW, ch), strokeWidth = 1f)
                }
                for (j in 0..6) {
                    val progressH = (j.toFloat() / 6f) * ch
                    drawLine(Color(0xFF151928), Offset(0f, progressH), Offset(cw, progressH), strokeWidth = 1f)
                }

                inkStains.forEach { stain ->
                    val cx = (stain.x / 320f) * cw
                    val cy = (stain.y / 182f) * ch
                    val r = (stain.radius / 320f) * cw

                    drawCircle(color = stain.color, radius = r, center = Offset(cx, cy))
                    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = r * 0.4f, center = Offset(cx, cy))
                }

                val dx = (duckLocalOffset.x / 320f) * cw
                val dy = (duckLocalOffset.y / 182f) * ch

                drawCircle(color = Color(0xFFE040FB).copy(alpha = 0.3f), radius = 16.dp.toPx(), center = Offset(dx, dy))
                drawCircle(color = Color(0xFF00E676), radius = 6.dp.toPx(), center = Offset(dx, dy))
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * (duckLocalOffset.x / 320f) - 14.dp,
                        y = maxHeight * (duckLocalOffset.y / 182f) - 14.dp
                    )
                    .size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🦆", fontSize = 18.sp)
            }
        }
    }
}

data class InkStain(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun MemoryMatcherGame(onSuccess: () -> Unit, onFail: () -> Unit) {
    val symbolsPresets = remember { listOf("🎨", "🖌️", "🦆", "💎", "🎨", "🖌️", "🦆", "💎").shuffled() }

    var cardStates by remember {
        mutableStateOf(
            List(8) { idx ->
                MemoryCard(id = idx, symbol = symbolsPresets[idx], isFlipped = false, isMatched = false)
            }
        )
    }

    var selectedIndexOne by remember { mutableStateOf<Int?>(null) }
    var secondsRemaining by remember { mutableStateOf(30) }
    var matchesCount by remember { mutableStateOf(0) }
    var processing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0 && matchesCount < 4) {
            delay(1000)
            secondsRemaining--
        }
        if (matchesCount >= 4) {
            onSuccess()
        } else {
            onFail()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Matched: $matchesCount/4 🧬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Timer: ${secondsRemaining}s ⏱️", color = if (secondsRemaining < 10) Color.Red else Color.LightGray, fontSize = 12.sp)
        }

        Text(
            text = "Reveal matching artwork coordinates to activate synergy connection!",
            color = Color.Gray,
            fontSize = 9.sp
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (row in 0..1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0..3) {
                        val cardIdx = row * 4 + col
                        val card = cardStates[cardIdx]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (card.isMatched) Color(0xFF003311)
                                    else if (card.isFlipped) Color(0xFF2E1B4E)
                                    else Color(0xFF1B1D28)
                                )
                                .border(
                                    1.dp,
                                    if (card.isMatched) Color(0xFF00E676)
                                    else if (card.isFlipped) Color(0xFF90CAF9)
                                    else Color(0xFF2E3244),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !card.isMatched && !card.isFlipped && !processing) {
                                    val updated = cardStates.mapIndexed { i, c ->
                                        if (i == cardIdx) c.copy(isFlipped = true) else c
                                    }
                                    cardStates = updated

                                    if (selectedIndexOne == null) {
                                        selectedIndexOne = cardIdx
                                    } else {
                                        val idxOne = selectedIndexOne!!
                                        if (updated[idxOne].symbol == card.symbol) {
                                            cardStates = updated.mapIndexed { i, c ->
                                                if (i == idxOne || i == cardIdx) c.copy(isMatched = true) else c
                                            }
                                            matchesCount++
                                            selectedIndexOne = null
                                        } else {
                                            processing = true
                                            selectedIndexOne = null
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (card.isFlipped || card.isMatched) {
                                Text(card.symbol, fontSize = 24.sp)
                            } else {
                                Text("❓", color = Color(0xFF42A5F5), fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        if (processing && selectedIndexOne == null) {
            LaunchedEffect(cardStates) {
                delay(650)
                cardStates = cardStates.map { c ->
                    if (!c.isMatched) c.copy(isFlipped = false) else c
                }
                processing = false
            }
        }
    }
}

data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isFlipped: Boolean,
    val isMatched: Boolean
)

@Composable
fun MinigameResultScreen(
    winOrLoss: String,
    initiator: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    if (winOrLoss == "WIN") {
        Text("🎉 CHALLENGE SUCCESS 🎉", color = Color(0xFF00E676), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text("✨ BARRIER BROKEN! ✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (initiator == "BOSS") {
                "Awesome reflex mastery! The active Boss Obstacle has been completely driven out and bounty coins credited to your purse! 🪙"
            } else {
                "Dual core synchronization achieved! The loaded Duo challenge is activated with a special +15 bonus added to final co-draw calculations! 🔥"
            },
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Complete & Claim Rewards 🏆", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    } else {
        Text("💔 CHALLENGE DEFEATED 💔", color = Color(0xFFFF5252), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text("RESOURCES DEPLETED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Skill obstacles happen. Adjust your minigame selection layout or try again immediately to override!",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC80)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text("Try Game Match Again 🔁", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text("Retreat For Now ✖️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
