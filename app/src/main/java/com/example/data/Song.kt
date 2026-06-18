package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    @get:PropertyName("video_id") @set:PropertyName("video_id")
    var videoId: String = "",
    
    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",
    
    @get:PropertyName("artist") @set:PropertyName("artist")
    var artist: String = "",
    
    @get:PropertyName("duration") @set:PropertyName("duration")
    var duration: Long = 0,
    
    @get:PropertyName("catbox_mp3") @set:PropertyName("catbox_mp3")
    var catboxMp3: String = "",
    
    @get:PropertyName("catbox_thumb") @set:PropertyName("catbox_thumb")
    var catboxThumb: String = "",
    
    @get:PropertyName("telegram_message_id") @set:PropertyName("telegram_message_id")
    var telegramMessageId: String = "",
    
    @get:PropertyName("channel_post_link") @set:PropertyName("channel_post_link")
    var channelPostLink: String = "",
    
    @get:PropertyName("uploaded_at") @set:PropertyName("uploaded_at")
    var uploadedAt: String = "",
    
    // Local specific fields
    var isFavorite: Boolean = false,
    var lastPlayedAt: Long = 0,
    var playCount: Int = 0
)
