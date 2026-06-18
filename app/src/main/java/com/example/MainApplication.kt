package com.example

import android.app.Application
import com.example.di.AppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyChJUtOEnYGW9iryHYqst2ql-oNDdJyysw")
                    .setApplicationId("1:457308432728:web:01568ae63de26444105d2d")
                    .setProjectId("marketplace-ea770")
                    .setStorageBucket("marketplace-ea770.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
