package com.replica.cleaner.ui.components

import com.replica.cleaner.l10n.tr

import android.graphics.Color as AndroidColor
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.replica.cleaner.R
import com.replica.cleaner.ads.AdsInitializer
import com.replica.cleaner.ads.InterstitialAdManager
import com.replica.cleaner.ads.findActivity
import kotlinx.coroutines.delay

/**
 * Exit / feature-transition ad.
 * 1) Full-screen interstitial when ready (largest).
 * 2) Else a reliable large adaptive banner that fills most of the screen width/height.
 * Close (X) after 3–5 seconds on the banner UI.
 *
 * Always paints an opaque edge-to-edge layer so Home chrome (AVG / UPGRADE) stays
 * behind the ad and never peeks above the status-bar / interstitial letterbox.
 */
@Composable
fun TransitionAdOverlay(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val density = LocalDensity.current
    val closeAfterMs = remember { listOf(3_000L, 4_000L, 5_000L).random() }

    var mode by remember { mutableStateOf(Mode.Loading) }
    var canClose by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf((closeAfterMs / 1000L).toInt()) }
    var adLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }
    val unitId = remember { context.getString(R.string.admob_banner_home) }

    DisposableEffect(Unit) {
        AdsInitializer.init(context)
        InterstitialAdManager.preload(context)
        onDispose { }
    }

    // Prefer full-screen interstitial; fall back to large banner overlay.
    LaunchedEffect(Unit) {
        val shown = InterstitialAdManager.awaitShow(
            activity = activity,
            premium = false,
            timeoutMs = 2_500L,
            onContinue = onDismiss
        )
        if (shown) {
            mode = Mode.Interstitial
            return@LaunchedEffect
        }
        mode = Mode.LargeBanner

        var left = closeAfterMs
        while (left > 0) {
            secondsLeft = ((left + 999) / 1000).toInt().coerceAtLeast(1)
            delay(250)
            left -= 250
        }
        secondsLeft = 0
        canClose = true
    }

    Dialog(
        onDismissRequest = {
            if (mode == Mode.LargeBanner && canClose) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = mode == Mode.LargeBanner && canClose,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        SideEffect {
            val window = (dialogView.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = AndroidColor.BLACK
            window.navigationBarColor = AndroidColor.BLACK
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            WindowCompat.getInsetsController(window, dialogView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (mode) {
                Mode.Loading, Mode.Interstitial -> {
                    // Solid black under the AdMob interstitial so AVG / UPGRADE never show through.
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mode == Mode.Loading) {
                            CircularProgressIndicator(color = Color.White.copy(alpha = 0.75f))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = tr("Loading ad…"),
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Mode.LargeBanner -> {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(top = 8.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val adWidthDp = maxWidth.value.toInt().coerceAtLeast(320)
                        // Large but within AdMob-safe range so test/real ads actually load.
                        val maxH = (maxHeight.value * 0.75f).toInt().coerceIn(320, 450)
                        val adWidthPx = with(density) { maxWidth.roundToPx() }

                        key(adWidthDp, maxH) {
                            AndroidView(
                                modifier = Modifier.fillMaxWidth(),
                                factory = { ctx ->
                                    LinearLayout(ctx).apply {
                                        orientation = LinearLayout.VERTICAL
                                        gravity = Gravity.CENTER_HORIZONTAL
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )

                                        // Primary: large inline adaptive (fills most of the screen).
                                        addView(
                                            AdView(ctx).apply {
                                                setAdSize(
                                                    AdSize.getInlineAdaptiveBannerAdSize(
                                                        adWidthDp,
                                                        maxH
                                                    )
                                                )
                                                adUnitId = unitId
                                                layoutParams = LinearLayout.LayoutParams(
                                                    adWidthPx,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                ).apply { gravity = Gravity.CENTER_HORIZONTAL }
                                                adListener = object : AdListener() {
                                                    override fun onAdLoaded() {
                                                        adLoaded = true
                                                        adFailed = false
                                                    }

                                                    override fun onAdFailedToLoad(e: LoadAdError) {
                                                        // Fall through — MREC below may still load.
                                                    }
                                                }
                                                loadAd(AdRequest.Builder().build())
                                            }
                                        )

                                        // Extra height: reliable 300x250 MREC under it.
                                        addView(
                                            FrameLayout(ctx).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                ).apply {
                                                    topMargin =
                                                        (12 * ctx.resources.displayMetrics.density).toInt()
                                                    gravity = Gravity.CENTER_HORIZONTAL
                                                }
                                                addView(
                                                    AdView(ctx).apply {
                                                        setAdSize(AdSize.MEDIUM_RECTANGLE)
                                                        adUnitId = unitId
                                                        layoutParams = FrameLayout.LayoutParams(
                                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            Gravity.CENTER
                                                        )
                                                        adListener = object : AdListener() {
                                                            override fun onAdLoaded() {
                                                                adLoaded = true
                                                                adFailed = false
                                                            }

                                                            override fun onAdFailedToLoad(
                                                                e: LoadAdError
                                                            ) {
                                                                if (!adLoaded) adFailed = true
                                                            }
                                                        }
                                                        loadAd(AdRequest.Builder().build())
                                                    }
                                                )
                                            }
                                        )
                                    }
                                },
                                onRelease = { root ->
                                    fun wipe(v: ViewGroup) {
                                        for (i in 0 until v.childCount) {
                                            when (val c = v.getChildAt(i)) {
                                                is AdView -> c.destroy()
                                                is ViewGroup -> wipe(c)
                                            }
                                        }
                                    }
                                    wipe(root)
                                }
                            )
                        }

                        if (!adLoaded && !adFailed) {
                            CircularProgressIndicator(
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        if (adFailed && !adLoaded) {
                            Text(
                                text = tr("Ad unavailable — tap X when ready"),
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    // Close always becomes available after countdown (even if still loading).
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(12.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .then(
                                if (canClose) Modifier.clickable(onClick = onDismiss)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (canClose) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = tr("Close ad"),
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Text(
                                text = "$secondsLeft",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class Mode {
    Loading,
    Interstitial,
    LargeBanner
}
