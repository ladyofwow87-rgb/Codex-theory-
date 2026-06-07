package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRepository {

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY") "" else key
    }

    suspend fun generateDreamWorld(theme: String, style: String): String = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty()) {
            return@withContext "You enter a shimmering realm of $theme drawn in lush $style art. Towering obsidian peaks drift weightlessly in a golden morning sky while small bioluminescent creatures hum in the whispering grasses."
        }

        val prompt = "Create a detailed, inspiring visual description (1-2 paragraphs) of an dream world landscape for artists to paint. Theme: $theme. Artistic Style: $style. Provide vivid color descriptors, interesting geographical anomalies, and mysterious creatures that exist there. High-fidelity artistic realism, no childish tropes."
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.8f, maxOutputTokens = 300)
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to extract dreamscape"
        } catch (e: Exception) {
            "An error occurred while calling Gemini: ${e.localizedMessage}. Imagine a beautiful crystal cavern shifting under $style lighting..."
        }
    }

    suspend fun askProfessorQuackers(question: String, history: List<Pair<String, Boolean>>, personality: String = "Scholarly"): String = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty()) {
            // Local high-fidelity offline responses if key missing
            return@withContext when {
                question.contains("wolf", ignoreCase = true) -> "Ah! Drawing a wolf requires capturing sleek, powerful predatory lines. Begin with overlapping chest/skull ovals!"
                question.contains("cloud", ignoreCase = true) -> "Clouds look physical when shaded from underneath! Keep tops pure white and color the bottom parts."
                else -> when (personality) {
                    "Quacky" -> "QUACK! Energetic greetings, best friend! 🦆 Let's create an AMAZING drawing! Scatter little happy lines, QUACK!"
                    "Sarcastic" -> "Oh, great. Another question. 🦆 Let me guess: you want to know how to draw circles? Pro tip: try not to draw eggs when aiming for perfect circles, Michelangelo!"
                    "Zen" -> "Inhale... exhale... let the lines flow from the shoulder like mountain streams. 🌿 Feel the quiet negative space. Let your wrist relax."
                    else -> "Greetings, distinguished scholar! 🎓 It is I, Professor Quackers, your wise sidekick duck tutor. Let's study fine art color theory, composition standards, and atmospheric lighting."
                }
            }
        }

        val conversationParts = mutableListOf<Part>()
        history.forEach { (text, isUser) ->
            // Simpler historical mapping
            conversationParts.add(Part(text = "${if (isUser) "User" else "Professor Quackers"}: $text"))
        }
        conversationParts.add(Part(text = "User: $question"))

        val instructionText = when (personality) {
            "Quacky" -> "You are Professor Quackers, an incredibly energetic, bubbling sidekick duck who is an expert artist! You sprinkle your highly supportive, fun drawing guidance with capital 'QUACK!' sounds, enthusiastic exclamation points, and warm feathery comments. You are the user's ultimate drawing cheerleader!"
            "Sarcastic" -> "You are Professor Quackers, a hilariously dry-witted, sarcastic, cynical (but secretly brilliant and supportive) art professor duck. You tease the user in a clever, humorous way about their drawing skills, and offer genius, sophisticated advice wrapped in cynical dry humor (but always make sure they learn visual concepts in the end)."
            "Zen" -> "You are Professor Quackers, a serene, calm, peaceful duck artist master who views sketching as a calming meditation. You guide the artist using beautiful tranquil words and relaxing nature metaphors (flowing water, quiet mountains), telling them to breathe, enjoy negative spaces, and let the wrist relaxed."
            else -> "You are Professor Quackers, a highly distinguished, scholarly, slightly eccentric duck artist and assistant sidekick. You provide helpful, structured, academic drawing and illustration tips using formal fine art concepts, color theory, and lighting guides in a witty, mature, sophisticated style (no infantile tropes)."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = conversationParts)),
            generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 400),
            systemInstruction = Content(parts = listOf(Part(text = instructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Quack! I couldn't divine that. Let's trace it again!"
        } catch (e: Exception) {
            "Professor Quackers whispers: I hit a cosmic block (${e.localizedMessage}). But remember, true art flows from the shoulder, not just the wrist!"
        }
    }

    suspend fun analyzeDrawing(bitmap: Bitmap, promptAddition: String): String = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty()) {
            return@withContext "Professor Quackers Art Science Lab (Offline Mode):\n- Brilliant structure of geometric lines detected.\n- Excellent tonal layout of strokes on the canvas.\n- Artist Tip: Experiment with high-contrast ambient occlusion shadows underneath the main forms to anchor your drawing in three-dimensional space!"
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val promptText = "You are an expert Art Scientist and illustrator. Analyze this drawing. Provide a highly constructive, professional review analyzing its form, shadow, perspective, or palette. Suggest one specific technique the artist could practice next to elevate it. Be inspiring, helpful, mature, and professional (avoid kiddie language). Keep it under 150 words. Additional context: $promptAddition"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(
                Part(text = promptText),
                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data))
            ))),
            generationConfig = GenerationConfig(temperature = 0.6f, maxOutputTokens = 300)
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I looked into the lens, but the shadows were too dense to analyze. Try boosting contrast and re-submitting!"
        } catch (e: Exception) {
            "Art analysis error: ${e.localizedMessage}. But looking closely at your strokes, the composition shows confident wrist gesture and excellent weight!"
        }
    }

    suspend fun generateStorySceneNext(character: String, location: String, problem: String, chapterIndex: Int, previousSceneText: String): String = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isEmpty()) {
            return@withContext when(chapterIndex) {
                1 -> "Chapter 1: The Arrival. Our protagonist $character arrives at $location. The air is thick with anticipation as they suddenly encounter the problem: $problem. They look to the sky, ready to draw their path."
                2 -> "Chapter 2: The Search. Stepping deeper into $location, $character tracks a faint glowing ribbon. They must resolve the crisis of $problem before the setting sun seals the portal forever."
                else -> "Chapter 3: The Discovery. A brilliant flash of gold reveals that $character solved the riddle. The twilight of $location glows softly, signaling a victorious end to this creative adventure."
            }
        }

        val prompt = if (chapterIndex == 1) {
            "Generate Chapter 1 of a creative drawing picture-book adventure. Character: $character. Location: $location. Problem faced: $problem. Write a compelling, highly imaginative, mature narration (3-4 sentences maximum) that sets the scene for an illustrator to draw on their canvas. Keep it visually descriptive and dramatic."
        } else {
            "This is Chapter $chapterIndex. Based on previous chapter's scene: '$previousSceneText'. Continue the journey of $character in $location as they try to overcome the obstacle: $problem. Write 3-4 highly narrative sentences. Bring the story closer to a resolution, leaving a visual prompt for drawing what happens."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.8f, maxOutputTokens = 250)
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "The story scrolls forward endlessly. Draw your next stride."
        } catch (e: Exception) {
            "Story Builder: Chapter $chapterIndex continues despite the cosmic tear. $character must press on through $location!"
        }
    }
}
