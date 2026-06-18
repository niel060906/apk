package com.example.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.Song
import com.example.player.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerController(
    private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val activePlaylist = mutableListOf<Song>()

    init {
        initializeController()
        startProgressTracker()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                controller = controllerFuture?.get()
                controller?.addListener(object : Player.Listener {
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
                })
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        activePlaylist.clear()
        activePlaylist.addAll(songs)
        
        val mediaItems = songs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(if (song.catboxThumb.isNotEmpty()) Uri.parse(song.catboxThumb) else Uri.EMPTY)
                .build()
                
            MediaItem.Builder()
                .setMediaId(song.videoId)
                .setUri(if (song.catboxMp3.isNotEmpty()) Uri.parse(song.catboxMp3) else Uri.EMPTY)
                .setMediaMetadata(metadata)
                .build()
        }
        
        controller?.setMediaItems(mediaItems, startIndex, 0L)
        controller?.prepare()
        controller?.play()
    }

    fun play() { controller?.play() }
    fun pause() { controller?.pause() }
    fun next() { controller?.seekToNextMediaItem() }
    fun prev() { controller?.seekToPreviousMediaItem() }
    fun seekTo(position: Long) { controller?.seekTo(position) }

    private fun startProgressTracker() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (_isPlaying.value) {
                    _currentPosition.value = controller?.currentPosition?.coerceAtLeast(0L) ?: 0L
                    _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
                }
                delay(500L)
            }
        }
    }
}
