package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class WorldBooksApp : Application() {
    companion object {
        lateinit var instance: WorldBooksApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:790757231685:android:6a39f8187b6642a8bb7d16")
                    .setProjectId("worldbooks-applet")
                    .setApiKey("AIzaSyDummyKeyForGoogleServicesBuild12345")
                    .setStorageBucket("worldbooks-applet.appspot.com")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("WorldBooksApp", "Firebase initialized with fallback options")
            } else {
                Log.d("WorldBooksApp", "Firebase initialized successfully via google-services")
            }
        } catch (e: Exception) {
            Log.e("WorldBooksApp", "Firebase init warning: ${e.message}", e)
        }
    }
}
