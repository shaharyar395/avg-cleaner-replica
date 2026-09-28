package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatUptime
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.Divider
import com.replica.cleaner.ui.components.LockBadge
import com.replica.cleaner.ui.components.MeterBar
import com.replica.cleaner.ui.components.SectionLabel
import com.replica.cleaner.ui.theme.LocalCleanerColors

private val PromoIconBg = Color(0xFF5B9FD4)

@Composable
fun ToolsScreen(
    vm: CleanerViewModel,
    onQuickClean: () -> Unit,
    onAutoCleaning: () -> Unit,
    onCloudTransfers: () -> Unit,
    onSystemInfo: () -> Unit,
    onUpgrade: () -> Unit,
    onFeature: (String) -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = LocalCleanerColors.current
    val t = LocalL10n.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = t.tools,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // ---- Remove Junk ----
            ToolsSectionHeader(
                title = t.removeJunk,
                subtitle = t.removeJunkBody
            )
            ToolCard {
                ToolRow(
                    title = t.quickClean,
                    icon = Icons.Default.CleaningServices,
                    onClick = onQuickClean
                )
            }
            PremiumLabel(show = !premium)
            ToolCard {
                ToolRow(
                    title = t.deepClean,
                    subtitle = tr("Cleans browser data and hidden memory"),
                    icon = Icons.Default.CleaningServices,
                    locked = !premium,
                    onClick = { onFeature("deep_clean") }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
                ToolRow(
                    title = t.browserCleaner,
                    subtitle = tr("Clears browsing data and hidden web clutter"),
                    icon = Icons.Default.Language,
                    locked = !premium,
                    onClick = { onFeature("browser_cleaner") }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
                ToolRow(
                    title = t.autoCleaning,
                    subtitle = tr("Makes cleaning automatic, not manual"),
                    icon = Icons.Default.Schedule,
                    locked = !premium,
                    onClick = onAutoCleaning
                )
            }

            // ---- Make More Room ----
            ToolsSectionHeader(
                title = t.makeMoreRoom,
                subtitle = t.makeMoreRoomBody
            )
            ToolCard {
                ToolRow(
                    title = t.cloudTransfers,
                    icon = Icons.Default.CloudUpload,
                    onClick = onCloudTransfers
                )
            }
            PremiumLabel(show = !premium)
            ToolCard {
                ToolRow(
                    title = t.photoOptimizer,
                    subtitle = tr("Compresses images to reclaim space, keeps the memories"),
                    icon = Icons.Default.Compress,
                    locked = !premium,
                    onClick = { onFeature("photo_optimizer") }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
                ToolRow(
                    title = t.videoOptimizer,
                    subtitle = tr("Lighter videos, same moments"),
                    icon = Icons.Default.Movie,
                    locked = !premium,
                    onClick = { onFeature("video_optimizer") }
                )
            }

            // ---- Reduce Load ----
            ToolsSectionHeader(
                title = tr("Reduce Load"),
                subtitle = tr("Check device stats to keep your phone running smoothly.")
            )
            ToolCard {
                ToolRow(
                    title = t.systemInfo,
                    icon = Icons.Default.PhoneAndroid,
                    onClick = onSystemInfo
                )
            }
            PremiumLabel(show = !premium)
            ToolCard {
                ToolRow(
                    title = t.sleepMode,
                    subtitle = tr("Reduces background load by putting apps to sleep"),
                    icon = Icons.Default.Bedtime,
                    locked = !premium,
                    onClick = { onFeature("sleep_mode") }
                )
            }

            // ---- More by AVG ----
            ToolsSectionHeader(
                title = tr("More by AVG"),
                subtitle = tr("Discover other tools to protect your device and boost your privacy.")
            )
            listOf(
                Triple("AVG Antivirus", Icons.Default.Security, "com.antivirus"),
                Triple("AVG Secure VPN", Icons.Default.VpnKey, "com.avg.android.vpn"),
                Triple("Alarm Clock Xtreme", Icons.Default.Alarm, "com.apalon.alarmclock.weather")
            ).forEach { (label, icon, pkg) ->
                val installed = vm.isInstalled(pkg)
                ToolCard {
                    ToolRow(
                        title = label,
                        subtitle = if (installed) tr("Installed") else tr("Not installed"),
                        leading = {
                            PromoCircleIcon(icon)
                        },
                        onClick = { vm.openPlayStore(context, pkg) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsSectionHeader(title: String, subtitle: String) {
    val colors = LocalCleanerColors.current
    Spacer(Modifier.height(20.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun PremiumLabel(show: Boolean) {
    if (!show) {
        Spacer(Modifier.height(12.dp))
        return
    }
    val colors = LocalCleanerColors.current
    val t = LocalL10n.current
    Text(
        text = t.withPremium,
        style = MaterialTheme.typography.labelSmall,
        color = colors.textSecondary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun ToolCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card),
        content = content
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun ToolRow(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
    locked: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    val colors = LocalCleanerColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            leading != null -> leading()
            locked -> LockBadge()
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PromoCircleIcon(icon: ImageVector) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(PromoIconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SystemInfoScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val info by vm.systemInfo.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) { vm.loadSystemInfo() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("System Info"), onBack = onBack, centered = false)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel(tr("Device"))
            InfoRow(tr("Android"), "${info.androidVersion} (${info.androidCodename})")
            InfoRow(tr("Up-time"), formatUptime(info.uptimeMillis))
            InfoRow(tr("Model"), info.model)

            SectionLabel(tr("Network"))
            InfoRow(
                tr("Wi-fi"),
                if (info.wifiEnabled) tr("On") else tr("Off"),
                valueColor = if (info.wifiEnabled) MaterialTheme.colorScheme.primary else colors.danger
            )
            InfoRow(tr("Network SSID"), info.ssid ?: "—", muted = true)
            InfoRow(tr("IP address"), info.ipAddress ?: "—", muted = true)
            InfoRow(
                tr("Bluetooth"),
                if (info.bluetoothOn) tr("On") else tr("Off"),
                valueColor = if (info.bluetoothOn) MaterialTheme.colorScheme.primary else colors.danger
            )
            InfoRow(
                tr("Mobile data"),
                if (info.mobileDataOn) tr("On") else tr("Off"),
                valueColor = if (info.mobileDataOn) MaterialTheme.colorScheme.primary else colors.danger
            )

            SectionLabel(tr("Memory"))
            MeterSection(
                usedLabel = formatBytes(info.ramUsedBytes),
                availableLabel = formatBytes(info.ramAvailableBytes),
                fraction = if (info.ramTotal == 0L) 0f else info.ramUsedBytes.toFloat() / info.ramTotal
            )

            SectionLabel(tr("Internal storage"))
            MeterSection(
                usedLabel = formatBytes(info.storageUsedBytes),
                availableLabel = formatBytes(info.storageAvailableBytes),
                fraction = if (info.storageTotal == 0L) 0f
                else info.storageUsedBytes.toFloat() / info.storageTotal
            )

            SectionLabel(tr("SD card"))
            if (info.sdCardPresent) {
                MeterSection(
                    usedLabel = formatBytes(info.sdCardUsedBytes),
                    availableLabel = formatBytes(info.sdCardAvailableBytes),
                    fraction = if (info.sdCardTotal == 0L) 0f
                    else info.sdCardUsedBytes.toFloat() / info.sdCardTotal
                )
            } else {
                InfoRow(tr("Status"), tr("Not present"), muted = true)
            }

            SectionLabel(tr("Battery"))
            InfoRow(tr("Level"), "${info.batteryPercent} %")
            InfoRow(
                tr("Temperature"),
                String.format("%.1f °F", info.batteryFahrenheit)
            )

            SectionLabel(tr("CPU activity"))
            InfoRow(tr("Used"), "${info.cpuUsedPercent} %")
            InfoRow(tr("Idle"), "${info.cpuIdlePercent} %")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    muted: Boolean = false,
    valueColor: Color? = null
) {
    val colors = LocalCleanerColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (muted) MaterialTheme.typography.bodyMedium
            else MaterialTheme.typography.titleSmall,
            color = if (muted) colors.textSecondary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: colors.textSecondary
        )
    }
}

@Composable
private fun MeterSection(usedLabel: String, availableLabel: String, fraction: Float) {
    val colors = LocalCleanerColors.current
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        MeterBar(usedFraction = fraction)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(
                tr("Used"),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(usedLabel, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(
                tr("Available"),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(availableLabel, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}
