package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatPercent
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCard
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.Divider
import com.replica.cleaner.ui.components.DonutChart
import com.replica.cleaner.ui.components.Segment
import com.replica.cleaner.ui.theme.ChartAudio
import com.replica.cleaner.ui.theme.ChartPhotos
import com.replica.cleaner.ui.theme.ChartVideo
import com.replica.cleaner.ui.theme.LocalCleanerColors

@Composable
fun StorageScreen(
    vm: CleanerViewModel,
    onTips: () -> Unit,
    onApps: () -> Unit,
    onOpen: (String) -> Unit
) {
    val media by vm.media.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val t = LocalL10n.current

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        vm.refreshPermissions()
        vm.loadMedia(force = true)
    }

    fun requestMediaAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
        if (!Permissions.hasAllFilesAccess(context)) {
            Permissions.safeStart(
                context,
                Permissions.allFilesIntent(context),
                Permissions.allFilesFallbackIntent()
            )
        }
    }

    LaunchedEffect(Unit) {
        vm.refreshPermissions()
        vm.loadApps()
        // Always refresh so we don't keep a stale 0/0 B cache from before permission grant.
        vm.loadMedia(force = true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else if (!Permissions.hasAllFilesAccess(context)) {
            mediaPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshPermissions()
                vm.reloadMediaIfNeeded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // AVG Storage shows combined user+system app count / total size.
    val appCount = (apps?.installedCount ?: 0) + (apps?.systemCount ?: 0)
    val appBytes = apps?.appsBytes ?: 0L
    val mediaEmpty = media == null ||
        ((media?.photos?.count ?: 0) == 0 &&
            (media?.video?.count ?: 0) == 0 &&
            (media?.audio?.count ?: 0) == 0 &&
            (media?.others?.count ?: 0) == 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = t.storage,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            if (mediaEmpty && !permissions.allFiles) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.cardHigh)
                        .clickable { requestMediaAccess() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            tr("Allow file access"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            tr("Needed to show Photos, Audio, Video and Other files from this device."),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        tr("ALLOW"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Order matches AVG: Apps → Photos → Audio → Video → Others
            StorageCategoryCard(
                icon = Icons.Default.Apps,
                title = t.apps,
                value = "$appCount / ${formatBytes(appBytes)}",
                tip = tr("Discover unused apps, storage hogs, and performance drainers."),
                onClick = onApps,
                onTips = onTips
            )
            Spacer(Modifier.height(12.dp))

            StorageCategoryCard(
                icon = Icons.Default.Image,
                title = t.photos,
                value = "${media?.photos?.count ?: 0} / ${formatBytes(media?.photos?.bytes ?: 0)}",
                tip = tr("Shrink large photos, spot duplicates, and clear old or low-quality shots."),
                onClick = { onOpen("photos") },
                onTips = onTips
            )
            Spacer(Modifier.height(12.dp))

            StorageCategoryCard(
                icon = Icons.Default.Audiotrack,
                title = t.audio,
                value = "${media?.audio?.count ?: 0} / ${formatBytes(media?.audio?.bytes ?: 0)}",
                tip = null,
                onClick = { onOpen("audio") },
                onTips = onTips
            )
            Spacer(Modifier.height(12.dp))

            StorageCategoryCard(
                icon = Icons.Default.Movie,
                title = t.video,
                value = "${media?.video?.count ?: 0} / ${formatBytes(media?.video?.bytes ?: 0)}",
                tip = tr("Check what's taking up the most room and decide what to keep."),
                onClick = { onOpen("video") },
                onTips = onTips
            )
            Spacer(Modifier.height(12.dp))

            StorageCategoryCard(
                icon = Icons.Default.Description,
                title = t.others,
                value = "${media?.others?.count ?: 0} / ${formatBytes(media?.others?.bytes ?: 0)}",
                tip = null,
                onClick = { onOpen("others") },
                onTips = onTips
            )
        }
    }
}

/**
 * AVG Storage row: icon + title + "count / size" + chevron, optional tip strip
 * with SEE TIPS under a divider inside the same card.
 */
@Composable
private fun StorageCategoryCard(
    icon: ImageVector,
    title: String,
    value: String,
    tip: String?,
    onClick: () -> Unit,
    onTips: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
        if (tip != null) {
            Divider(Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTips)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = LocalL10n.current.seeTips,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** The donut + photo-analysis screen. */
@Composable
fun MediaOverviewScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val media by vm.media.collectAsStateWithLifecycle()
    val photos by vm.photos.collectAsStateWithLifecycle()
    val photoProgress by vm.photoProgress.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) {
        vm.loadMedia(force = true)
        vm.analyzePhotos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Photos"), onBack = onBack, centered = false)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val m = media
            val total = (m?.photos?.bytes ?: 0L) + (m?.audio?.bytes ?: 0L) + (m?.video?.bytes ?: 0L)
            Spacer(Modifier.height(12.dp))
            DonutChart(
                segments = listOf(
                    Segment((m?.audio?.bytes ?: 0L).toFloat(), ChartAudio),
                    Segment((m?.photos?.bytes ?: 0L).toFloat(), ChartPhotos),
                    Segment((m?.video?.bytes ?: 0L).toFloat(), ChartVideo)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                strokeWidth = 22.dp,
                center = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            formatBytes(total),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            tr("Media"),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MediaChip("${m?.audio?.count ?: 0}", tr("Audio"), ChartAudio) { onOpen("audio") }
                MediaChip("${m?.photos?.count ?: 0}", tr("Photos"), ChartPhotos) { onOpen("photos") }
                MediaChip("${m?.video?.count ?: 0}", tr("Video"), ChartVideo) { onOpen("video") }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                tr("Photo analysis"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (photoProgress in 0f..<1f && photos == null) {
                Text(
                    "${tr("Scanning photos…")} ${formatPercent(photoProgress)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))

            val similarCount = photos?.similar?.sumOf { it.size } ?: 0
            val similarBytes = photos?.similar?.sumOf { g -> g.sumOf { it.sizeBytes } } ?: 0L
            PhotoBucketRow(
                title = tr("Similar photos"),
                count = similarCount,
                bytes = similarBytes
            ) { onOpen("similar") }
            PhotoBucketRow(
                title = tr("Bad photos"),
                count = photos?.badQuality?.size ?: 0,
                bytes = photos?.badQuality?.sumOf { it.sizeBytes } ?: 0L
            ) { onOpen("bad") }
            PhotoBucketRow(
                title = tr("Screenshots"),
                count = photos?.screenshots?.size ?: 0,
                bytes = photos?.screenshots?.sumOf { it.sizeBytes } ?: 0L
            ) { onOpen("screenshots") }
            PhotoBucketRow(
                title = tr("Old photos"),
                count = photos?.old?.size ?: 0,
                bytes = photos?.old?.sumOf { it.sizeBytes } ?: 0L
            ) { onOpen("old") }

            Spacer(Modifier.height(12.dp))
            CleanerCard(onClick = { if (premium) onOpen("optimizable") else onUpgrade() }) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            tr("Optimizable photos"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${photos?.optimizable?.size ?: 0} photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    if (!premium) {
                        Text(
                            tr("PREMIUM"),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.amber
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MediaChip(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.bodySmall, color = LocalCleanerColors.current.textSecondary)
    }
}

@Composable
private fun PhotoBucketRow(
    title: String,
    count: Int,
    bytes: Long,
    onClick: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "$count · ${formatBytes(bytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary)
    }
}
