package com.example

import android.app.Application
import android.util.Log
import com.example.di.AppContainer
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        
        try {
            // Firebase will auto-initialize from google-services.json
            // ✅ REMOVED hardcoded credentials - use google-services.json instead
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainApplication", "Firebase initialization error", e)
        }
    }
}
