package com.example.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import kotlin.random.Random

data class TapCosmeticEffect(
    val id: Long,
    val text: String, // e.g. "✨", "😊", "❤️", "😎", "🦖" or "RAWWR! 🦖"
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val initialX: Float,
    val initialY: Float,
    val scale: Float,
    val angle: Float,
    val alpha: Float,
    val age: Float, // 1.0 down to 0f
    val decay: Float, // age decay speed
    val modeType: String, // "fairy", "smiley", "heart", "sunglasses", "dinosaur"
    val behaviorStyle: String, // "spiral", "fountain", "wave", "bounce", "split", "cardioid", "pulse", "kickflip", "stomp", "sprint"
    val seedVal: Float, // random seeding for non-identical movement
    val customColor: Color = Color.White,
    val isTextBubble: Boolean = false,
    val bubbleText: String = ""
) {
    fun update(): TapCosmeticEffect {
        val nextAge = age - decay
        if (nextAge <= 0f) return this.copy(age = 0f)

        // Time passed (normalized, 0 to 1)
        val t = 1.0f - nextAge
        
        var nextX = x + vx
        var nextY = y + vy
        var nextAngle = angle + seedVal * 12f
        var nextScale = scale
        val nextAlpha = nextAge

        // Dynamic, high-fidelity trajectories that NEVER repeat due to randomized seedVal phase alignments!
        when (behaviorStyle) {
            "spiral" -> {
                val theta = t * 24f + seedVal * 15f
                val radius = (20f + seedVal * 70f) * t
                nextX = initialX + radius * cos(theta)
                nextY = initialY + radius * sin(theta)
                nextScale = scale * (1.2f - t)
            }
            "fountain" -> {
                val gravity = 0.6f + seedVal * 0.4f
                val currentVy = vy + gravity * (t * 80f)
                nextX = x + vx
                nextY = y + currentVy
                nextAngle = angle + (vx * 3f)
            }
            "wave" -> {
                val freq = 3.5f + seedVal * 4.0f
                val amp = 25f + seedVal * 40f
                val dy = -4f - seedVal * 5f
                nextY = y + dy
                nextX = initialX + amp * sin(t * freq * PI.toFloat())
            }
            "bounce" -> {
                val frequency = 2.5f + seedVal * 2.5f
                val bounceHeight = 90f + seedVal * 70f
                nextX = x + vx
                val groundLevel = initialY + 40f
                val bounceY = groundLevel - abs(cos(t * frequency * PI.toFloat())) * bounceHeight * (1.0f - t)
                nextY = bounceY
                nextAngle = angle + 15f * sin(t * 8f)
            }
            "split" -> {
                if (t < 0.35f) {
                    nextScale = scale * (1.0f + t * 3f)
                } else {
                    nextX = x + vx * 3.2f
                    nextY = y + vy * 3.2f
                    nextScale = scale * (1.2f - t)
                }
            }
            "cardioid" -> {
                val angleRad = t * 2f * PI.toFloat() + seedVal * PI.toFloat()
                val rScale = 5f + seedVal * 6f
                val hx = 16f * sin(angleRad).pow(3) * rScale
                val hy = -(13f * cos(angleRad) - 5f * cos(2f * angleRad) - 2f * cos(3f * angleRad) - cos(4f * angleRad)) * rScale
                nextX = initialX + hx
                nextY = initialY + hy
                nextScale = scale * (1.0f + 0.3f * sin(t * 18f))
            }
            "pulse" -> {
                val beat = 1.0f + 0.4f * sin(t * 24f + seedVal * 6f)
                nextScale = scale * beat
                nextY = y - 2f
            }
            "kickflip" -> {
                val speedCoeff = 1.2f + seedVal
                nextX = x + vx * 2.5f * speedCoeff
                if (t > 0.15f && t < 0.65f) {
                    nextAngle = angle + 30f
                    nextY = y - 5f
                } else if (t >= 0.65f) {
                    nextY = y + 5f
                }
            }
            "stomp" -> {
                if (t < 0.25f) {
                    nextScale = scale * (1.0f + t * 5f)
                    nextY = initialY - 40f * t
                } else {
                    nextY = initialY
                    nextScale = scale * 2.5f
                }
            }
            "sprint" -> {
                nextX = x + vx * 5f
                nextY = initialY + 20f * sin(t * 14f + seedVal * 4f)
                nextAngle = 0f
            }
            else -> {
                nextX = x + vx
                nextY = y + vy
            }
        }

        return this.copy(
            x = nextX,
            y = nextY,
            angle = nextAngle,
            scale = nextScale,
            alpha = nextAlpha,
            age = nextAge
        )
    }
}

