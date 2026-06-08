package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "podcast_episodes")
data class PodcastEpisode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val scriptText: String,
    val duration: Int, // Dynamic duration reading in seconds
    val category: String, // Topic, e.g., "El Gran Monarca", "El Papa Santo", etc.
    val isFavorite: Boolean = false
)

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcast_episodes ORDER BY date DESC")
    fun getAllEpisodes(): Flow<List<PodcastEpisode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: PodcastEpisode): Long

    @Query("DELETE FROM podcast_episodes WHERE id = :id")
    suspend fun deleteEpisodeById(id: Long)

    @Query("UPDATE podcast_episodes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM podcast_episodes")
    suspend fun getEpisodeCount(): Int
}

@Database(entities = [PodcastEpisode::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "podcast_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
