package com.smartshop.app

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp

// This annotation tells Hilt to set up
// dependency injection for the whole app
@HiltAndroidApp
class SmartShopApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}