package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application class for ArrowCube.
 * Initializes the Google Mobile Ads SDK to prepare for rewarded video ads,
 * interstitials, and banners.
 */
class ArrowCubeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Google Mobile Ads SDK in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@ArrowCubeApplication) { initializationStatus ->
                    Log.d("ArrowCubeApp", "Google Mobile Ads SDK Initialized: $initializationStatus")
                }
            } catch (e: Exception) {
                Log.e("ArrowCubeApp", "Failed to initialize Google Mobile Ads SDK", e)
            }
        }
    }
}
