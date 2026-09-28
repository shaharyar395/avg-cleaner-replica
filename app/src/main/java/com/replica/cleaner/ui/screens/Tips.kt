package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatBytesParts
import com.replica.cleaner.core.formatDuration
import com.replica.cleaner.data.model.Tip
import com.replica.cleaner.data.model.TipKind
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.DonutChart
import com.replica.cleaner.ui.components.LegendRow
import com.replica.cleaner.ui.components.LockBadge
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.components.Segment
import com.replica.cleaner.ui.theme.ChartHiddenCache
import com.replica.cleaner.ui.theme.ChartOther
import com.replica.cleaner.ui.theme.ChartReview
import com.replica.cleaner.ui.theme.ChartUnneeded
import com.replica.cleaner.ui.theme.LocalCleanerColors

@Composable
fun TipsScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onQuickClean: () -> Unit,
    onOpenMedia: (String) -> Unit,
    onOpenApps: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val tips by vm.tips.collectAsStateWithLifecycle()
    val scanState by vm.scan.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) { vm.loadEverythingForTips() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(
            title = tr("%d space saving tips").replace("%d", tips.size.toString()),
            onBack = onBack,
            centered = false
        )

        if (tips.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = tr("Gathering tips…"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            items(tips, key = { it.id }) { tip ->
                TipCard(
                    tip = tip,
                    premium = premium,
                    scanResult = scanState.result,
                    diaryApps = apps?.installed?.sortedByDescending { it.screenTimeMillis }?.take(4),
                    onQuickClean = onQuickClean,
                    onOpenMedia = onOpenMedia,
                    onOpenApps = onOpenApps,
                    onUpgrade = onUpgrade
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TipCard(
    tip: Tip,
    premium: Boolean,
    scanResult: com.replica.cleaner.data.model.ScanResult?,
    diaryApps: List<com.replica.cleaner.data.model.AppInfo>?,
    onQuickClean: () -> Unit,
    onOpenMedia: (String) -> Unit,
    onOpenApps: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val locked = tip.premium && !premium

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = localizeTipText(tip.title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (tip.subtitle != null) {
                    Text(
                        text = localizeTipText(tip.subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            if (locked) {
                LockBadge()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = tr("Tip %d").replace("%d", tip.index.toString()),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.cardHigh)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        when (tip.kind) {
            TipKind.UnnecessaryData -> {
                val unneeded = scanResult?.unneededBytes ?: 0L
                val hidden = scanResult?.hiddenCacheBytes ?: 0L
                val review = scanResult?.reviewBytes ?: 0L
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        segments = listOf(
                            Segment(hidden.toFloat(), ChartHiddenCache),
                            Segment(unneeded.toFloat(), ChartUnneeded),
                            Segment(review.toFloat(), ChartReview)
                        ),
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 10.dp
                    ) {
                        val (value, unit) = formatBytesParts(tip.payloadBytes)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        LegendRow(ChartHiddenCache, tr("Hidden caches"), formatBytes(hidden))
                        Spacer(Modifier.height(8.dp))
                        LegendRow(ChartUnneeded, tr("Unneeded files"), formatBytes(unneeded))
                        Spacer(Modifier.height(8.dp))
                        LegendRow(ChartReview, tr("Files to review"), formatBytes(review))
                        Spacer(Modifier.height(8.dp))
                        LegendRow(ChartOther, tr("Other"), formatBytes(0L))
                    }
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(tr("QUICK CLEAN"), onQuickClean)
            }

            TipKind.AppDiary -> {
                diaryApps.orEmpty().forEach { app ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = app.icon,
                                contentDescription = app.label,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatDuration(app.screenTimeMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    SecondaryButton(
                        text = tr("UNINSTALL"),
                        onClick = { onOpenApps("installed") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    PrimaryButton(
                        text = tr("SHOW ALL"),
                        onClick = { onOpenApps("all") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            TipKind.RarelyUsed, TipKind.NotUsed ->
                PrimaryButton(tr("SEE ALL DETAILS"), onClick = { onOpenApps("unused") })

            TipKind.BadPhotos -> PrimaryButton(tr("REVIEW AND CLEAN"), onClick = { onOpenMedia("bad") })
            TipKind.Screenshots -> PrimaryButton(tr("REVIEW AND CLEAN"), onClick = { onOpenMedia("screenshots") })
            TipKind.SimilarPhotos -> PrimaryButton(tr("REVIEW AND CLEAN"), onClick = { onOpenMedia("similar") })
            TipKind.OldPhotos -> PrimaryButton(tr("REVIEW AND CLEAN"), onClick = { onOpenMedia("old") })
            TipKind.LargeVideos -> PrimaryButton(tr("CHECK NOW"), onClick = { onOpenMedia("video") })
            TipKind.BigFiles, TipKind.Downloads ->
                PrimaryButton(
                    "${tr("CHECK NOW")} (${formatBytes(tip.payloadBytes)})",
                    onClick = { onOpenMedia("others") }
                )

            TipKind.EmptyFolders -> PrimaryButton(tr("QUICK CLEAN"), onQuickClean)

            TipKind.OptimizableImages ->
                if (locked) {
                    PrimaryButton(
                        text = tr("UPGRADE"),
                        onClick = onUpgrade,
                        color = colors.amber
                    )
                } else {
                    PrimaryButton(tr("OPTIMIZE"), onClick = { onOpenMedia("optimizable") })
                }
        }
    }
}

/**
 * Localizes tip titles/subtitles that embed numbers or sizes.
 * Tries exact [tr] first, then known TipsEngine patterns.
 */
@Composable
private fun localizeTipText(raw: String): String {
    val exact = tr(raw)
    if (exact != raw) return exact

    val numPatterns = listOf(
        "empty folders" to "%d empty folders",
        "files in Downloads" to "%d files in Downloads",
        "bad photos found" to "%d bad photos found",
        "screenshots found" to "%d screenshots found",
        "similar photos" to "%d similar photos",
        "old photos" to "%d old photos",
        "optimizable images" to "%d optimizable images",
    )
    for ((pattern, template) in numPatterns) {
        val m = Regex("^(\\d+) $pattern$").matchEntire(raw)
        if (m != null) return tr(template).replace("%d", m.groupValues[1])
    }

    Regex("^(.+) can be reviewed$").matchEntire(raw)?.let {
        return tr("%s can be reviewed").replace("%s", it.groupValues[1])
    }
    Regex("^(.+) can be cleaned$").matchEntire(raw)?.let {
        return tr("%s can be cleaned").replace("%s", it.groupValues[1])
    }
    Regex("^Free up to (.+)$").matchEntire(raw)?.let {
        return tr("Free up to %s").replace("%s", it.groupValues[1])
    }
    Regex("^You have spent least time using (.+)$").matchEntire(raw)?.let {
        return tr("You have spent least time using %s").replace("%s", it.groupValues[1])
    }
    Regex("^Get (.+) more space$").matchEntire(raw)?.let {
        return tr("Get %s more space").replace("%s", it.groupValues[1])
    }
    Regex("^(\\d+) videos using (.+)$").matchEntire(raw)?.let {
        return tr("%d videos using %s")
            .replace("%d", it.groupValues[1])
            .replace("%s", it.groupValues[2])
    }

    return raw
}
