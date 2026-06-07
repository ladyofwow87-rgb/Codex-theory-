package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_drawings")
data class SavedDrawing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val drawingData: String, // Textual coordinates representing drawn paths
    val imagePath: String?, // Uri/local disk path to saved drawing PNG thumbnail
    val timestamp: Long = System.currentTimeMillis(),
    val isImported: Boolean = false
)

@Entity(tableName = "disliked_tips")
data class DislikedTip(
    @PrimaryKey val tipText: String
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 100,
    val duckLevel: Int = 0, // 0: Tiny Duckling, 1: Explorer Duck, 2: Inventor Duck, 3: Space Duck
    val activeOutfit: String = "None", // From Cowboy, Pirate, Astronaut, Viking, etc.
    val unlockedOutfits: String = "None", // Comma-separated list
    val unlockedBrushes: String = "Pencil,Marker,Crayon,Eraser,Color Picker", // Comma-separated
    val unlockedStickers: String = "Smiley faces,Stars,Trees", // Comma-separated
    val unlockedPaperTypes: String = "Plain White,Dotted Journal", // Comma-separated
    val unlockedFrames: String = "None,Renaissance Gold,Neo-Neon Guard,Rustic Redwood,Obsidian Gallery", // Comma-separated
    val unlockedMusic: String = "Rainy Sketchbook,Campfire,Space Journey,Ocean Drift,Retro Arcade,Lofi Meadow,Zen Bamboo Stream", // Comma-separated
    val unlockedAchievements: String = "", // Comma-separated
    val activeMood: String = "Calm" // Calm, Silly, Epic, Mysterious, Funny, Adventurous, Cozy
)

@Dao
interface DoodleDao {
    @Query("SELECT * FROM saved_drawings ORDER BY timestamp DESC")
    fun getAllDrawings(): Flow<List<SavedDrawing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: SavedDrawing): Long

    @Delete
    suspend fun deleteDrawing(drawing: SavedDrawing)

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressSync(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Query("SELECT EXISTS(SELECT 1 FROM disliked_tips WHERE tipText = :text)")
    suspend fun isTipDisliked(text: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDislikedTip(tip: DislikedTip)
}

@Database(entities = [SavedDrawing::class, DislikedTip::class, UserProgress::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doodleDao(): DoodleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doodle_duck_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
