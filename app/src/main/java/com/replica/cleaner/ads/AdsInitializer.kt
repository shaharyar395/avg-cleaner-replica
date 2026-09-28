package com.replica.cleaner.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.atomic.AtomicBoolean

/** Initializes the Google Mobile Ads SDK once per process. */
object AdsInitializer {
    private val started = AtomicBoolean(false)

    fun init(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val app = context.applicationContext
        MobileAds.initialize(app) {
            // Ready — preload an interstitial for feature transitions.
            InterstitialAdManager.preload(app)
        }
    }

    fun activity(context: Context): Activity? =
        context as? Activity
}
