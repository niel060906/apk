package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Song
import com.example.data.repository.SongRepository
import com.example.di.AppContainer
import com.example.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val songRepository: SongRepository,
    val playerController: PlayerController
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allSongs: StateFlow<List<Song>> = songRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = songRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val recentlyPlayed: StateFlow<List<Song>> = songRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchResults: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) emptyList()
        else songs.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.artist.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _showFullPlayer = MutableStateFlow(false)
    val showFullPlayer = _showFullPlayer.asStateFlow()

    init {
        viewModelScope.launch {
            songRepository.syncWithRemote()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song) {
        val list = allSongs.value.ifEmpty { listOf(song) }
        val index = list.indexOfFirst { it.videoId == song.videoId }.takeIf { it >= 0 } ?: 0
        playerController.playSongs(list, index)
        
        viewModelScope.launch {
            songRepository.markAsPlayed(song.videoId)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(song.videoId, !song.isFavorite)
        }
    }

    fun openFullPlayer() { _showFullPlayer.value = true }
    fun closeFullPlayer() { _showFullPlayer.value = false }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(container.songRepository, container.playerController) as T
        }
    }
}
