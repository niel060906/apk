package com.example.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.Song
import com.example.player.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(
    private val context: Context
) : ViewModel() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val activePlaylist = mutableListOf<Song>()
    private var playerListener: Player.Listener? = null

    init {
        initializeController()
        startProgressTracker()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    controller = controllerFuture?.get()
                    
                    playerListener = object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            mediaItem?.mediaId?.let { mediaId ->
                                _currentSong.value = activePlaylist.find { it.videoId == mediaId }
                            }
                        }
                        
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
                            }
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            _errorMessage.value = error.message ?: "Playback error occurred"
                        }
                    }
                    
                    controller?.addListener(playerListener!!)
                    _isReady.value = true
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to initialize player: ${e.message}"
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0, onError: (String) -> Unit = {}) {
        // Check if player is ready
        if (!_isReady.value) {
            val error = "Player initializing. Please wait..."
            _errorMessage.value = error
            onError(error)
            return
        }
        
        if (controller == null) {
            val error = "Player not initialized. Please try again."
            _errorMessage.value = error
            onError(error)
            return
        }
        
        // Validate songs
        val validSongs = songs.filter { 
            it.catboxMp3.isNotEmpty() && it.catboxThumb.isNotEmpty()
        }
        
        if (validSongs.isEmpty()) {
            val error = "No valid songs to play. Check audio/image URLs."
            _errorMessage.value = error
            onError(error)
            return
        }

        if (startIndex >= validSongs.size) {
            val error = "Invalid start index"
            _errorMessage.value = error
            onError(error)
            return
        }

        activePlaylist.clear()
        activePlaylist.addAll(validSongs)
        
        val mediaItems = validSongs.mapNotNull { song ->
            try {
                val audioUri = Uri.parse(song.catboxMp3)
                val thumbUri = Uri.parse(song.catboxThumb)
                
                // Validate URI scheme
                if (audioUri.scheme != "http" && audioUri.scheme != "https") {
                    _errorMessage.value = "Invalid audio URL for ${song.title}"
                    return@mapNotNull null
                }
                
                val metadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.artist)
                    .setArtworkUri(thumbUri)
                    .setMediaId(song.videoId)
                    .build()
                    
                MediaItem.Builder()
                    .setMediaId(song.videoId)
                    .setUri(audioUri)
                    .setMediaMetadata(metadata)
                    .build()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to parse URLs for ${song.title}: ${e.message}"
                null
            }
        }
        
        if (mediaItems.isEmpty()) {
            val error = "No valid media items to play"
            _errorMessage.value = error
            onError(error)
            return
        }

        try {
            controller?.setMediaItems(mediaItems, startIndex, 0L)
            controller?.prepare()
            controller?.play()
            _errorMessage.value = null
        } catch (e: Exception) {
            val error = "Failed to play: ${e.message}"
            _errorMessage.value = error
            onError(error)
        }
    }

    fun play() { 
        if (controller != null) {
            controller?.play()
        } else {
            _errorMessage.value = "Player not initialized"
        }
    }

    fun pause() { 
        controller?.pause()
    }

    fun next() { 
        controller?.seekToNextMediaItem()
    }

    fun prev() { 
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) { 
        controller?.seekTo(position)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun startProgressTracker() {
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (_isPlaying.value && controller != null) {
                    _currentPosition.value = controller?.currentPosition?.coerceAtLeast(0L) ?: 0L
                    _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
                }
                delay(500L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerListener?.let { controller?.removeListener(it) }
        controller?.release()
        controllerFuture = null
    }
}
