package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.SongRepository
import com.example.player.PlayerController
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    val database by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "music_premium_db"
        ).fallbackToDestructiveMigration().build()
    }

    val firestore by lazy { FirebaseFirestore.getInstance() }

    val songRepository by lazy {
        SongRepository(database.songDao(), firestore)
    }

    val playerController by lazy {
        PlayerController(context.applicationContext)
    }
}
