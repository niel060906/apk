package com.example.player.service

import android.media.AudioManager
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService(), AudioManager.OnAudioFocusChangeListener {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var audioManager: AudioManager? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
            
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setAudioSessionId(C.AUDIO_SESSION_ID_GENERATE)
            .build()

        player?.let {
            mediaSession = MediaSession.Builder(this, it).build()
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.volume = 1f
                player?.play()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                player?.pause()
                abandonAudioFocus()
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        } catch (e: Exception) {
            Log.e("PlaybackService", "Failed to request audio focus", e)
        }
    }

    private fun abandonAudioFocus() {
        try {
            audioManager?.abandonAudioFocus(this)
        } catch (e: Exception) {
            Log.e("PlaybackService", "Failed to abandon audio focus", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run { release() }
        mediaSession = null
        player?.run { release() }
        player = null
        abandonAudioFocus()
        super.onDestroy()
    }
}