object TapCosmeticRegistry {
    // Generate cosmetic particles on touch click
    fun generateClickFX(
        clickX: Float,
        clickY: Float,
        activeStyles: Map<String, String>, // e.g., "dinosaur" -> "stomp", etc.
        unlockedLevels: Map<String, Int> // level 1, 2, 3 etc. for each style
    ): List<TapCosmeticEffect> {
        val list = mutableListOf<TapCosmeticEffect>()
        val rand = Random(System.nanoTime())
        
        // Randomly pick which of the enabled or active categories to trigger
        // Let's cycle or randomly pick to keep it completely dynamic
        val categories = listOf("fairy", "smiley", "heart", "sunglasses", "dinosaur")
        val selectedCategory = categories.random(rand)

        val behavior = activeStyles[selectedCategory] ?: when (selectedCategory) {
            "fairy" -> listOf("spiral", "fountain", "wave").random(rand)
            "smiley" -> listOf("bounce", "split", "wave").random(rand)
            "heart" -> listOf("cardioid", "pulse", "spiral").random(rand)
            "sunglasses" -> listOf("kickflip", "spiral", "fountain").random(rand)
            "dinosaur" -> listOf("stomp", "sprint", "bounce").random(rand)
            else -> "fountain"
        }

        val level = unlockedLevels[selectedCategory] ?: 1

        when (selectedCategory) {
            "fairy" -> {
                // Fairy dust sparks
                val numSparks = 8 * level
                val colors = listOf(
                    Color(0xFF81D4FA), Color(0xFFFFCC80), Color(0xFFE040FB),
                    Color(0xFF80DEEA), Color(0xFFC5E1A5), Color(0xFFFFF59D)
                )
                for (i in 0 until numSparks) {
                    val angle = (2.0 * Math.PI * i) / numSparks + rand.nextFloat() * 0.4
                    val speed = 2f + rand.nextFloat() * 4f
                    val vx = (speed * cos(angle)).toFloat()
                    val vy = (speed * sin(angle)).toFloat()
                    list.add(
                        TapCosmeticEffect(
                            id = rand.nextLong(),
                            text = listOf("✨", "⭐", "💫", "✨", "*").random(rand),
                            x = clickX,
                            y = clickY,
                            vx = vx,
                            vy = vy,
                            initialX = clickX,
                            initialY = clickY,
                            scale = 0.7f + rand.nextFloat() * 0.7f,
                            angle = rand.nextFloat() * 360f,
                            alpha = 1.0f,
                            age = 1.0f,
                            decay = 0.015f + rand.nextFloat() * 0.015f,
                            modeType = "fairy",
                            behaviorStyle = behavior,
                            seedVal = rand.nextFloat(),
                            customColor = colors.random(rand)
                        )
                    )
                }
            }
            "smiley" -> {
                // Smiley face explosions
                val numSmileys = if (behavior == "split") 4 else 2 * level
                for (i in 0 until numSmileys) {
                    val angle = (2.0 * Math.PI * i) / numSmileys + rand.nextFloat() * 0.6
                    val speed = 3f + rand.nextFloat() * 3f
                    list.add(
                        TapCosmeticEffect(
                            id = rand.nextLong(),
                            text = listOf("😊", "😀", "😆", "😸", "🌞").random(rand),
                            x = clickX,
                            y = clickY,
                            vx = (speed * cos(angle)).toFloat(),
                            vy = (speed * sin(angle)).toFloat() - 2f,
                            initialX = clickX,
                            initialY = clickY,
                            scale = 1.0f + rand.nextFloat() * 0.5f,
                            angle = rand.nextFloat() * 40f - 20f,
                            alpha = 1.0f,
                            age = 1.0f,
                            decay = 0.012f + rand.nextFloat() * 0.01f,
                            modeType = "smiley",
                            behaviorStyle = behavior,
                            seedVal = rand.nextFloat()
                        )
                    )
                }
            }
            "heart" -> {
                // Loving heart effects
                val numHearts = 1 * level
                val heartSymbols = listOf("❤️", "💖", "💝", "💕", "💘")
                for (i in 0 until numHearts) {
                    val angle = rand.nextFloat() * 2f * PI.toFloat()
                    val speed = 1.5f + rand.nextFloat() * 2.5f
                    list.add(
                        TapCosmeticEffect(
                            id = rand.nextLong(),
                            text = heartSymbols.random(rand),
                            x = clickX,
                            y = clickY,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed - 1f,
                            initialX = clickX,
                            initialY = clickY,
                            scale = 1.1f + rand.nextFloat() * 0.6f,
                            angle = rand.nextFloat() * 30f - 15f,
                            alpha = 1.0f,
                            age = 1.0f,
                            decay = 0.01f + rand.nextFloat() * 0.008f,
                            modeType = "heart",
                            behaviorStyle = behavior,
                            seedVal = rand.nextFloat()
                        )
                    )
                }
            }
            "sunglasses" -> {
                // Cool sunglasses smiles
                val numCaps = 1 * level
                for (i in 0 until numCaps) {
                    list.add(
                        TapCosmeticEffect(
                            id = rand.nextLong(),
                            text = "😎",
                            x = clickX,
                            y = clickY,
                            vx = if (rand.nextBoolean()) 4f else -4f,
                            vy = -1.5f - rand.nextFloat() * 2.5f,
                            initialX = clickX,
                            initialY = clickY,
                            scale = 1.2f + rand.nextFloat() * 0.4f,
                            angle = 0f,
                            alpha = 1.0f,
                            age = 1.0f,
                            decay = 0.014f + rand.nextFloat() * 0.008f,
                            modeType = "sunglasses",
                            behaviorStyle = behavior,
                            seedVal = rand.nextFloat()
                        )
                    )
                    // Mini trail sunglasses
                    if (level >= 2) {
                        list.add(
                            TapCosmeticEffect(
                                id = rand.nextLong(),
                                text = "🕶️",
                                x = clickX - 25f,
                                y = clickY + 15f,
                                vx = if (rand.nextBoolean()) 2f else -2f,
                                vy = 1f,
                                initialX = clickX,
                                initialY = clickY,
                                scale = 0.8f,
                                angle = 15f,
                                alpha = 0.8f,
                                age = 0.8f,
                                decay = 0.02f,
                                modeType = "sunglasses",
                                behaviorStyle = behavior,
                                seedVal = rand.nextFloat()
                            )
                        )
                    }
                }
            }
            "dinosaur" -> {
                // Awesome dinosaurs
                list.add(
                    TapCosmeticEffect(
                        id = rand.nextLong(),
                        text = listOf("🦖", "🦕", "🐊").random(rand),
                        x = clickX,
                        y = clickY,
                        vx = if (rand.nextBoolean()) 2.5f else -2.5f,
                        vy = -5f - rand.nextFloat() * 3f,
                        initialX = clickX,
                        initialY = clickY,
                        scale = 1.4f,
                        angle = 0f,
                        alpha = 1.0f,
                        age = 1.0f,
                        decay = 0.016f,
                        modeType = "dinosaur",
                        behaviorStyle = behavior,
                        seedVal = rand.nextFloat()
                    )
                )

                // Add cute Roar bubble for high levels!
                if (level >= 2 || behavior == "stomp") {
                    val messages = listOf("RAWWR!", "ROAR! 🦖", "CHOMP!", "Dino Power! 🔥", "Stomp! 💥")
                    list.add(
                        TapCosmeticEffect(
                            id = rand.nextLong(),
                            text = messages.random(rand),
                            x = clickX,
                            y = clickY - 45f,
                            vx = 0f,
                            vy = -1.2f,
                            initialX = clickX,
                            initialY = clickY - 45f,
                            scale = 1.0f,
                            angle = 0f,
                            alpha = 1.0f,
                            age = 0.9f,
                            decay = 0.018f,
                            modeType = "dinosaur",
                            behaviorStyle = "pulse",
                            seedVal = rand.nextFloat(),
                            isTextBubble = true
                        )
                    )
                }
            }
        }
        return list
    }
}
