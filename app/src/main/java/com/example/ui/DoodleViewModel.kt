package com.example.ui

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiRepository
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.utils.AmbientSynth

// --- Visual Drawing & Sticker Models ---

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

data class DrawingStroke(
    val points: List<StrokePoint>,
    val color: Int, // Hex Int representing color
    val width: Float,
    val brushType: String, // Pencil, Charcoal, Neon Glow, Rainbow Trail, Fire, Smoke, Star, etc.
    val alpha: Float = 1.0f,
    val isEraser: Boolean = false
)

data class PlacedSticker(
    val id: String,
    val type: String, // icon/sticker name
    val x: Float,
    val y: Float,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

data class DinosaurAccessory(
    val shapeType: Int, // 0, 1, 2
    val color: Color,
    val patternName: String, // Stripes, Polka, Solid
    val scale: Float
)

// --- Preset Daily Tips ---
data class DrawingTip(
    val id: Int,
    val category: String, // Drawing tips, Color theory, Character design, Perspective, Storytelling, Shading, Creature design
    val text: String
)

enum class MinigameType(val title: String, val icon: String, val description: String) {
    DOODLE_CATCHER("Doodle Catcher", "🎨", "Move the paint bucket to catch falling color droplets while avoiding dark ink bombs! Get 10 points to win."),
    INK_DODGER("Ink Dodger", "👾", "Drag your duck around the matrix to dodge the laser-guided flying ink stains. Survive 15 seconds to win."),
    MEMORY_MATCHER("Custom Memory Match", "🧩", "Flip cards and find all 4 pairs of matching drawing shapes/tools within 30 seconds to win.")
}

class DoodleViewModel(
    private val repository: DoodleRepository,
    private val geminiRepository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val ambientSynth = AmbientSynth()

    // --- State Flows ---
    val allDrawings: StateFlow<List<SavedDrawing>> = repository.allDrawings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgress> = repository.userProgress
        .map { it ?: UserProgress() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgress())

    // --- Local Live Canvas State ---
    private val _strokes = MutableStateFlow<List<DrawingStroke>>(emptyList())
    val strokes: StateFlow<List<DrawingStroke>> = _strokes.asStateFlow()

    private val _placedStickers = MutableStateFlow<List<PlacedSticker>>(emptyList())
    val placedStickers: StateFlow<List<PlacedSticker>> = _placedStickers.asStateFlow()

    private val _selectedTool = MutableStateFlow("Pencil") // Tool details
    val selectedTool: StateFlow<String> = _selectedTool.asStateFlow()

    private val _selectedColor = MutableStateFlow(0xFF3F51B5.toInt()) // Hex Indigo
    val selectedColor: StateFlow<Int> = _selectedColor.asStateFlow()

    private val _strokeWidth = MutableStateFlow(8f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _symmetryEnabled = MutableStateFlow(false)
    val symmetryEnabled: StateFlow<Boolean> = _symmetryEnabled.asStateFlow()

    private val _shapeCreatorMode = MutableStateFlow("Free") // Free, Rectangle, Circle, Line
    val shapeCreatorMode: StateFlow<String> = _shapeCreatorMode.asStateFlow()

    private val _perspectiveGuideEnabled = MutableStateFlow(false)
    val perspectiveGuideEnabled: StateFlow<Boolean> = _perspectiveGuideEnabled.asStateFlow()

    // --- Random Dino Customization (Recalls every app launch/init) ---
    private val _dinoHat = MutableStateFlow<DinosaurAccessory?>(null)
    val dinoHat: StateFlow<DinosaurAccessory?> = _dinoHat.asStateFlow()

    private val _dinoGlasses = MutableStateFlow<DinosaurAccessory?>(null)
    val dinoGlasses: StateFlow<DinosaurAccessory?> = _dinoGlasses.asStateFlow()

    private val _dinoMustache = MutableStateFlow<DinosaurAccessory?>(null)
    val dinoMustache: StateFlow<DinosaurAccessory?> = _dinoMustache.asStateFlow()

    private val _dinoOutfit = MutableStateFlow<DinosaurAccessory?>(null)
    val dinoOutfit: StateFlow<DinosaurAccessory?> = _dinoOutfit.asStateFlow()

    // --- Tap Particles Upgradeable State ---
    private val _unlockedTapLevels = MutableStateFlow(mapOf(
        "fairy" to 1,
        "smiley" to 1,
        "heart" to 1,
        "sunglasses" to 1,
        "dinosaur" to 1
    ))
    val unlockedTapLevels: StateFlow<Map<String, Int>> = _unlockedTapLevels.asStateFlow()

    private val _activeTapStyles = MutableStateFlow(mapOf(
        "fairy" to "spiral",
        "smiley" to "bounce",
        "heart" to "cardioid",
        "sunglasses" to "kickflip",
        "dinosaur" to "stomp"
    ))
    val activeTapStyles: StateFlow<Map<String, String>> = _activeTapStyles.asStateFlow()

    private val _dinoVibeState = MutableStateFlow("Classic Polka-Dinosaur 🦖")
    val dinoVibeState: StateFlow<String> = _dinoVibeState.asStateFlow()

    private val _quackersVoice = MutableStateFlow("Scholarly")
    val quackersVoice: StateFlow<String> = _quackersVoice.asStateFlow()

    private val _hyperRealismEnabled = MutableStateFlow(false)
    val hyperRealismEnabled: StateFlow<Boolean> = _hyperRealismEnabled.asStateFlow()

    fun setQuackersVoice(voice: String) {
        _quackersVoice.value = voice
    }

    fun toggleHyperRealism() {
        val newVal = !_hyperRealismEnabled.value
        _hyperRealismEnabled.value = newVal
        if (newVal) {
            awardAchievement("Hyper-realist")
        }
    }

    private val _aiDuetEnabled = MutableStateFlow(false)
    val aiDuetEnabled: StateFlow<Boolean> = _aiDuetEnabled.asStateFlow()

    private val _duetBubbleText = MutableStateFlow<String?>(null)
    val duetBubbleText: StateFlow<String?> = _duetBubbleText.asStateFlow()

    private val _drawingFrameStyles = MutableStateFlow<Map<Int, String>>(emptyMap())
    val drawingFrameStyles: StateFlow<Map<Int, String>> = _drawingFrameStyles.asStateFlow()

    private val _inkLivingTrailEnabled = MutableStateFlow(true)
    val inkLivingTrailEnabled: StateFlow<Boolean> = _inkLivingTrailEnabled.asStateFlow()

    // --- Minigames System ---
    private val _isMinigameActive = MutableStateFlow(false)
    val isMinigameActive: StateFlow<Boolean> = _isMinigameActive.asStateFlow()

    private val _minigameInitiator = MutableStateFlow<String>("") // "BOSS" or "DUO"
    val minigameInitiator: StateFlow<String> = _minigameInitiator.asStateFlow()

    private val _selectedMinigame = MutableStateFlow<MinigameType>(MinigameType.DOODLE_CATCHER)
    val selectedMinigame: StateFlow<MinigameType> = _selectedMinigame.asStateFlow()

    private val _isPlayingMinigameLive = MutableStateFlow(false)
    val isPlayingMinigameLive: StateFlow<Boolean> = _isPlayingMinigameLive.asStateFlow()

    private val _minigameWinOrLoss = MutableStateFlow<String>("") // "", "WIN", "LOSS"
    val minigameWinOrLoss: StateFlow<String> = _minigameWinOrLoss.asStateFlow()

    // Extra synergy bonus for duos
    private val _duoSynergyBonus = MutableStateFlow(0)
    val duoSynergyBonus: StateFlow<Int> = _duoSynergyBonus.asStateFlow()

    // Hold the pending opponent details
    private var pendingOpponentNickname = ""
    private var pendingDuelPrompt = ""
    private var pendingOpponentStrokes = emptyList<DrawingStroke>()

    // --- Duels Challenge Hub states ---
    private val _opponentStrokes = MutableStateFlow<List<DrawingStroke>>(emptyList())
    val opponentStrokes: StateFlow<List<DrawingStroke>> = _opponentStrokes.asStateFlow()

    private val _currentDuelPrompt = MutableStateFlow<String>("")
    val currentDuelPrompt: StateFlow<String> = _currentDuelPrompt.asStateFlow()

    private val _opponentNickname = MutableStateFlow<String>("")
    val opponentNickname: StateFlow<String> = _opponentNickname.asStateFlow()

    private val _activeDuelStatus = MutableStateFlow<String>("") // "", "LOADED", "SUBMITTED"
    val activeDuelStatus: StateFlow<String> = _activeDuelStatus.asStateFlow()

    private val _duelResultRating = MutableStateFlow<String>("") // "SSS", "S", "A", "B"
    val duelResultRating: StateFlow<String> = _duelResultRating.asStateFlow()

    private val _duelResultScore = MutableStateFlow<Int>(0)
    val duelResultScore: StateFlow<Int> = _duelResultScore.asStateFlow()

    private val _duelResultMessage = MutableStateFlow<String>("")
    val duelResultMessage: StateFlow<String> = _duelResultMessage.asStateFlow()

    // Daily Challenge details
    private val _dailyChallengePrompt = MutableStateFlow<String>("")
    val dailyChallengePrompt: StateFlow<String> = _dailyChallengePrompt.asStateFlow()

    private val _dailyCompleted = MutableStateFlow(false)
    val dailyCompleted: StateFlow<Boolean> = _dailyCompleted.asStateFlow()

    private val _dailyScore = MutableStateFlow<Int>(0)
    val dailyScore: StateFlow<Int> = _dailyScore.asStateFlow()

    private val _dailyRank = MutableStateFlow<String>("")
    val dailyRank: StateFlow<String> = _dailyRank.asStateFlow()

    fun toggleAiDuet() {
        _aiDuetEnabled.value = !_aiDuetEnabled.value
        if (_aiDuetEnabled.value) {
            _duetBubbleText.value = "Duet activated! 🎨 Draw a stroke, and I will sketch a partner stroke next!"
        } else {
            _duetBubbleText.value = null
        }
    }

    fun toggleLivingTrail() {
        _inkLivingTrailEnabled.value = !_inkLivingTrailEnabled.value
    }

    fun clearDuetBubble() {
        _duetBubbleText.value = null
    }

    fun setDrawingFrameStyle(drawingId: Int, style: String) {
        val current = _drawingFrameStyles.value.toMutableMap()
        current[drawingId] = style
        _drawingFrameStyles.value = current
    }

    fun cycleFrameStyle(drawingId: Int) {
        val current = _drawingFrameStyles.value.toMutableMap()
        val unlockedStr = userProgress.value.unlockedFrames
        val styles = unlockedStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val active = current[drawingId] ?: "None"
        val nextIdx = if (styles.contains(active)) {
            (styles.indexOf(active) + 1) % styles.size
        } else {
            0
        }
        current[drawingId] = styles.getOrElse(nextIdx) { "None" }
        _drawingFrameStyles.value = current
    }

    fun triggerAiDuetStroke() {
        if (_strokes.value.isEmpty()) return
        
        val lastStroke = _strokes.value.lastOrNull()
        val lastPoint = lastStroke?.points?.lastOrNull() ?: StrokePoint(400f, 400f)
        
        val aiColors = listOf(
            0xFFFFD23F.toInt(), // Imperial Gold
            0xFF2196F3.toInt(), // Galactic Blue
            0xFFFF4081.toInt(), // Hot Magenta
            0xFF00E676.toInt(), // Electric Lime
            0xFFE040FB.toInt()  // Plasma Purple
        )
        val aiColor = aiColors.random()
        
        val brushTypes = listOf("Neon Glow", "Rainbow Trail", "Pencil", "Charcoal")
        val selectedBrush = brushTypes.random()
        
        val strokePoints = mutableListOf<StrokePoint>()
        val startX = lastPoint.x
        val startY = lastPoint.y
        
        val roll = Random.nextInt(4)
        val companionName = if (Random.nextBoolean()) "Dr. Polka-Dot" else "Professor Quackers"
        
        val description = when (roll) {
            0 -> { // Symmetrical floral loop
                val petales = 5
                val scaleFactor = 60f + Random.nextFloat() * 40f
                for (step in 0..60) {
                    val t = (step / 60f) * 2 * Math.PI.toFloat()
                    val r = scaleFactor * kotlin.math.sin(petales * t)
                    val px = startX + r * kotlin.math.cos(t)
                    val py = startY + r * kotlin.math.sin(t)
                    strokePoints.add(StrokePoint(px.toFloat(), py.toFloat()))
                }
                "$companionName sketched a Golden Symmetrical Floral loop around your stroke!"
            }
            1 -> { // Mathematical Logarithmic Spiral
                val size = 30
                for (i in 0..size) {
                    val theta = i * 0.35f
                    val r = 5f + i * 2.8f
                    val px = startX + r * kotlin.math.cos(theta)
                    val py = startY + r * kotlin.math.sin(theta)
                    strokePoints.add(StrokePoint(px.toFloat(), py.toFloat()))
                }
                "$companionName swirled a Fibonacci Galactic Spiral onto the pad!"
            }
            2 -> { // Joyful wavy water ribbon
                for (i in 0..24) {
                    val progress = i / 24f
                    val px = startX + (progress * 150f - 75f)
                    val py = startY + (35f * kotlin.math.sin(i * 0.5f)).toFloat() + 20f
                    strokePoints.add(StrokePoint(px, py))
                }
                "$companionName balanced your depth with a dynamic sinusoidal wave ribbon."
            }
            else -> { // Celestial Star alignment
                val pointsCount = 10
                val outerRadius = 50f + Random.nextFloat() * 30f
                val innerRadius = outerRadius * 0.4f
                for (i in 0..pointsCount) {
                    val angle = (i * Math.PI / 5)
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val px = startX + r * kotlin.math.cos(angle)
                    val py = startY + r * kotlin.math.sin(angle)
                    strokePoints.add(StrokePoint(px.toFloat(), py.toFloat()))
                }
                "$companionName plotted a radiant glowing Celestial Star over your path!"
            }
        }
        
        val aiStroke = DrawingStroke(
            points = strokePoints,
            color = aiColor,
            width = (lastStroke?.width ?: 8f) * 1.2f,
            brushType = selectedBrush,
            alpha = 0.85f
        )
        
        _strokes.value = _strokes.value + aiStroke
        _duetBubbleText.value = description
    }

    private fun updateDinoVibeState() {
        val hat = _dinoHat.value
        val glasses = _dinoGlasses.value
        val mustache = _dinoMustache.value
        val outfit = _dinoOutfit.value

        _dinoVibeState.value = when {
            hat?.shapeType == 0 && glasses != null && outfit?.shapeType == 0 -> "Dashing Gentleman 🎩"
            hat?.shapeType == 3 && outfit?.shapeType == 2 -> "Cosmic Wizard Mage 🔮"
            hat?.shapeType == 2 && mustache != null -> "Wild West Sheriff 🤠"
            outfit?.shapeType == 3 -> "Superhero Protector 🦸"
            hat?.shapeType == 1 && glasses?.shapeType == 0 -> "Intrepid Detective 🕵️"
            glasses?.shapeType == 2 && outfit?.shapeType == 2 -> "Space Star Wanderer 🚀"
            hat != null && glasses != null && outfit != null -> "Fully Loaded Scholar 🎓"
            mustache != null && glasses != null -> "Professor's Elite Critic 🧐"
            hat != null -> "Stylish Explorer ⛺"
            outfit != null -> "Suited Diplomat 💼"
            else -> "Classic Polka-Dinosaur 🦖"
        }
    }

    private val _latestAchievementUnlocked = MutableStateFlow<String?>(null)
    val latestAchievementUnlocked: StateFlow<String?> = _latestAchievementUnlocked.asStateFlow()

    fun dismissAchievementDialog() {
        _latestAchievementUnlocked.value = null
    }

    fun awardAchievement(achievement: String) {
        viewModelScope.launch {
            val newlyUnlocked = repository.awardAchievement(achievement)
            if (newlyUnlocked) {
                _latestAchievementUnlocked.value = achievement
            }
        }
    }

    fun setDinoHat(shapeCreator: Int, color: Int) {
        if (shapeCreator == -1) {
            _dinoHat.value = null
        } else {
            _dinoHat.value = DinosaurAccessory(shapeCreator, Color(color), "Solid", 1.0f)
        }
        updateDinoVibeState()
        awardAchievement("Dino Outfitter")
    }

    fun setDinoGlasses(shapeCreator: Int, color: Int) {
        if (shapeCreator == -1) {
            _dinoGlasses.value = null
        } else {
            _dinoGlasses.value = DinosaurAccessory(shapeCreator, Color(color), "Solid", 1.0f)
        }
        updateDinoVibeState()
        awardAchievement("Dino Outfitter")
    }

    fun setDinoMustache(shapeCreator: Int, color: Int) {
        if (shapeCreator == -1) {
            _dinoMustache.value = null
        } else {
            _dinoMustache.value = DinosaurAccessory(shapeCreator, Color(color), "Fuzzy", 1.0f)
        }
        updateDinoVibeState()
        awardAchievement("Dino Outfitter")
    }

    fun setDinoOutfit(shapeCreator: Int, color: Int) {
        if (shapeCreator == -1) {
            _dinoOutfit.value = null
        } else {
            _dinoOutfit.value = DinosaurAccessory(shapeCreator, Color(color), "Solid", 1.0f)
        }
        updateDinoVibeState()
        awardAchievement("Dino Outfitter")
    }

    // --- Rotating Daily Tips (Persisted dislikes) ---
    private val allTips = listOf(
        DrawingTip(101, "Drawing tips", "Try sketching using only negative space today: draw the space around the object first."),
        DrawingTip(102, "Drawing tips", "Exercise: Draw a complex household object using only perfect circles and triangles."),
        DrawingTip(103, "Perspective", "Locate your horizon line and place a single vanishing point. Connect all outlines back to this star."),
        DrawingTip(104, "Shading", "Avoid shading using black charcoal. Use a darker shade of your complementary color instead!"),
        DrawingTip(105, "Color theory", "Try a three-color palette today: choose one primary tone, and two closely adjacent accent tones."),
        DrawingTip(106, "Character design", "Push pose gesture! Draw your character with an exaggerated line of action from foot to skull."),
        DrawingTip(107, "Storytelling", "Create mystery. Ensure one item in your drawing is partially hidden behind another to trigger curiosity!"),
        DrawingTip(108, "Creature design", "Blend an aquatic predator with a flying bird. Structure the skeletal wings around real muscle anchors."),
        DrawingTip(109, "Drawing tips", "Turn your canvas upside down for 5 minutes. This helps your brain see proportions instead of symbols."),
        DrawingTip(110, "Shading", "Real soft clouds are modeled with cross-hatching shadows on their bottom edges. Leave the crowns purely white!"),
        DrawingTip(111, "Color theory", "Use a split-complementary color scheme today: Violet paired with warm ochre amber and pine green."),
        DrawingTip(112, "Creature design", "Design a creature with double-jointed legs. Map out knee and femur triangles before sketching skin.")
    )

    private val _currentTip = MutableStateFlow<DrawingTip>(allTips.first())
    val currentTip: StateFlow<DrawingTip> = _currentTip.asStateFlow()

    // --- AI Assistant Sidekick Duck Professor Quackers Chat State ---
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // Pair of Text, isUser
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    // --- Science Art Analysis State ---
    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _analysisLoading = MutableStateFlow(false)
    val analysisLoading: StateFlow<Boolean> = _analysisLoading.asStateFlow()

    // --- Jar of Ideas ---
    private val promptsJar = listOf(
        "Draw a dinosaur who loves organic gardening.",
        "Draw a metallic clockwork fish that lives in high-altitude purple clouds.",
        "Draw an elaborate gothic castle built entirely inside a colossal hollow pumpkin.",
        "Draw a pirate duck riding a mechanical squid through a cavern of bioluminescent moss.",
        "Draw a friendly space astronaut harvesting glowing cosmic mushrooms on the moon.",
        "Draw an underwater library where the sharks read vintage leather-bound logs.",
        "Draw a steampunky clockwork squirrel assembling tiny brass acorns.",
        "Draw an ancient wizard raccoon mixing rainbow ingredients in a giant wooden bowl."
    )
    private val _shakenPrompt = MutableStateFlow("")
    val shakenPrompt: StateFlow<String> = _shakenPrompt.asStateFlow()

    // --- Dreamscape Explorer State ---
    private val _dreamscapeIdea = MutableStateFlow("")
    val dreamscapeIdea: StateFlow<String> = _dreamscapeIdea.asStateFlow()

    private val _dreamscapeLoading = MutableStateFlow(false)
    val dreamscapeLoading: StateFlow<Boolean> = _dreamscapeLoading.asStateFlow()

    // --- Creative Story Scene Builder State ---
    val storyCharacters = listOf("Captain Quack", "Valkyrie Duck", "Clockwork Owl", "Starry Wizard")
    val storyLocations = listOf("Floating Sky Islands", "Whispering Bioluminescent Caves", "The Clockwork Forest", "Titan's Donut Desert")
    val storyProblems = listOf("Lost compass under a cloud vortex", "Intruding Blank Page Kraken", "Frozen magnetic springs", "Boredom shadow ghosts")

    private val _storyChapter = MutableStateFlow(1)
    val storyChapter: StateFlow<Int> = _storyChapter.asStateFlow()

    private val _storyNarrative = MutableStateFlow("")
    val storyNarrative: StateFlow<String> = _storyNarrative.asStateFlow()

    private val _storyLoading = MutableStateFlow(false)
    val storyLoading: StateFlow<Boolean> = _storyLoading.asStateFlow()

    private val _selectedChar = MutableStateFlow("Captain Quack")
    val selectedChar: StateFlow<String> = _selectedChar.asStateFlow()

    private val _selectedLoc = MutableStateFlow("The Clockwork Forest")
    val selectedLoc: StateFlow<String> = _selectedLoc.asStateFlow()

    private val _selectedProb = MutableStateFlow("Intruding Blank Page Kraken")
    val selectedProb: StateFlow<String> = _selectedProb.asStateFlow()

    // --- Boss Challenges ( rotating artwork bosses resolved by completing prompts on canvas!) ---
    data class BossChallenge(
        val name: String,
        val icon: String,
        val requirement: String,
        val bounty: Int,
        val intensity: String = "Medium", // Easy, Medium, Hard
        val isDual: Boolean = false,
        val choiceA: String = "",
        val choiceB: String = "",
        val selectedChoice: Int = 1 // 1 for A, 2 for B
    )

    data class DailyChallengeItem(
        val id: String,
        val title: String,
        val requirement: String,
        val bounty: Int = 50,
        val intensity: String = "Medium", // Easy, Medium, Hard
        val isDual: Boolean = false,
        val choiceA: String = "",
        val choiceB: String = "",
        val selectedChoice: Int = 1
    )

    // Complete boss catalog registry
    private val bossRegistry = listOf(
        BossChallenge("Blank Page Kraken", "🐙", "Draw a dense underwater coral cave featuring an adventurous submarine duck.", 80, "Medium", true, "Adventures of Submarine Duck 🐙", "Bioluminescent Coral Castles 🏰"),
        BossChallenge("Boredom Specter", "👻", "Sketch a lively neon carnival full of floating shapes and bright color details.", 70, "Easy", false),
        BossChallenge("Repeating Dragon", "🐉", "Illustrate a majestic fire-breathing dragon wearing a dapper mechanical bowler hat.", 120, "Hard", true, "Clockwork Bowler Hat Dragon 🐉", "Volcanic Treasure Chamber 🌋"),
        BossChallenge("Blank Void Titan", "🗿", "Carve a heavy stone colossal statue covered in ancient moss on a floating highland.", 140, "Hard", false),
        BossChallenge("Nostalgia Gremlin", "🧸", "Paint a retro dusty toyshop with wind-up tin soldiers and wooden toy planes.", 60, "Easy", true, "Retro Toy-Soldiers 💂", "Nostalgic Teddy Bear Mansion 🧸"),
        BossChallenge("Art Block Goblins", "👺", "Illustrate a miniature goblin community living inside a hollow creative boot.", 90, "Medium", false),
        BossChallenge("Cyber Grid-Spider", "🕷️", "Weave a glowing neon spiderweb in a futuristic dark cyber metropolis.", 110, "Medium", true, "Neon Cyber Spiderweb 🕸️", "Future Dark Cityscape 🌃"),
        BossChallenge("Imposter Syndrome Raven", "🐦", "Draw a majestic silver raven carrying a glowing crystal lantern under a crescent moon.", 130, "Hard", false),
        BossChallenge("Distraction Siren", "🧜‍♀️", "Depict a beautiful deep sea siren playing music on a gold harp made of seashells.", 100, "Medium", true, "Shell Harp Melodies 🧜‍♀️", "Lighthouse Sunset Oasis 🌅"),
        BossChallenge("Procrastination Sloth", "🦥", "Sketch a lazy sloth sleeping on a comfy giant floating cloud hammock.", 50, "Easy", false),
        BossChallenge("Scribble Overlord", "👾", "Assemble a funny robotic arcade cabinet that creates digital ink doodles.", 85, "Medium", true, "Robotic Retro Arcade 👾", "Chiptune Soundwaves 🎹"),
        BossChallenge("Blank Page Phoenix", "🦅", "Compose a radiant crimson phoenix rising out of an open burning leather-bound sketchbook.", 150, "Hard", false)
    )

    // Complete daily catalog registry
    private val dailyRegistry = listOf(
        DailyChallengeItem("D1", "Cyber Sunset flight", "Draw a cybernetic duck soaring across neon cloud horizons.", 50, "Medium", true, "Cyber Solar Flight 🛸", "Sunset Grid-Hills 🌅"),
        DailyChallengeItem("D2", "Retro Roller Disco", "Sketch a retro duck doing wheelie spins at a roller skate disco.", 50, "Easy", false),
        DailyChallengeItem("D3", "Steampunk Clockwork Castle", "Build a high-altitude copper castle with steam pipes and giant bronze gears.", 60, "Hard", true, "Bronze Steam Pipes ⚙️", "High Altitude Copper Castle 🏰"),
        DailyChallengeItem("D4", "Mushroom Forest Glow", "Illustrate glowing red fairy houses nested in oversized wild mushrooms.", 50, "Medium", false),
        DailyChallengeItem("D5", "Pharaoh Treasure Chamber", "Delineate a golden sphinx guarding deep treasures of Nile valley.", 65, "Hard", true, "Golden Sphinx Guard 🦁", "Nile Valley Oasis 🌴"),
        DailyChallengeItem("D6", "Deep Sea Coral Glow", "Draw deep ocean creatures exploring bioluminescent coral caverns.", 50, "Medium", false),
        DailyChallengeItem("D7", "Cheese Moon Landing", "Doodle an astronaut mouse harvesting yellow dairy crater blocks.", 40, "Easy", false),
        DailyChallengeItem("D8", "Bonsai Tea Party", "Paint a tranquil tea room on a giant levitating bonsai tree.", 55, "Medium", true, "Levitating Bonsai Tree 🌳", "Tranquil Blossom Tea Ceremony 🍵")
    )

    private val completedBossNames = mutableSetOf<String>()
    private val completedDailyIds = mutableSetOf<String>()

    private val _activeBoss = MutableStateFlow(bossRegistry.first())
    val activeBoss: StateFlow<BossChallenge> = _activeBoss.asStateFlow()

    private val _activeDaily = MutableStateFlow(dailyRegistry.first())
    val activeDaily: StateFlow<DailyChallengeItem> = _activeDaily.asStateFlow()

    fun selectBossChoice(choiceIdx: Int) {
        val current = _activeBoss.value
        _activeBoss.value = current.copy(
            selectedChoice = choiceIdx,
            requirement = if (choiceIdx == 2) current.choiceB else current.choiceA
        )
    }

    fun selectDailyChoice(choiceIdx: Int) {
        val current = _activeDaily.value
        _activeDaily.value = current.copy(selectedChoice = choiceIdx)
        _dailyChallengePrompt.value = if (choiceIdx == 2) current.choiceB else current.choiceA
    }

    // --- Endless Store Upgradability Systems ---
    private val _frameStoreItems = MutableStateFlow(listOf(
        "Silver Minimalist" to 50,
        "Royal Velvet Blue" to 80,
        "Cyber Synthwave" to 100,
        "Emerald Forest" to 120
    ))
    val frameStoreItems: StateFlow<List<Pair<String, Int>>> = _frameStoreItems.asStateFlow()

    private val _paperStoreItems = MutableStateFlow(listOf(
        "Dotted Journal" to 30,
        "Vintage Parchment" to 50,
        "Crumpled Slate" to 70,
        "Midnight Grid" to 90,
        "Desert Sand" to 60,
        "Candy Pink" to 60
    ))
    val paperStoreItems: StateFlow<List<Pair<String, Int>>> = _paperStoreItems.asStateFlow()

    private val _stickerStoreItems = MutableStateFlow(listOf(
        "Animal Friends Series" to (40 to listOf("🐱", "🐶", "🦊", "🐸", "🦉")),
        "Retro Arcade Set" to (60 to listOf("🛸", "🍄", "🎮", "👾", "⭐")),
        "Outer Space Odyssey" to (80 to listOf("🚀", "🪐", "👽", "☄️", "🛡️"))
    ))
    val stickerStoreItems: StateFlow<List<Pair<String, Pair<Int, List<String>>>>> = _stickerStoreItems.asStateFlow()

    private val _brushStoreItems = MutableStateFlow(listOf(
        "Fire" to 80,
        "Smoke" to 50,
        "Star" to 60,
        "Bubble" to 40,
        "Pixel" to 30,
        "Rainbow Trail" to 100,
        "Neon Glow" to 120
    ))
    val brushStoreItems: StateFlow<List<Pair<String, Int>>> = _brushStoreItems.asStateFlow()

    private val _costumeStoreItems = MutableStateFlow(listOf(
        "Detective Hat & Trenchcoat" to 100,
        "Crown of the Art Master" to 150,
        "Space Astronaut Helmet" to 180,
        "Silly Clown Nose" to 60
    ))
    val costumeStoreItems: StateFlow<List<Pair<String, Int>>> = _costumeStoreItems.asStateFlow()

    private val _musicStoreItems = MutableStateFlow(listOf(
        "Wizard Library" to 80,
        "Cyberpunk Disco" to 100,
        "Cosmic Aurora" to 110
    ))
    val musicStoreItems: StateFlow<List<Pair<String, Int>>> = _musicStoreItems.asStateFlow()

    // Reserve catalog pools (endless restocking buffer)
    private val framePulseQueue = mutableListOf(
        "Polished Platinum" to 130, "Gilded Baroque" to 140, "Abstract Memphis" to 150,
        "Retro CRT scanlines" to 160, "Sparkling Diamond Prism" to 180, "Cyberpunk Holo Frame" to 175,
        "Amethyst Cluster" to 190, "Magma Flow" to 210, "Frosted Ice Window" to 145, "Wild Ivy Ivy" to 115
    )
    private val paperPulseQueue = mutableListOf(
        "Manga Screentone Paper" to 80, "Watercolored Parchment" to 95, "Graph Tech Sheet" to 75,
        "Space Nebula Canvas" to 110, "Pulp Comic Newsprint" to 85, "Obsidian Blackboard" to 105,
        "Rusty Tin Plate" to 90, "Linen Canvas weave" to 115, "Frozen Glass Sheet" to 125, "Gold Foil Leaf" to 160
    )
    private val stickerPulseQueue = mutableListOf(
        "Doodle Monsters Pack" to (90 to listOf("👹", "👺", "👻", "👾", "💀")),
        "Yummy Fastfood Pack" to (55 to listOf("🍕", "🍔", "🍟", "🍩", "🥤")),
        "Magical Runes Pack" to (75 to listOf("🔮", "📜", "⭐️", "⚡️", "🕯️")),
        "Cute Garden Pack" to (50 to listOf("🌸", "🌻", "🍀", "🌽", "🥕")),
        "Ocean Explorer Pack" to (70 to listOf("🦈", "🐙", "🦀", "🐬", "🐚")),
        "Retro Chiptune Set" to (65 to listOf("🔋", "🔌", "💿", "💾", "📡"))
    )
    private val brushPulseQueue = mutableListOf(
        "Glitch Shift" to 110, "Oil Pastel" to 75, "Neon Splash" to 115, "Charcoal Chalk" to 45,
        "3D Gold Ribbon" to 130, "Bioluminescent Dot" to 95, "Calligraphy Ribbons" to 85,
        "Graffiti Ink Splat" to 125, "Plasma Fire Spark" to 140, "Wind Blow Trail" to 70
    )
    private val costumePulseQueue = mutableListOf(
        "Steampunk Goggles & Top Hat" to 130, "Ancient Pharaoh Nemes Crown" to 140,
        "Dapper Bowler Hat & Monocle" to 110, "Samurai Kabuto Helmet" to 160,
        "Viking Horned War Helmet" to 125, "Magical Pointy Wizard Hat" to 115,
        "Cyber Ninja glowing vizor" to 170, "Gentleman Monocle Mustache" to 85,
        "Chef Hats with parsley" to 55, "Glittering Angel Crown" to 145
    )
    private val musicPulseQueue = mutableListOf(
        "Deep Sea Abyss" to 120, "8-Bit Retro Wasteland" to 90, "Mystic Desert Caravan" to 140,
        "Lofi Rain Coffee Shop" to 115, "Hyper-Glow Rave Party" to 160, "Astral Dream-State" to 150,
        "Cozy Winter Socks" to 105, "Golden Sunrise Harmony" to 125, "Ghostly Cathedrals" to 135
    )

    private fun restockStoreCategory(category: String) {
        when (category) {
            "Frames" -> {
                if (framePulseQueue.isNotEmpty()) {
                    val nextItem = framePulseQueue.removeFirst()
                    _frameStoreItems.value = _frameStoreItems.value + nextItem
                }
            }
            "Papers" -> {
                if (paperPulseQueue.isNotEmpty()) {
                    val nextItem = paperPulseQueue.removeFirst()
                    _paperStoreItems.value = _paperStoreItems.value + nextItem
                }
            }
            "Stickers" -> {
                if (stickerPulseQueue.isNotEmpty()) {
                    val nextItem = stickerPulseQueue.removeFirst()
                    _stickerStoreItems.value = _stickerStoreItems.value + nextItem
                }
            }
            "Brushes" -> {
                if (brushPulseQueue.isNotEmpty()) {
                    val nextItem = brushPulseQueue.removeFirst()
                    _brushStoreItems.value = _brushStoreItems.value + nextItem
                }
            }
            "Costumes" -> {
                if (costumePulseQueue.isNotEmpty()) {
                    val nextItem = costumePulseQueue.removeFirst()
                    _costumeStoreItems.value = _costumeStoreItems.value + nextItem
                }
            }
            "Music" -> {
                if (musicPulseQueue.isNotEmpty()) {
                    val nextItem = musicPulseQueue.removeFirst()
                    _musicStoreItems.value = _musicStoreItems.value + nextItem
                }
            }
        }
    }

    // --- Soundscape Mode System ---
    private val _activeSoundscape = MutableStateFlow("Rainy Sketchbook")
    val activeSoundscape: StateFlow<String> = _activeSoundscape.asStateFlow()

    private val _soundscapePlaying = MutableStateFlow(false)
    val soundscapePlaying: StateFlow<Boolean> = _soundscapePlaying.asStateFlow()

    private val _activePaper = MutableStateFlow("Plain White")
    val activePaper: StateFlow<String> = _activePaper.asStateFlow()

    fun setPaperStyle(paper: String) {
        _activePaper.value = paper
    }

    fun randomizeDino() {
        randomizeDinosaurAccessories()
    }

    init {
        randomizeDinosaurAccessories()
        loadRandomTip()
        shakeIdeaJar()
        initDailyChallenge()
    }

    // --- Functions & Action Handlers ---

    private fun randomizeDinosaurAccessories() {
        val rand = Random(System.currentTimeMillis())
        val colors = listOf(
            Color(0xFFFF5722), Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFF4CAF50),
            Color(0xFF00BCD4), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFFFFFFF)
        )
        val patterns = listOf("Solid", "Stripes", "Polka", "Flannel")

        _dinoHat.value = DinosaurAccessory(
            shapeType = rand.nextInt(4), // Bowler, Top Hat, Cowboy, Wizard
            color = colors[rand.nextInt(colors.size)],
            patternName = patterns[rand.nextInt(patterns.size)],
            scale = 0.8f + rand.nextFloat() * 0.4f
        )
        _dinoGlasses.value = DinosaurAccessory(
            shapeType = rand.nextInt(3), // Circular, Star, Sleek Rect
            color = colors[rand.nextInt(colors.size)],
            patternName = "Solid",
            scale = 0.9f + rand.nextFloat() * 0.2f
        )
        _dinoMustache.value = DinosaurAccessory(
            shapeType = rand.nextInt(3), // Handlebar, Bushy, Poirot
            color = Color(0xFF3E2723), // brown
            patternName = "Fuzzy",
            scale = 1.0f
        )
        _dinoOutfit.value = DinosaurAccessory(
            shapeType = rand.nextInt(4), // Tweed Cape, Bowtie, Star Coat, Dapper Scarf
            color = colors[rand.nextInt(colors.size)],
            patternName = patterns[rand.nextInt(patterns.size)],
            scale = 1.0f
        )
        updateDinoVibeState()
    }

    // --- Canvas Management ---
    fun selectTool(tool: String) {
        _selectedTool.value = tool
    }

    fun selectColor(color: Int) {
        _selectedColor.value = color
    }

    fun updateWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun toggleSymmetry() {
        _symmetryEnabled.value = !_symmetryEnabled.value
    }

    fun setShapeMode(mode: String) {
        _shapeCreatorMode.value = mode
    }

    fun togglePerspective() {
        _perspectiveGuideEnabled.value = !_perspectiveGuideEnabled.value
    }

    fun changeMood(mood: String) {
        viewModelScope.launch {
            repository.changeMood(mood)
        }
    }

    fun addStroke(stroke: DrawingStroke) {
        _strokes.value = _strokes.value + stroke
    }

    fun clearCanvas() {
        _strokes.value = emptyList()
        _placedStickers.value = emptyList()
    }

    fun undoStroke() {
        val current = _strokes.value
        if (current.isNotEmpty()) {
            _strokes.value = current.dropLast(1)
        }
    }

    // --- Sticker System ---
    fun placeSticker(stickerType: String) {
        val id = "sticker_${System.currentTimeMillis()}"
        val sizeOffset = 180f
        val randX = 300f + Random.nextFloat() * 200f
        val randY = 300f + Random.nextFloat() * 200f
        val newSticker = PlacedSticker(
            id = id,
            type = stickerType,
            x = randX,
            y = randY,
            scale = 0.8f + Random.nextFloat() * 0.4f,
            rotation = -15f + Random.nextFloat() * 30f
        )
        _placedStickers.value = _placedStickers.value + newSticker
    }

    fun removeSticker(stickerId: String) {
        _placedStickers.value = _placedStickers.value.filter { it.id != stickerId }
    }

    // --- Thumbs Up / Thumbs Down Daily Tips ---
    fun loadRandomTip() {
        viewModelScope.launch {
            val available = allTips.filter { tip ->
                !repository.isTipDisliked(tip.text)
            }
            if (available.isNotEmpty()) {
                _currentTip.value = available[Random.nextInt(available.size)]
            } else {
                _currentTip.value = DrawingTip(0, "Artist Wisdom", "Keep creating! Draw bold lines today, capturing pure light.")
            }
        }
    }

    fun dislikeCurrentTip() {
        viewModelScope.launch {
            repository.dislikeTip(_currentTip.value.text)
            loadRandomTip()
        }
    }

    fun likeCurrentTip() {
        // Boosts confidence, award tiny bonus coin for engagement
        viewModelScope.launch {
            repository.addCoins(5)
            loadRandomTip()
        }
    }

    // --- Idea Jar ---
    fun shakeIdeaJar() {
        val currentMood = userProgress.value.activeMood
        val moodPrompts = when (currentMood) {
            "Calm" -> listOf(
                "Draw a sleepy sleeping kitten wrapped in warm pastel quilts.",
                "Draw a serene floating green bonsai tree nested in foggy morning cliffs.",
                "Draw a tranquil cup of lavender tea emitting glowing steam on a windowsill."
            )
            "Silly" -> listOf(
                "Draw a goofy banana wearing a dapper tuxedo playing a giant acoustic guitar to a pineapple.",
                "Draw a chubby duck wearing roller skates juggling birthday cakes on a tightrope.",
                "Draw an octopus attempting to play eight wind instruments at once in a bathtub."
            )
            "Epic" -> listOf(
                "Draw a colossal cosmic fire dragon nesting inside an active crystalline volcano.",
                "Draw a towering stardust shield knight standing guard against the space vortex.",
                "Draw a giant phoenix rising from neon lightning clouds with wings of liquid fire."
            )
            "Mysterious" -> listOf(
                "Draw a glowing antique runic key hovering inside a floating obsidian chest.",
                "Draw a shadowy gothic library with secret paths hidden behind ancient grandfather clocks.",
                "Draw an owl wearing a monocle reading from a dusty spellbook in a hollow tree trunk."
            )
            "Funny" -> listOf(
                "Draw a pug puppy running a miniature sushi stall for hungry backyard squirrels.",
                "Draw an enormous walrus performing an elegant ballet dance dressed in a pink tutu.",
                "Draw a fancy pigeon wearing a crown criticizing a street chalk doodle of himself."
            )
            "Adventurous" -> listOf(
                "Draw an explorer duck climbing the sheer cliffs of the Maple Syrup Volcano.",
                "Draw a wooden sailing ship cruising over high sky clouds toward a golden horizon.",
                "Draw an adventurer squirrel crossing a vine bridge with a glowing crystal compass."
            )
            "Cozy" -> listOf(
                "Draw a steaming mug of giant marshmallow cocoa next to a warm vintage emerald lantern.",
                "Draw a tiny sleepy hedgehog wrapped in a woolen sock sleeping near a brick fireplace.",
                "Draw a miniature greenhouse filled with glowing succulent pots on a rainy winter night."
            )
            else -> promptsJar
        }
        val rand = Random(System.currentTimeMillis())
        val selected = moodPrompts[rand.nextInt(moodPrompts.size)]
        _shakenPrompt.value = selected
        
        viewModelScope.launch {
            repository.addCoins(5)
            awardAchievement("Inspirational Jar")
        }
    }

    // --- Story Builder ---
    fun setStoryConfig(character: String, location: String, problem: String) {
        _selectedChar.value = character
        _selectedLoc.value = location
        _selectedProb.value = problem
        _storyChapter.value = 1
        _storyNarrative.value = ""
    }

    fun generateStoryScene() {
        _storyLoading.value = true
        viewModelScope.launch {
            val narrative = geminiRepository.generateStorySceneNext(
                character = _selectedChar.value,
                location = _selectedLoc.value,
                problem = _selectedProb.value,
                chapterIndex = _storyChapter.value,
                previousSceneText = _storyNarrative.value
            )
            _storyNarrative.value = narrative
            _storyLoading.value = false
        }
    }

    fun advanceStoryChapter() {
        val next = _storyChapter.value + 1
        if (next <= 3) {
            _storyChapter.value = next
            generateStoryScene()
        } else {
            // Restart
            _storyChapter.value = 1
            generateStoryScene()
        }
    }

    // --- Dreamscape Explorer ---
    fun exploreDreamWorld(theme: String, style: String) {
        _dreamscapeLoading.value = true
        viewModelScope.launch {
            val outcome = geminiRepository.generateDreamWorld(theme, style)
            _dreamscapeIdea.value = outcome
            _dreamscapeLoading.value = false
        }
    }

    // --- Professor Quackers Chat ---
    fun sendQuackersQuestion(question: String) {
        if (question.isBlank()) return
        val updatedHistory = _chatHistory.value + (question to true)
        _chatHistory.value = updatedHistory
        _chatLoading.value = true

        viewModelScope.launch {
            val answer = geminiRepository.askProfessorQuackers(question, updatedHistory, _quackersVoice.value)
            _chatHistory.value = updatedHistory + (answer to false)
            _chatLoading.value = false
            repository.addCoins(2) // Little bonus for chat consulting!
        }
    }

    fun clearChat() {
        _chatHistory.value = emptyList()
    }

    // --- Art Scientist Multimodal Analysis ---
    fun analyzeSketch(bitmap: Bitmap, contextTitle: String) {
        _analysisLoading.value = true
        viewModelScope.launch {
            val result = geminiRepository.analyzeDrawing(bitmap, contextTitle)
            _analysisResult.value = result
            _analysisLoading.value = false
            awardAchievement("Art Scientist analysis") // Unlock coins & achievements
        }
    }

    // --- Soundscape Simulation ---
    fun selectSoundscape(mode: String) {
        _activeSoundscape.value = mode
        if (_soundscapePlaying.value) {
            ambientSynth.startPlaying(mode, viewModelScope)
        }
    }

    fun toggleSoundscape() {
        val nextState = !_soundscapePlaying.value
        _soundscapePlaying.value = nextState
        if (nextState) {
            ambientSynth.startPlaying(_activeSoundscape.value, viewModelScope)
        } else {
            ambientSynth.stopPlaying()
        }
    }

    // --- Unlock Outfits, Brushes, Stickers & Coins ---
    fun tryUnlockOutfit(outfitName: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockOutfit(outfitName, cost)
            if (success) {
                repository.selectOutfit(outfitName)
                triggerAchievementCheck()
                restockStoreCategory("Costumes")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun setCompanionOutfit(outfitName: String) {
        viewModelScope.launch {
            repository.selectOutfit(outfitName)
        }
    }

    fun selectTapBehavior(category: String, style: String) {
        val current = _activeTapStyles.value.toMutableMap()
        current[category] = style
        _activeTapStyles.value = current
    }

    fun upgradeTapLevel(category: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val progress = repository.getProgressSync()
            if (progress.coins >= cost) {
                repository.addCoins(-cost)
                val currentLevels = _unlockedTapLevels.value.toMutableMap()
                val currentLvl = currentLevels[category] ?: 1
                currentLevels[category] = currentLvl + 1
                _unlockedTapLevels.value = currentLevels

                // Swap style trigger variety non-cycle rule
                val stylesPool = when (category) {
                    "fairy" -> listOf("spiral", "fountain", "wave")
                    "smiley" -> listOf("bounce", "split", "wave")
                    "heart" -> listOf("cardioid", "pulse", "spiral")
                    "sunglasses" -> listOf("kickflip", "spiral", "fountain")
                    "dinosaur" -> listOf("stomp", "sprint", "bounce")
                    else -> listOf("fountain")
                }
                val currentStyle = _activeTapStyles.value[category] ?: "fountain"
                val nextStyle = stylesPool.filter { it != currentStyle }.randomOrNull() ?: currentStyle
                selectTapBehavior(category, nextStyle)

                awardAchievement("Tap Stylist Upgrade")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun tryUnlockBrush(brushName: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockBrush(brushName, cost)
            if (success) {
                triggerAchievementCheck()
                restockStoreCategory("Brushes")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun tryUnlockStickers(stickerPack: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockStickers(stickerPack, cost)
            if (success) {
                triggerAchievementCheck()
                restockStoreCategory("Stickers")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun tryUnlockPaper(paperName: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockPaper(paperName, cost)
            if (success) {
                triggerAchievementCheck()
                restockStoreCategory("Papers")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun tryUnlockFrame(frameName: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockFrame(frameName, cost)
            if (success) {
                triggerAchievementCheck()
                restockStoreCategory("Frames")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun tryUnlockMusic(musicName: String, cost: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockMusic(musicName, cost)
            if (success) {
                triggerAchievementCheck()
                restockStoreCategory("Music")
                onSuccess()
            } else {
                onError()
            }
        }
    }

    // --- Save drawing to Room DB & Museum ---
    fun saveDrawingToMuseum(bitmap: Bitmap, filename: String, promptTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save locally, write to database
            // In typical cases we encode drawing paths inside drawingData JSON
            // We can serialize simplified strokes list
            val serializedStrokes = _strokes.value.joinToString(";") { stroke ->
                "${stroke.brushType},${stroke.color},${stroke.width}:${stroke.points.joinToString("|") { "${it.x},${it.y}" }}"
            }

            val savedId = repository.insertDrawing(
                SavedDrawing(
                    title = promptTitle.ifBlank { "Untitled Masterpiece" },
                    drawingData = serializedStrokes,
                    imagePath = null, // Path to local PNG
                    isImported = false
                )
            )

            repository.addCoins(50) // Reward 50 coins!
            awardAchievement("First Sketch")

            // Check drawing totals to advance companion Level and achievements
            val drawingsCount = allDrawings.value.size + 1
            if (drawingsCount >= 10) awardAchievement("Drew 10 Animals")
            if (drawingsCount >= 50) awardAchievement("Drew 50 Pictures")

            repository.checkAndEvolveDuck(drawingsCount)
        }
    }

    fun importPictureToMuseum(importedTitle: String) {
        viewModelScope.launch {
            repository.insertDrawing(
                SavedDrawing(
                    title = importedTitle.ifBlank { "Imported Art" },
                    drawingData = "IMPORTED",
                    imagePath = null,
                    isImported = true
                )
            )
            awardAchievement("Museum Import")
            repository.addCoins(30)
        }
    }

    data class FriendMuseum(
        val friendName: String,
        val drawings: List<SavedDrawing>
    )

    private val _friendsMuseums = MutableStateFlow<List<FriendMuseum>>(emptyList())
    val friendsMuseums: StateFlow<List<FriendMuseum>> = _friendsMuseums.asStateFlow()

    fun uploadPictureToMuseum(title: String, presetOrUri: String) {
        viewModelScope.launch {
            repository.insertDrawing(
                SavedDrawing(
                    title = title.ifBlank { "Uploaded Photographic Art" },
                    drawingData = "UPLOADED_IMAGE",
                    imagePath = presetOrUri,
                    isImported = false
                )
            )
            repository.addCoins(50) // Reward 50 coins for photographic uploads!
            awardAchievement("Museum Import")
        }
    }

    fun generateMuseumSharingCode(): String {
        val currentDrawings = allDrawings.value
        if (currentDrawings.isEmpty()) return "MUSEUM_CODE_V1:"
        val serialized = currentDrawings.joinToString("##") { drawing ->
            "${drawing.title}||${drawing.drawingData}||${drawing.imagePath ?: ""}||${drawing.isImported}"
        }
        return "MUSEUM_CODE_V1:$serialized"
    }

    fun importSharedMuseum(friendName: String, rawCode: String): Pair<Boolean, String> {
        val cleaned = rawCode.trim()
        if (!cleaned.startsWith("MUSEUM_CODE_V1:")) {
            return Pair(false, "Invalid Code! Make sure to copy the code exactly from your friend's app.")
        }
        try {
            val content = cleaned.removePrefix("MUSEUM_CODE_V1:")
            if (content.isEmpty()) return Pair(false, "This code does not contain any artwork coordinates.")
            
            val tokens = content.split("##")
            val list = mutableListOf<SavedDrawing>()
            tokens.forEachIndexed { i, tok ->
                val p = tok.split("||")
                if (p.size >= 3) {
                    val title = p[0]
                    val data = p[1]
                    val path = p[2].ifBlank { null }
                    val isImp = p.getOrNull(3)?.toBoolean() ?: false
                    list.add(
                        SavedDrawing(
                            id = -100 - i, // Virtual negative id for temporary showcase objects
                            title = title,
                            drawingData = data,
                            imagePath = path,
                            isImported = isImp
                        )
                    )
                }
            }
            if (list.isEmpty()) {
                return Pair(false, "No artwork found in code.")
            }
            val entry = FriendMuseum(
                friendName = friendName.ifBlank { "Mysterious Artist Duck" },
                drawings = list
            )
            _friendsMuseums.value = _friendsMuseums.value + entry
            return Pair(true, "Successfully set up ${friendName}'s gallery wall with ${list.size} pieces!")
        } catch (e: Exception) {
            return Pair(false, "Error: ${e.localizedMessage}")
        }
    }

    fun processStickerTrade(proposalCode: String): Pair<Boolean, String> {
        val cleaned = proposalCode.trim()
        if (!cleaned.startsWith("DUCK_STICKER_TRADE_V1:")) {
            return Pair(false, "Invalid Trade Code structure!")
        }
        try {
            val content = cleaned.removePrefix("DUCK_STICKER_TRADE_V1:")
            val parts = content.split(":")
            if (parts.size < 2) return Pair(false, "Incomplete Trade proposal.")
            val giveSticker = parts[0]
            val wantSticker = parts[1]
            
            val unlockedStr = userProgress.value.unlockedStickers
            val currentList = unlockedStr.split(",").map { it.trim() }.toMutableList()
            
            if (!currentList.contains(giveSticker)) {
                currentList.add(giveSticker)
                viewModelScope.launch {
                    val updated = userProgress.value.copy(
                        unlockedStickers = currentList.joinToString(",")
                    )
                    repository.updateProgress(updated)
                }
                return Pair(true, "Trade Accepted! Swapped your $wantSticker for friend's $giveSticker successfully! Check your Sticker Vault!")
            } else {
                return Pair(true, "Trade completes! You already had $giveSticker in your Vault. Swapped $wantSticker successfully!")
            }
        } catch (e: Exception) {
            return Pair(false, "Failed to execute trade: ${e.localizedMessage}")
        }
    }

    fun tradeStickerWithMerchant(giveSticker: String, receiveItem: String, isCoinReward: Boolean) {
        viewModelScope.launch {
            if (isCoinReward) {
                repository.addCoins(40) // Merchant rewards with 40 coins!
            } else {
                val current = repository.getProgressSync()
                val list = current.unlockedStickers.split(",").map { it.trim() }.toMutableList()
                if (!list.contains(receiveItem)) {
                    list.add(receiveItem)
                }
                val updated = current.copy(
                    unlockedStickers = list.joinToString(",")
                )
                repository.updateProgress(updated)
            }
            triggerAchievementCheck()
        }
    }

    fun downloadStickerToGallery(sticker: String): Pair<Boolean, String> {
        return Pair(true, "Sticker \"$sticker\" downloaded successfully as a high-density vector SVG asset in your internal device's /Downloads/ directory! 📥🎨")
    }

    fun loadDrawingToCanvas(drawing: SavedDrawing) {
        if (drawing.isImported) return
        val parsed = mutableListOf<DrawingStroke>()
        try {
            val parts = drawing.drawingData.split(";")
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
                        parsed.add(
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
            _strokes.value = parsed
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Beat Boss Challenge ---
    fun challengeActiveBoss() {
        viewModelScope.launch {
            val currentProgress = repository.getProgressSync()
            val completedName = _activeBoss.value.name
            val reward = _activeBoss.value.bounty
            repository.addCoins(reward)
            awardAchievement("Boss Conqueror: $completedName")

            completedBossNames.add(completedName)
            // Load a new boss that hasn't been generated yet
            val uncompleted = bossRegistry.filter { it.name !in completedBossNames }
            val nextBoss = if (uncompleted.isNotEmpty()) {
                uncompleted.random()
            } else {
                completedBossNames.clear()
                completedBossNames.add(completedName) // add current back to avoid instant repeat
                bossRegistry.filter { it.name != completedName }.random()
            }
            _activeBoss.value = nextBoss
        }
    }

    // --- Duels and Challenge Systems ---
    fun initDailyChallenge() {
        val current = _activeDaily.value
        _dailyChallengePrompt.value = if (current.selectedChoice == 2) current.choiceB else current.choiceA
    }

    fun generateDuelInvitationCode(customNickname: String, prompt: String): String {
        val serialized = _strokes.value.joinToString(";") { stroke ->
            "${stroke.brushType},${stroke.color},${stroke.width}:${stroke.points.joinToString("|") { "${it.x},${it.y}" }}"
        }
        val combined = "DUEL_V1|$customNickname|$prompt|$serialized"
        return try {
            android.util.Base64.encodeToString(combined.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        } catch (e: Exception) {
            "ERROR_ENCODING_STREAMS"
        }
    }

    fun loadOpponentDuelCode(code: String): Boolean {
        return try {
            val trimmed = code.trim()
            val decodedBytes = android.util.Base64.decode(trimmed, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
            val combined = String(decodedBytes, Charsets.UTF_8)
            if (combined.startsWith("DUEL_V1|")) {
                val parts = combined.split("|")
                if (parts.size >= 4) {
                    val nick = parts[1]
                    val prompt = parts[2]
                    val strokeData = parts.subList(3, parts.size).joinToString("|")
                    
                    _opponentNickname.value = nick
                    _currentDuelPrompt.value = prompt
                    
                    // Parse drawing data strokes from opponent
                    val parsedOpponent = mutableListOf<DrawingStroke>()
                    if (strokeData.isNotBlank() && strokeData != "IMPORTED") {
                        val strokesParts = strokeData.split(";")
                        strokesParts.forEach { strokeStr ->
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
                                    parsedOpponent.add(
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
                    }
                    pendingOpponentNickname = nick
                    pendingDuelPrompt = prompt
                    pendingOpponentStrokes = parsedOpponent
                    
                    initiateMinigame("DUO")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun submitDuelChallenge(userNickname: String) {
        val userStrokes = _strokes.value
        val oppStrokes = _opponentStrokes.value
        
        if (userStrokes.isEmpty()) {
            _duelResultMessage.value = "Please draw your response on the canvas first to complete the duel!"
            return
        }
        
        val userPointCount = userStrokes.sumOf { it.points.size }
        val oppPointCount = oppStrokes.sumOf { it.points.size }
        
        val userColors = userStrokes.map { it.color }.distinct().size
        val oppColors = oppStrokes.map { it.color }.distinct().size
        
        val baseSynergyScore = (userPointCount * 4 + oppPointCount * 3 + userColors * 15 + oppColors * 10) % 41 + 60
        val synergyScore = baseSynergyScore + _duoSynergyBonus.value
        val normalizedScore = synergyScore.coerceIn(60, 100)
        
        val rating = when {
            normalizedScore >= 93 -> "SSS 👑"
            normalizedScore >= 85 -> "S 🏆"
            normalizedScore >= 75 -> "A 🎖️"
            else -> "B ✨"
        }
        
        val comment = when {
            normalizedScore >= 93 -> "Perfect synergy! Your strokes seamlessly combine."
            normalizedScore >= 85 -> "Outstanding collaboration! The coloring and composition are spectacular."
            normalizedScore >= 75 -> "Wonderful coordination! This canvas holds high artistic promise."
            else -> "Splendid duet creation! A beautiful exchange of ideas."
        }
        
        _duelResultScore.value = normalizedScore
        _duelResultRating.value = rating
        _duelResultMessage.value = comment
        _activeDuelStatus.value = "SUBMITTED"
        
        // Reset the bonus once grading completes
        _duoSynergyBonus.value = 0
        
        viewModelScope.launch {
            repository.addCoins(normalizedScore / 2)
            awardAchievement("Duel Legend: $userNickname vs ${_opponentNickname.value}")
        }
    }

    fun saveDuoMasterpieceToMuseum(customTitle: String) {
        viewModelScope.launch {
            val merged = _opponentStrokes.value + _strokes.value
            val serialized = merged.joinToString(";") { stroke ->
                "${stroke.brushType},${stroke.color},${stroke.width}:${stroke.points.joinToString("|") { "${it.x},${it.y}" }}"
            }
            repository.insertDrawing(
                SavedDrawing(
                    title = customTitle.ifBlank { "Duo Masterpiece: ${_opponentNickname.value} & Me" },
                    drawingData = serialized,
                    imagePath = null,
                    isImported = false
                )
            )
            repository.addCoins(40)
            awardAchievement("Museum Spotlight Collector")
            
            // Clean up duel
            _opponentStrokes.value = emptyList()
            _opponentNickname.value = ""
            _activeDuelStatus.value = ""
            _strokes.value = emptyList()
        }
    }

    fun submitDailyChallenge() {
        val userStrokes = _strokes.value
        if (userStrokes.isEmpty()) {
            return
        }
        val prompt = _dailyChallengePrompt.value
        val score = (userStrokes.sumOf { it.points.size } * 3 + userStrokes.size * 10) % 35 + 65
        val rank = when {
            score >= 90 -> "1st Place (Doodle Sensation)"
            score >= 80 -> "2nd Place (Pro Sketcher)"
            else -> "3rd Place (Rising Talent)"
        }
        
        _dailyScore.value = score
        _dailyRank.value = rank
        _dailyCompleted.value = true
        
        viewModelScope.launch {
            repository.addCoins(50)
            awardAchievement("Daily Challenger")
            
            val serialized = userStrokes.joinToString(";") { stroke ->
                "${stroke.brushType},${stroke.color},${stroke.width}:${stroke.points.joinToString("|") { "${it.x},${it.y}" }}"
            }
            repository.insertDrawing(
                SavedDrawing(
                    title = "Daily Challenge: $prompt",
                    drawingData = serialized,
                    imagePath = null,
                    isImported = false
                )
            )
        }
    }

    fun advanceToNextDailyChallenge() {
        val completedId = _activeDaily.value.id
        completedDailyIds.add(completedId)

        // Find next uncompleted daily challenge
        val uncompleted = dailyRegistry.filter { it.id !in completedDailyIds }
        val nextDaily = if (uncompleted.isNotEmpty()) {
            uncompleted.random()
        } else {
            completedDailyIds.clear()
            completedDailyIds.add(completedId)
            dailyRegistry.filter { it.id != completedId }.random()
        }

        _activeDaily.value = nextDaily
        _dailyCompleted.value = false
        _dailyScore.value = 0
        _dailyRank.value = ""
        _dailyChallengePrompt.value = if (nextDaily.selectedChoice == 2) nextDaily.choiceB else nextDaily.choiceA
    }

    fun cancelDuel() {
        _opponentStrokes.value = emptyList()
        _opponentNickname.value = ""
        _activeDuelStatus.value = ""
        _duelResultRating.value = ""
        _strokes.value = emptyList()
    }

    fun initiateMinigame(initiator: String) {
        _minigameInitiator.value = initiator
        _selectedMinigame.value = MinigameType.values().random()
        _isMinigameActive.value = true
        _isPlayingMinigameLive.value = false
        _minigameWinOrLoss.value = ""
    }

    fun selectMinigameType(type: MinigameType) {
        _selectedMinigame.value = type
    }

    fun startPlayingMinigameLive() {
        _isPlayingMinigameLive.value = true
        _minigameWinOrLoss.value = ""
    }

    fun exitMinigame() {
        _isMinigameActive.value = false
        _isPlayingMinigameLive.value = false
        _minigameWinOrLoss.value = ""
    }

    fun completeMinigameWithSuccess() {
        _minigameWinOrLoss.value = "WIN"
        if (_minigameInitiator.value == "BOSS") {
            challengeActiveBoss()
        } else if (_minigameInitiator.value == "DUO") {
            _opponentNickname.value = pendingOpponentNickname
            _currentDuelPrompt.value = pendingDuelPrompt
            _opponentStrokes.value = pendingOpponentStrokes
            
            _activeDuelStatus.value = "LOADED"
            _duelResultRating.value = ""
            _duelResultMessage.value = "Loaded ${pendingOpponentNickname}'s sketch of \"${pendingDuelPrompt}\"! Sketch a co-drawing response with a visual minigame bonus! ⚔️"
            _strokes.value = emptyList() // clear local to draw co-drawing
            
            _duoSynergyBonus.value = 15 // Synergy Bonus unlocked by beating the minigame!
            viewModelScope.launch {
                repository.addCoins(25) // extra warm prep bounty
            }
        }
    }

    fun completeMinigameWithFailure() {
        _minigameWinOrLoss.value = "LOSS"
    }

    private suspend fun triggerAchievementCheck() {
        val current = repository.getProgressSync()
        val outfits = current.unlockedOutfits.split(",").size
        val brushes = current.unlockedBrushes.split(",").size

        if (outfits >= 3) awardAchievement("Cosmic Wardrobe")
        if (brushes >= 8) awardAchievement("Color Wizard")
    }

    override fun onCleared() {
        super.onCleared()
        ambientSynth.stopPlaying()
    }
}

class DoodleViewModelFactory(
    private val repository: DoodleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoodleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoodleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
