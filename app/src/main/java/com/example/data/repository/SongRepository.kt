package com.example.data.repository

import android.util.Log
import com.example.data.Song
import com.example.data.local.SongDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SongRepository(
    private val songDao: SongDao,
    private val firestore: FirebaseFirestore
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()
    fun getRecentlyPlayed(): Flow<List<Song>> = songDao.getRecentlyPlayed()
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    suspend fun syncWithRemote() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("songs").get().await()
                val remoteSongs = snapshot.documents.mapNotNull { it.toObject(Song::class.java) }
                if (remoteSongs.isNotEmpty()) {
                    songDao.insertSongs(remoteSongs)
                }
            } catch (e: Exception) {
                Log.e("SongRepository", "Error syncing with remote", e)
            }
        }
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            songDao.updateFavoriteStatus(id, isFavorite)
        }
    }

    suspend fun markAsPlayed(id: String) {
        withContext(Dispatchers.IO) {
            songDao.updateLastPlayed(id, System.currentTimeMillis())
        }
    }
}
