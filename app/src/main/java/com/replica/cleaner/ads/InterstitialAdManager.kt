package com.replica.cleaner.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.replica.cleaner.R
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

/**
 * Preloads and shows full-screen AdMob interstitial ads (true phone-screen size).
 */
object InterstitialAdManager {
    private const val COOLDOWN_MS = 4_000L

    private var interstitial: InterstitialAd? = null
    private val loading = AtomicBoolean(false)
    private var lastShownAt = 0L

    fun isReady(): Boolean = interstitial != null

    fun preload(context: Context) {
        AdsInitializer.init(context)
        if (interstitial != null || loading.get()) return
        if (!loading.compareAndSet(false, true)) return

        val appCtx = context.applicationContext
        val unitId = appCtx.getString(R.string.admob_interstitial)
        InterstitialAd.load(
            appCtx,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading.set(false)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    loading.set(false)
                }
            }
        )
    }

    fun onCooldown(): Boolean {
        val now = SystemClock.elapsedRealtime()
        return now - lastShownAt < COOLDOWN_MS
    }

    /**
     * Shows a full-screen interstitial if ready.
     * @return true if showing (wait for [onContinue] on dismiss).
     */
    fun tryShow(
        activity: Activity?,
        premium: Boolean,
        skipCooldown: Boolean = false,
        onContinue: () -> Unit
    ): Boolean {
        if (premium || activity == null || activity.isFinishing) return false
        if (!skipCooldown && onCooldown()) return false

        val ad = interstitial ?: run {
            preload(activity)
            return false
        }

        interstitial = null
        lastShownAt = SystemClock.elapsedRealtime()

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload(activity)
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preload(activity)
                onContinue()
            }
        }

        return try {
            ad.show(activity)
            true
        } catch (_: Exception) {
            preload(activity)
            false
        }
    }

    /**
     * Waits for an interstitial to load, then shows it full-screen.
     * @return true if an interstitial was shown.
     */
    suspend fun awaitShow(
        activity: Activity?,
        premium: Boolean = false,
        timeoutMs: Long = 3_000L,
        onContinue: () -> Unit
    ): Boolean {
        if (premium || activity == null || activity.isFinishing) return false
        if (tryShow(activity, premium, skipCooldown = true, onContinue = onContinue)) return true

        preload(activity)
        val start = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            delay(120)
            if (tryShow(activity, premium, skipCooldown = true, onContinue = onContinue)) {
                return true
            }
        }
        return false
    }

    fun markShown() {
        lastShownAt = SystemClock.elapsedRealtime()
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
