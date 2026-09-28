package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatDaysAgo
import com.replica.cleaner.core.formatDuration
import com.replica.cleaner.core.formatPercent
import com.replica.cleaner.data.model.AppInfo
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCheckbox
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.LockBadge
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.components.StatTile
import com.replica.cleaner.ui.components.WeekBarChart
import com.replica.cleaner.ui.theme.Amber
import com.replica.cleaner.ui.theme.LocalCleanerColors

@Composable
fun AppsOverviewScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onOpenList: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val usage by vm.usageWeek.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) {
        vm.refreshPermissions()
        vm.loadApps()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Apps Overview"), onBack = onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val a = apps

            Row(Modifier.fillMaxWidth().height(300.dp)) {
                Column(Modifier.weight(1f)) {
                    StatTile(
                        value = "${a?.installedCount ?: 0}",
                        caption = tr("Installed"),
                        background = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onOpenList("installed") }
                    )
                    Spacer(Modifier.height(4.dp))
                    StatTile(
                        value = "${a?.systemCount ?: 0}",
                        caption = tr("System"),
                        background = colors.cardHigh,
                        valueColor = MaterialTheme.colorScheme.onBackground,
                        captionColor = colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onOpenList("system") }
                    )
                }
                Spacer(Modifier.width(4.dp))
                StatTile(
                    value = "${formatPercent(a?.appsFraction ?: 0f)} %",
                    caption = tr("used by apps"),
                    secondary = formatBytes(a?.appsBytes ?: 0L),
                    background = Amber,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onOpenList("all") }
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                tr("Drainers"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DrainerChip(a?.dataDrainer, formatBytes(a?.dataDrainer?.dataBytes ?: 0L), tr("Data"))
                DrainerChip(a?.storageDrainer, formatBytes(a?.storageDrainer?.totalBytes ?: 0L), "Storage")
                DrainerChip(
                    a?.batteryDrainer,
                    formatDuration(a?.batteryDrainer?.screenTimeMillis ?: 0L),
                    tr("Battery")
                )
            }

            Spacer(Modifier.height(32.dp))
            Text(
                tr("Usage"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                tr("Last 7 days"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            if (!permissions.usageAccess) {
                Text(
                    text = tr("Usage access is needed to show screen time, launch counts and unused apps."),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = tr("GRANT ACCESS"),
                    onClick = { Permissions.safeStart(context, Permissions.usageAccessIntent()) }
                )
            } else {
                WeekBarChart(values = usage.map { it.label to it.millis })
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val top = a?.installed?.maxByOrNull { it.launchCount }
                    DrainerChip(top, "${top?.launchCount ?: 0}", tr("Times opened"))
                    val screen = a?.installed?.maxByOrNull { it.screenTimeMillis }
                    DrainerChip(screen, formatDuration(screen?.screenTimeMillis ?: 0L), tr("Screen time"))
                    DrainerChip(
                        a?.unused?.firstOrNull(),
                        "${a?.unused?.size ?: 0}",
                        tr("Unused")
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Growing"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(8.dp))
                if (!premium) LockBadge()
            }
            Text(
                text = tr("This feature becomes available once enough time has passed to provide results."),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            if (!premium) {
                SecondaryButton(tr("UNLOCK WITH PREMIUM"), onUpgrade)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                tr("Notifying"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = tr("Notification access is needed to display apps sending you most notifications"),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = tr("GRANT ACCESS"),
                onClick = {
                    Permissions.safeStart(
                        context,
                        android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                        Permissions.notificationSettingsIntent(context)
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DrainerChip(app: AppInfo?, value: String, label: String) {
    val colors = LocalCleanerColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.magenta)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardHigh),
            contentAlignment = Alignment.Center
        ) {
            if (app?.icon != null) {
                AsyncImage(
                    model = app.icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
    }
}

private enum class AppSort { Size, Name, LastUsed }

@Composable
fun AppListScreen(
    vm: CleanerViewModel,
    kind: String,
    onBack: () -> Unit
) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val appsLoading by vm.appsLoading.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    var sort by remember { mutableStateOf(AppSort.Size) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) { vm.loadApps(force = apps == null) }

    val source = when (kind) {
        "system" -> apps?.system.orEmpty()
        "all" -> apps?.let { it.installed + it.system }.orEmpty()
        "unused" -> apps?.unused.orEmpty()
        else -> apps?.installed.orEmpty()
    }
    val list = when (sort) {
        AppSort.Size -> source.sortedByDescending { it.totalBytes }
        AppSort.Name -> source.sortedBy { it.label.lowercase() }
        AppSort.LastUsed -> source.sortedByDescending { it.lastUsed }
    }

    val title = when (kind) {
        "system" -> tr("System apps")
        "all" -> tr("All apps")
        "unused" -> tr("Unused apps")
        else -> tr("Installed apps")
    }

    val sortLabel = when (sort) {
        AppSort.Size -> tr("Size / Total size")
        AppSort.Name -> tr("Name")
        AppSort.LastUsed -> tr("Last used")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(title, onBack = onBack, centered = true) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        sort = when (sort) {
                            AppSort.Size -> AppSort.Name
                            AppSort.Name -> AppSort.LastUsed
                            AppSort.LastUsed -> AppSort.Size
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SwapVert, tr("Change sorting"), tint = colors.textSecondary)
            }
        }

        // AVG filter chips: green tune + active sort pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        sort = when (sort) {
                            AppSort.Size -> AppSort.Name
                            AppSort.Name -> AppSort.LastUsed
                            AppSort.LastUsed -> AppSort.Size
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = tr("Filter"),
                    tint = Color(0xFF07281A),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = sortLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.cardHigh)
                    .clickable {
                        sort = when (sort) {
                            AppSort.Size -> AppSort.Name
                            AppSort.Name -> AppSort.LastUsed
                            AppSort.LastUsed -> AppSort.Size
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${list.size} TOTAL (${formatBytes(list.sumOf { it.totalBytes })})",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (selected.size == list.size && list.isNotEmpty()) tr("DESELECT ALL") else tr("SELECT ALL"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable {
                    selected = if (selected.size == list.size) emptySet()
                    else list.map { it.packageName }.toSet()
                }
            )
        }

        if (appsLoading && list.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        tr("Scanning apps…"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(list, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (app.packageName in selected) selected - app.packageName
                                else selected + app.packageName
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.cardHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = app.icon,
                                contentDescription = app.label,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (sort) {
                                    AppSort.LastUsed -> formatDaysAgo(app.lastUsed)
                                    AppSort.Name -> formatBytes(app.totalBytes)
                                    AppSort.Size -> formatBytes(app.totalBytes)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        CleanerCheckbox(
                            checked = app.packageName in selected,
                            onCheckedChange = null
                        )
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        if (selected.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                val selectedApps = list.filter { it.packageName in selected }
                Text(
                    text = "${selectedApps.size} SELECTED (${formatBytes(selectedApps.sumOf { it.totalBytes })})",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    SecondaryButton(
                        text = tr("APP INFO"),
                        onClick = {
                            selectedApps.firstOrNull()?.let { vm.openAppSettings(it.packageName) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    PrimaryButton(
                        text = tr("UNINSTALL"),
                        onClick = {
                            selectedApps.filter { !it.isSystem }.forEach { vm.uninstall(it.packageName) }
                            selected = emptySet()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
