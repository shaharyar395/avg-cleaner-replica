package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.R
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatBytesParts
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCard
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.components.Segment
import com.replica.cleaner.ui.components.SegmentedBar
import com.replica.cleaner.ui.components.UpgradePill
import com.replica.cleaner.ui.theme.ChartHiddenCache
import com.replica.cleaner.ui.theme.ChartReview
import com.replica.cleaner.ui.theme.ChartUnneeded
import com.replica.cleaner.ui.theme.DangerRed
import com.replica.cleaner.ui.theme.LocalCleanerColors

@Composable
fun HomeScreen(
    vm: CleanerViewModel,
    onQuickClean: () -> Unit,
    onTips: () -> Unit,
    onMedia: () -> Unit,
    onApps: () -> Unit,
    onUpgrade: () -> Unit,
    onAutoCleaning: () -> Unit,
    onCustomize: () -> Unit,
    onSleepMode: () -> Unit,
    onInstallAntivirus: () -> Unit = {},
    onInstallVpn: () -> Unit = {}
) {
    val storage by vm.storage.collectAsStateWithLifecycle()
    val scanState by vm.scan.collectAsStateWithLifecycle()
    val media by vm.media.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val t = LocalL10n.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        vm.refreshPermissions()
        vm.refreshStorage()
        vm.startScan()
        vm.loadMedia()
        vm.loadApps()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshStorage()
                vm.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val result = scanState.result
    val scanning = scanState.running || result == null
    val unneededBytes = result?.unneededBytes ?: 0L
    val hiddenBytes = result?.hiddenCacheBytes ?: 0L
    val reviewBytes = result?.reviewBytes ?: 0L
    val showHiddenWarning = !permissions.usageAccess
    val cleanable = if (showHiddenWarning) {
        unneededBytes + reviewBytes
    } else {
        unneededBytes + hiddenBytes + reviewBytes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_app_brand),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "AVG",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            if (!premium) UpgradePill(onUpgrade)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Free space card — matches AVG screenshot
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
                    .background(colors.card)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = t.freeSpace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(4.dp))
                val (value, unit) = formatBytesParts(storage.freeBytes)
                Text(
                    text = "$value $unit",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        scanning -> t.scanning
                        else -> t.freeUpTo(formatBytes(cleanable))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))
                SegmentedBar(
                    segments = listOf(
                        Segment(unneededBytes.toFloat(), ChartUnneeded),
                        Segment(
                            if (showHiddenWarning) 0f else hiddenBytes.toFloat(),
                            ChartHiddenCache
                        ),
                        Segment(reviewBytes.toFloat(), ChartReview)
                    ),
                    height = 10.dp
                )
                Spacer(Modifier.height(16.dp))
                HomeLegendRow(
                    color = ChartUnneeded,
                    label = t.unneededFiles,
                    value = if (scanning) null else formatBytes(unneededBytes)
                )
                Spacer(Modifier.height(12.dp))
                HomeLegendRow(
                    color = ChartHiddenCache,
                    label = t.hiddenCaches,
                    value = when {
                        scanning -> null
                        showHiddenWarning -> null
                        else -> formatBytes(hiddenBytes)
                    },
                    showWarning = !scanning && showHiddenWarning
                )
                Spacer(Modifier.height(12.dp))
                HomeLegendRow(
                    color = ChartReview,
                    label = t.filesToReview,
                    value = if (scanning) null else formatBytes(reviewBytes)
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = if (scanState.running) t.scanning.uppercase() else t.quickClean.uppercase(),
                    onClick = onQuickClean,
                    enabled = !scanState.running
                )
            }

            Spacer(Modifier.height(14.dp))

            // 2x2 Sleep Mode / Tips / Media / Apps — separate cards with a small gap
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureTile(
                        icon = Icons.Default.Speed,
                        label = t.sleepMode,
                        modifier = Modifier.weight(1f),
                        onClick = { onSleepMode() }
                    )
                    FeatureTile(
                        icon = Icons.Default.Lightbulb,
                        label = t.tips,
                        modifier = Modifier.weight(1f),
                        onClick = onTips
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureTile(
                        icon = Icons.Default.PermMedia,
                        label = t.media,
                        subtitle = media?.let { "+ ${formatBytes(it.mediaBytes)}" },
                        modifier = Modifier.weight(1f),
                        onClick = onMedia
                    )
                    FeatureTile(
                        icon = Icons.Outlined.Apps,
                        label = t.apps,
                        subtitle = apps?.let { "+ ${formatBytes(it.appsBytes)}" },
                        modifier = Modifier.weight(1f),
                        onClick = onApps
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = { if (premium) onCustomize() else onUpgrade() })
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (!premium) Icons.Default.Lock else Icons.Default.Tune,
                    contentDescription = null,
                    tint = colors.amber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = t.customize,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.amber
                )
            }

            Spacer(Modifier.height(8.dp))

            PromoCard(
                title = tr("AVG recommends"),
                body = tr("Get AVG Antivirus to help protect you from harmful viruses and malware."),
                action = tr("INSTALL FOR FREE"),
                onAction = onInstallAntivirus,
                icon = Icons.Default.Security
            )
            Spacer(Modifier.height(12.dp))
            PromoCard(
                title = tr("Your IP address is visible"),
                body = tr("Use a VPN to hide it from strangers and hackers."),
                action = tr("INSTALL VPN"),
                onAction = onInstallVpn,
                icon = Icons.Default.Public
            )
            Spacer(Modifier.height(12.dp))
            PromoCard(
                title = tr("Automatic Cleaning"),
                body = tr("Set up a schedule so AVG Cleaner can do its magic automatically."),
                action = tr("LEARN MORE"),
                onAction = { if (premium) onAutoCleaning() else onUpgrade() },
                icon = Icons.Default.Schedule
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HomeLegendRow(
    color: Color,
    label: String,
    value: String?,
    showWarning: Boolean = false
) {
    val colors = LocalCleanerColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        when {
            showWarning -> {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DangerRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            value == null -> {
                Box(
                    Modifier
                        .width(56.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.cardHigh)
                )
            }
            else -> {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun FeatureTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.divider, RoundedCornerShape(14.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.cardHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun PromoCard(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    icon: ImageVector
) {
    val colors = LocalCleanerColors.current
    CleanerCard(padding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Box(
            Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        SecondaryButton(action, onAction)
    }
}

@Composable
fun StatPair(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = LocalCleanerColors.current.textSecondary
        )
    }
}
