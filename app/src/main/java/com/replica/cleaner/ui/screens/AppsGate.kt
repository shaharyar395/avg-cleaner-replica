package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.RingProgress
import com.replica.cleaner.ui.components.TransitionAdOverlay
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.delay

private enum class GatePhase {
    Commercial,
    AlmostReady,
    Ad
}

/**
 * AVG Storage category gate:
 * Commercial break → "Almost ready…" (while scanning) → interstitial ads → content.
 *
 * [kind] is "apps", "photos", "audio", "video", or "others".
 */
@Composable
fun ContentGateScreen(
    vm: CleanerViewModel,
    kind: String,
    onDone: () -> Unit,
    onRemoveAds: () -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val appsLoading by vm.appsLoading.collectAsStateWithLifecycle()
    val media by vm.media.collectAsStateWithLifecycle()

    var phase by remember { mutableStateOf(GatePhase.Commercial) }
    var finished by remember { mutableStateOf(false) }

    fun finish() {
        if (!finished) {
            finished = true
            onDone()
        }
    }

    LaunchedEffect(kind) {
        when (kind) {
            "apps" -> vm.loadApps(force = true)
            else -> vm.loadMedia(force = true)
        }
    }

    LaunchedEffect(premium) {
        if (premium) finish()
    }

    val ready = when (kind) {
        "apps" -> apps != null && !appsLoading
        else -> media != null
    }

    when (phase) {
        GatePhase.Commercial -> CommercialBreakScreen(
            onFinished = { phase = GatePhase.AlmostReady },
            onRemoveAds = onRemoveAds
        )
        GatePhase.AlmostReady -> AlmostReadyScreen(
            ready = ready,
            onFinished = { phase = GatePhase.Ad }
        )
        GatePhase.Ad -> TransitionAdOverlay(onDismiss = { finish() })
    }
}

/** Storage → Apps (kept for existing call sites). */
@Composable
fun AppsGateScreen(
    vm: CleanerViewModel,
    onDone: () -> Unit,
    onRemoveAds: () -> Unit
) {
    ContentGateScreen(vm, kind = "apps", onDone = onDone, onRemoveAds = onRemoveAds)
}

@Composable
private fun CommercialBreakScreen(
    onFinished: () -> Unit,
    onRemoveAds: () -> Unit
) {
    val totalSeconds = 3
    var remaining by remember { mutableIntStateOf(totalSeconds) }
    val progress = 1f - (remaining - 1).coerceAtLeast(0).toFloat() / totalSeconds

    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        RingProgress(
            progress = progress.coerceIn(0.08f, 1f),
            modifier = Modifier.size(168.dp),
            strokeWidth = 4.dp
        ) {
            Text(
                text = "${remaining.coerceAtLeast(1)}",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(36.dp))
        Text(
            text = tr("Commercial break"),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = tr("This app is supported by advertisement. You can remove it by becoming a subscriber."),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalCleanerColors.current.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onRemoveAds)
                .padding(horizontal = 28.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tr("REMOVE ADS"),
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF07281A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AlmostReadyScreen(
    ready: Boolean,
    onFinished: () -> Unit
) {
    LaunchedEffect(ready) {
        // Keep the spinner visible briefly even if the scan finished early.
        delay(900)
        if (ready) {
            onFinished()
        } else {
            // Wait a bit longer for the scan, then proceed to ads anyway.
            delay(2500)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = tr("Almost ready..."),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
