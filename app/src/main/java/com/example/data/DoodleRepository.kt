package com.example.data

import kotlinx.coroutines.flow.Flow

class DoodleRepository(private val dao: DoodleDao) {
    val allDrawings: Flow<List<SavedDrawing>> = dao.getAllDrawings()
    val userProgress: Flow<UserProgress?> = dao.getUserProgress()

    suspend fun insertDrawing(drawing: SavedDrawing): Long {
        return dao.insertDrawing(drawing)
    }

    suspend fun deleteDrawing(drawing: SavedDrawing) {
        dao.deleteDrawing(drawing)
    }

    suspend fun getProgressSync(): UserProgress {
        var progress = dao.getUserProgressSync()
        if (progress == null) {
            progress = UserProgress()
            dao.insertUserProgress(progress)
        }
        return progress
    }

    suspend fun updateProgress(progress: UserProgress) {
        dao.insertUserProgress(progress)
    }

    suspend fun addCoins(amount: Int) {
        val current = getProgressSync()
        val updated = current.copy(coins = current.coins + amount)
        dao.insertUserProgress(updated)
    }

    suspend fun changeMood(newMood: String) {
        val current = getProgressSync()
        val updated = current.copy(activeMood = newMood)
        dao.insertUserProgress(updated)
    }

    suspend fun unlockOutfit(outfitName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedOutfits.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != "None" }.toMutableList()
            if (!list.contains(outfitName)) {
                list.add(outfitName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedOutfits = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    suspend fun selectOutfit(outfitName: String) {
        val current = getProgressSync()
        val updated = current.copy(activeOutfit = outfitName)
        dao.insertUserProgress(updated)
    }

    // Unlocking wild brushes in Doodle Studio
    suspend fun unlockBrush(brushName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedBrushes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(brushName)) {
                list.add(brushName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedBrushes = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    // Unlocking stickers in Doodle Studio
    suspend fun unlockStickers(stickerPackName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedStickers.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(stickerPackName)) {
                list.add(stickerPackName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedStickers = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    suspend fun unlockPaper(paperName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedPaperTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(paperName)) {
                list.add(paperName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedPaperTypes = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    suspend fun unlockFrame(frameName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedFrames.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(frameName)) {
                list.add(frameName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedFrames = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    suspend fun unlockMusic(musicName: String, cost: Int): Boolean {
        val current = getProgressSync()
        if (current.coins >= cost) {
            val list = current.unlockedMusic.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(musicName)) {
                list.add(musicName)
                val updated = current.copy(
                    coins = current.coins - cost,
                    unlockedMusic = list.joinToString(",")
                )
                dao.insertUserProgress(updated)
                return true
            }
        }
        return false
    }

    suspend fun awardAchievement(achievement: String): Boolean {
        val current = getProgressSync()
        val list = current.unlockedAchievements.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        if (!list.contains(achievement)) {
            list.add(achievement)
            // Award coins for unlocking achievements
            val updated = current.copy(
                unlockedAchievements = list.joinToString(","),
                coins = current.coins + 75
            )
            dao.insertUserProgress(updated)
            return true
        }
        return false
    }

    // Evolve the duck companion over time as you save more drawings or finish challenges
    suspend fun checkAndEvolveDuck(sketchesCount: Int): Boolean {
        val current = getProgressSync()
        val targetLevel = when {
            sketchesCount >= 15 -> 3 // Space Duck
            sketchesCount >= 7 -> 2  // Inventor Duck
            sketchesCount >= 3 -> 1  // Explorer Duck
            else -> 0                // Tiny Duckling
        }
        if (targetLevel > current.duckLevel) {
            val updated = current.copy(duckLevel = targetLevel)
            dao.insertUserProgress(updated)
            return true
        }
        return false
    }

    suspend fun isTipDisliked(tipText: String): Boolean {
        return dao.isTipDisliked(tipText)
    }

    suspend fun dislikeTip(tipText: String) {
        dao.insertDislikedTip(DislikedTip(tipText))
    }
}
