package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY uploadedAt DESC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE lastPlayedAt > 0 ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(songs: List<Song>)

    @Update
    suspend fun updateSong(song: Song)
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE videoId = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)
    
    @Query("UPDATE songs SET lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE videoId = :id")
    suspend fun updateLastPlayed(id: String, timestamp: Long)
}
