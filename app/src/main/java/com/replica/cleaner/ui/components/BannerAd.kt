package com.replica.cleaner.ui.components

import com.replica.cleaner.l10n.tr

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.replica.cleaner.R
import com.replica.cleaner.ads.AdsInitializer
import com.replica.cleaner.ui.theme.LocalCleanerColors

/**
 * Adaptive banner for Home (above bottom nav). Hidden for premium users by the caller.
 * Uses AdMob; ships with Google test unit IDs so a real ad creative appears in debug.
 */
@Composable
fun HomeBannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String? = null
) {
    val context = LocalContext.current
    val colors = LocalCleanerColors.current
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val unitId = adUnitId ?: context.getString(R.string.admob_banner_home)

    DisposableEffect(Unit) {
        AdsInitializer.init(context)
        onDispose { }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(colors.card),
        contentAlignment = Alignment.Center
    ) {
        if (!failed) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { ctx ->
                    val density = ctx.resources.displayMetrics
                    val adWidth = (density.widthPixels / density.density).toInt()
                    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth)
                    AdView(ctx).apply {
                        setAdSize(adSize)
                        this.adUnitId = unitId
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                loaded = true
                                failed = false
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                failed = true
                                loaded = false
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { /* keep same AdView */ }
            )
        }

        if (!loaded && !failed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tr("Loading ad…"),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
