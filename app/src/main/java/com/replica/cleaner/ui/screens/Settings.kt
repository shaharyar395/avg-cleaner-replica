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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.core.CloudProviders
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.Prefs
import com.replica.cleaner.data.scan.JunkScanner
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCheckbox
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.Divider
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SectionLabel
import com.replica.cleaner.ui.components.SettingRow
import com.replica.cleaner.ui.components.ToggleRow
import com.replica.cleaner.ui.theme.AccentChoice
import com.replica.cleaner.ui.theme.Amber
import com.replica.cleaner.ui.theme.LocalCleanerColors
import com.replica.cleaner.ui.theme.ThemeMode
import com.replica.cleaner.ui.util.clickOpenPrivacyPolicy
import kotlinx.coroutines.launch
import android.content.Intent
import android.widget.Toast
import com.replica.cleaner.l10n.AppLanguage
import com.replica.cleaner.l10n.LocalL10n

@Composable
fun SettingsScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    routes: SettingsRoutes
) {
    val t = LocalL10n.current
    val language by vm.prefs.language.collectAsStateWithLifecycle(
        initialValue = AppLanguage.DEFAULT.displayName
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var languageMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(t.settings, onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingRow(t.quickClean, showChevron = true, onClick = { onOpen(routes.quickClean) })
            SettingRow(t.analysisPrefs, showChevron = true, onClick = { onOpen(routes.analysis) })
            SettingRow(t.notifications, showChevron = true, onClick = { onOpen(routes.notifications) })
            SettingRow(t.realtime, showChevron = true, onClick = { onOpen(routes.realtime) })
            SettingRow(t.cloudServices, showChevron = true, onClick = { onOpen(routes.cloud) })
            SettingRow(t.personalPrivacy, showChevron = true, onClick = { onOpen(routes.privacy) })
            Box {
                SettingRow(
                    t.languageLabel,
                    subtitle = t.languageSubtitle,
                    showChevron = true,
                    onClick = { languageMenu = true }
                )
                DropdownMenu(
                    expanded = languageMenu,
                    onDismissRequest = { languageMenu = false },
                    offset = DpOffset(x = 120.dp, y = 0.dp)
                ) {
                    AppLanguage.all.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang.displayName,
                                    color = if (lang.displayName == language) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                languageMenu = false
                                scope.launch { vm.prefs.setLanguage(lang.displayName) }
                            },
                            trailingIcon = {
                                if (lang.displayName == language) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = t.selected,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

data class SettingsRoutes(
    val quickClean: String,
    val analysis: String,
    val notifications: String,
    val realtime: String,
    val cloud: String,
    val privacy: String,
    val language: String
)

/** Which junk categories Quick Clean scans and pre-selects. */
@Composable
fun QuickCleanSettingsScreen(vm: CleanerViewModel, onBack: () -> Unit, onUpgrade: () -> Unit) {
    val enabled by vm.prefs.enabledCategories.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_QUICK_CLEAN_CATEGORIES
    )
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val unneeded = listOf(
        Triple(JunkScanner.HIDDEN_CACHES, "Hidden caches", "Temporary files that are deep in your app settings and more difficult to remove."),
        Triple(JunkScanner.VISIBLE_CACHES, tr("Visible caches"), "Temporary files that can be recreated."),
        Triple(JunkScanner.BROWSER_DATA, tr("Browser data"), "Saved data collected by your browsers when you browse or search online."),
        Triple(JunkScanner.RESIDUAL_FILES, tr("Residual files"), "Leftover files after you uninstall apps from your device."),
        Triple(JunkScanner.INSTALLED_APKS, tr("Installed APKs"), "Leftover installation files after new apps are installed."),
        Triple(JunkScanner.AD_CACHES, tr("Ad caches"), "Temporary files that make ads work."),
        Triple(JunkScanner.THUMBNAILS, tr("Thumbnails"), "Small preview versions of your photos."),
        Triple(JunkScanner.EMPTY_FOLDERS, tr("Empty folders"), "Folders with nothing inside.")
    )
    val review = listOf(
        Triple("trash", tr("Trash"), "Files that you already moved to the Trash folder."),
        Triple(JunkScanner.DOWNLOADS, tr("Downloads"), "Files that you downloaded from the internet."),
        Triple("screenshots", tr("Screenshots"), "Photos that show what's visible on your device display at the moment they're taken."),
        Triple("bad_photos", tr("Bad photos"), "Photos from your camera that we detected as blurry, dark, or low quality."),
        Triple("large_old_files", tr("Large old files"), "Files that are at least 100 MB and were created at least one month ago."),
        Triple(JunkScanner.TEMP_FILES, tr("Temporary files"), "Includes log files, junk files imported from other systems, and other temporary data.")
    )
    val lockedIds = setOf(JunkScanner.HIDDEN_CACHES, JunkScanner.BROWSER_DATA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Quick Clean settings"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = tr("Choose which categories to scan and review during every Quick Clean."),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            SectionLabel("Unneeded files", color = MaterialTheme.colorScheme.primary)
            Text(
                text = "These items don't impact how your device works. You can safely delete " +
                    "them and you won't notice they're gone. You can still review these files " +
                    "before deleting.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            unneeded.forEach { (id, title, description) ->
                val locked = id in lockedIds && !premium
                val alwaysOn = id in Prefs.ALWAYS_ENABLED_QC_CATEGORIES
                ToggleRow(
                    title = title,
                    subtitle = description,
                    // Free staples (Visible caches, Residual, …) stay on for Home + Tools.
                    checked = alwaysOn || id in enabled,
                    locked = locked,
                    onLockedClick = onUpgrade,
                    onCheckedChange = { checked ->
                        if (alwaysOn && !checked) return@ToggleRow
                        scope.launch { vm.prefs.setCategoryEnabled(id, checked) }
                    }
                )
                Divider(Modifier.padding(horizontal = 20.dp))
            }

            SectionLabel("Files to review")
            Text(
                text = "These items may be valuable to you, so you might notice if they're gone. " +
                    "We recommend reviewing these files before deleting.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            review.forEach { (id, title, description) ->
                ToggleRow(
                    title = title,
                    subtitle = description,
                    checked = id in enabled,
                    onCheckedChange = { checked ->
                        scope.launch { vm.prefs.setCategoryEnabled(id, checked) }
                    }
                )
                Divider(Modifier.padding(horizontal = 20.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Photo analysis switches plus the drag-to-reorder tip priority list. */
@Composable
fun AnalysisPreferencesScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val findPhotos by vm.prefs.findUnwantedPhotos.collectAsStateWithLifecycle(initialValue = true)
    val scanSd by vm.prefs.scanSdCard.collectAsStateWithLifecycle(initialValue = false)
    val priority by vm.prefs.tipPriority.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_TIP_PRIORITY
    )
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val badgeBlue = Color(0xFF4C9BE8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Analysis preferences"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ToggleRow(
                title = tr("Find unwanted photos"),
                subtitle = tr("Automatically scan for bad, similar or optimizable photos you might want to clean."),
                checked = findPhotos,
                onCheckedChange = { scope.launch { vm.prefs.setFindUnwantedPhotos(it) } }
            )
            ToggleRow(
                title = tr("Scan SD card"),
                subtitle = tr("Include SD card when scanning storage space. This may slow down the scanning speed."),
                checked = scanSd,
                onCheckedChange = { scope.launch { vm.prefs.setScanSdCard(it) } }
            )

            SectionLabel(tr("Priority of space saving tips"))
            priority.forEachIndexed { index, label ->
                var menuOpen by remember(label) { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(50))
                            .background(badgeBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = tr("Reorder"),
                            tint = colors.textSecondary,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { menuOpen = true }
                                .padding(2.dp)
                        )
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(tr("Move up")) },
                                enabled = index > 0,
                                onClick = {
                                    menuOpen = false
                                    scope.launch {
                                        vm.prefs.setTipPriority(priority.swapped(index, index - 1))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr("Move down")) },
                                enabled = index < priority.lastIndex,
                                onClick = {
                                    menuOpen = false
                                    scope.launch {
                                        vm.prefs.setTipPriority(priority.swapped(index, index + 1))
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun List<String>.swapped(a: Int, b: Int): List<String> =
    toMutableList().also { val t = it[a]; it[a] = it[b]; it[b] = t }

@Composable
fun NotificationsSettingsScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onCategory: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val master by vm.prefs.notificationsMaster.collectAsStateWithLifecycle(initialValue = true)
    val channels by vm.prefs.notifChannels.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_NOTIF_CHANNELS
    )
    val reportDay by vm.prefs.reportDay.collectAsStateWithLifecycle(initialValue = "Friday")
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val categories = listOf(
        "junk_cleaning" to tr("Junk Cleaning"),
        "applications" to tr("Applications"),
        "photos" to "Photos",
        "other_files" to tr("Other files")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Notifications"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            ) {
                ToggleRow(
                    title = tr("Notifications and Reports"),
                    checked = master,
                    onCheckedChange = { scope.launch { vm.prefs.setNotificationsMaster(it) } }
                )
            }

            SectionLabel(tr("Notifications"))
            Text(
                text = "Select the items for which you want to receive notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            categories.forEach { (id, label) ->
                SettingRow(
                    title = label,
                    subtitle = if (id in channels) tr("Enabled") else tr("Disabled"),
                    showChevron = true,
                    enabled = master,
                    onClick = { onCategory(id) }
                )
                Divider(Modifier.padding(horizontal = 20.dp))
            }

            SettingRow(
                title = tr("Frequency"),
                subtitle = "Choose how often you want to receive notifications.",
                locked = !premium,
                onClick = { if (!premium) onUpgrade() }
            )

            SectionLabel(tr("Reports"))
            Text(
                text = "Select the day of the week on which to receive your weekly reports.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            SettingRow(
                title = tr("New installs"),
                subtitle = "An up-to-date report of newly installed applications. Delivered once a week.",
                trailingText = tr(reportDay),
                onClick = {
                    val days = listOf(
                        "Monday", "Tuesday", "Wednesday", "Thursday",
                        "Friday", "Saturday", "Sunday"
                    )
                    val idx = days.indexOf(reportDay).let { if (it < 0) 0 else it }
                    val next = days[(idx + 1).mod(days.size)]
                    scope.launch { vm.prefs.setReportDay(next) }
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun NotificationCategoryScreen(
    vm: CleanerViewModel,
    categoryId: String,
    onBack: () -> Unit
) {
    val channels by vm.prefs.notifChannels.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_NOTIF_CHANNELS
    )
    val threshold by vm.prefs.notifThresholdMb.collectAsStateWithLifecycle(initialValue = 50)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val (title, items) = when (categoryId) {
        "junk_cleaning" -> tr("Junk Cleaning") to listOf(
            tr("Low storage") to "Sent when less than 5% of your storage space is left.",
            tr("Unnecessary data") to "Sent when there's 10 MB or more that you can safely clean."
        )
        "applications" -> tr("Applications") to listOf(
            tr("Unused apps") to "Sent when apps have not been opened in a month.",
            tr("Growing apps") to "Sent when an app's storage grows unusually fast.",
            tr("New installs") to "Sent when new applications are installed."
        )
        "photos" -> "Photos" to listOf(
            tr("Bad photos") to "Sent when blurry or dark photos are detected.",
            tr("Similar photos") to "Sent when near-duplicate shots pile up.",
            tr("Screenshots") to "Sent when screenshots take up noticeable space."
        )
        else -> tr("Other files") to listOf(
            tr("Downloads") to "Sent when there's at least 4 document files in your Download folder.",
            tr("Large files") to "Sent when at least 4 files have occupied 50 MB or more space in the past week.",
            tr("Large videos") to "Sent when at least 4 videos have occupied 50 MB or more in the past week.",
            tr("Cleaning tips") to "Best recommendations for you."
        )
    }

    val enabled = categoryId in channels

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(title, onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ToggleRow(
                title = "Enable $title notifications",
                checked = enabled,
                onCheckedChange = { scope.launch { vm.prefs.setNotifChannel(categoryId, it) } }
            )
            Divider(Modifier.padding(horizontal = 20.dp))

            items.forEach { (name, description) ->
                ToggleRow(
                    title = name,
                    subtitle = description,
                    checked = enabled,
                    onCheckedChange = { scope.launch { vm.prefs.setNotifChannel(categoryId, it) } }
                )
                Divider(Modifier.padding(horizontal = 20.dp))
            }

            if (categoryId == "junk_cleaning") {
                SettingRow(
                    title = tr("Notification threshold"),
                    subtitle = "Notify me when you clean this much or more.",
                    trailingText = "$threshold MB",
                    onClick = {
                        val steps = listOf(10, 50, 100, 250, 500)
                        val next = steps[(steps.indexOf(threshold) + 1).mod(steps.size)]
                        scope.launch { vm.prefs.setNotifThresholdMb(next) }
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun RealtimeDetectionScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val leftovers by vm.prefs.appLeftovers.collectAsStateWithLifecycle(initialValue = true)
    val battery by vm.prefs.batteryMonitoring.collectAsStateWithLifecycle(initialValue = false)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Real-time Detection"), onBack = onBack, centered = false)
        Column(Modifier.weight(1f)) {
            ToggleRow(
                title = tr("App leftovers"),
                subtitle = tr("Let me know if there's unimportant data left behind after uninstalling an app."),
                checked = leftovers,
                onCheckedChange = { scope.launch { vm.prefs.setAppLeftovers(it) } }
            )
            ToggleRow(
                title = tr("Battery monitoring"),
                subtitle = tr("Measure how I use my battery so I can see insights about how to save power."),
                checked = battery,
                onCheckedChange = { scope.launch { vm.prefs.setBatteryMonitoring(it) } }
            )
        }
    }
}

@Composable
fun CloudServicesScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val deleteAfter by vm.prefs.deleteAfterTransfer.collectAsStateWithLifecycle(initialValue = true)
    val wifiOnly by vm.prefs.wifiOnlyUpload.collectAsStateWithLifecycle(initialValue = true)
    val connected by vm.prefs.connectedCloudProviders.collectAsStateWithLifecycle(
        initialValue = emptySet()
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val l10n = LocalL10n.current
    var driveAccountPicker by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<String?>(null) }

    val googleAccounts = remember {
        runCatching {
            android.accounts.AccountManager.get(context)
                .getAccountsByType("com.google")
                .map { it.name }
        }.getOrDefault(emptyList())
    }

    fun connectDropbox() {
        if (CloudProviders.DROPBOX in connected) {
            scope.launch { vm.prefs.setCloudProviderConnected(CloudProviders.DROPBOX, false) }
            Toast.makeText(context, l10n.tr("Dropbox disconnected"), Toast.LENGTH_SHORT).show()
        } else {
            CloudProviders.openConnect(context, CloudProviders.Dropbox)
            scope.launch { vm.prefs.setCloudProviderConnected(CloudProviders.DROPBOX, true) }
            Toast.makeText(context, l10n.tr("Dropbox connected"), Toast.LENGTH_SHORT).show()
        }
    }

    fun connectDrive(account: String?) {
        CloudProviders.openConnect(context, CloudProviders.GoogleDrive)
        scope.launch { vm.prefs.setCloudProviderConnected(CloudProviders.GOOGLE_DRIVE, true) }
        Toast.makeText(
            context,
            if (account != null) "${l10n.tr("Google Drive connected")} ($account)"
            else l10n.tr("Google Drive connected"),
            Toast.LENGTH_SHORT
        ).show()
    }

    if (driveAccountPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { driveAccountPicker = false },
            title = {
                Text(tr("Choose account for AVG Cleaner"))
            },
            text = {
                Column {
                    val addAccountKey = "Add account"
                    (googleAccounts + addAccountKey).forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedAccount = option }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedAccount == option,
                                onClick = { selectedAccount = option }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (option == addAccountKey) tr(addAccountKey) else option,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "OK",
                    color = if (selectedAccount != null) MaterialTheme.colorScheme.primary
                    else LocalCleanerColors.current.textSecondary,
                    modifier = Modifier
                        .clickable(enabled = selectedAccount != null) {
                            val pick = selectedAccount
                            driveAccountPicker = false
                            if (pick == "Add account") {
                                Permissions.safeStart(
                                    context,
                                    Intent(android.provider.Settings.ACTION_ADD_ACCOUNT).apply {
                                        putExtra(
                                            android.provider.Settings.EXTRA_ACCOUNT_TYPES,
                                            arrayOf("com.google")
                                        )
                                    },
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://accounts.google.com")
                                    )
                                )
                            } else if (pick != null) {
                                connectDrive(pick)
                            }
                            selectedAccount = null
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = tr("CANCEL"),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            driveAccountPicker = false
                            selectedAccount = null
                        }
                        .padding(8.dp)
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Cloud services"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel(tr("Add a cloud storage"))
            SettingRow(
                title = "Dropbox",
                subtitle = if (CloudProviders.DROPBOX in connected) tr("Connected") else null,
                icon = Icons.Default.Cloud,
                iconTint = MaterialTheme.colorScheme.onBackground,
                trailingText = if (CloudProviders.DROPBOX in connected) tr("DISCONNECT") else tr("CONNECT"),
                onClick = { connectDropbox() }
            )
            SettingRow(
                title = "Google Drive",
                subtitle = if (CloudProviders.GOOGLE_DRIVE in connected) tr("Connected") else null,
                icon = Icons.Default.CloudQueue,
                iconTint = MaterialTheme.colorScheme.onBackground,
                trailingText = if (CloudProviders.GOOGLE_DRIVE in connected) tr("DISCONNECT") else tr("CONNECT"),
                onClick = {
                    if (CloudProviders.GOOGLE_DRIVE in connected) {
                        scope.launch {
                            vm.prefs.setCloudProviderConnected(CloudProviders.GOOGLE_DRIVE, false)
                        }
                        Toast.makeText(context, l10n.tr("Google Drive disconnected"), Toast.LENGTH_SHORT).show()
                    } else {
                        selectedAccount = null
                        driveAccountPicker = true
                    }
                }
            )

            SectionLabel("Settings")
            ToggleRow(
                title = tr("Delete files after transfer"),
                checked = deleteAfter,
                onCheckedChange = { scope.launch { vm.prefs.setDeleteAfterTransfer(it) } }
            )
            ToggleRow(
                title = tr("Upload files only on Wi-Fi"),
                checked = wifiOnly,
                onCheckedChange = { scope.launch { vm.prefs.setWifiOnlyUpload(it) } }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PersonalPrivacyScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val shareAvg by vm.prefs.shareUsageAvg.collectAsStateWithLifecycle(initialValue = true)
    val shareThird by vm.prefs.shareUsageThirdParty.collectAsStateWithLifecycle(initialValue = false)
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Personal privacy"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel(tr("Personal privacy"))
            Text(
                text = buildAnnotatedString {
                    append(tr("Learn how we process your data in our "))
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            textDecoration = TextDecoration.Underline
                        )
                    ) { append("Privacy Policy") }
                    append(". See the categories of data we process in our ")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            textDecoration = TextDecoration.Underline
                        )
                    ) { append(tr("Product Policy")) }
                    append(".")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickOpenPrivacyPolicy()
            )

            SectionLabel(tr("Improvements"))
            ToggleRow(
                title = "Share app-usage data with AVG to help us with new product development.",
                checked = shareAvg,
                onCheckedChange = { scope.launch { vm.prefs.setShareUsageAvg(it) } }
            )
            ToggleRow(
                title = "Share app-usage data with 3rd-party analytics tools to improve this app.",
                checked = shareThird,
                onCheckedChange = { scope.launch { vm.prefs.setShareUsageThirdParty(it) } }
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "These settings apply only to AVG Cleaner.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val t = LocalL10n.current
    val current by vm.prefs.language.collectAsStateWithLifecycle(
        initialValue = AppLanguage.DEFAULT.displayName
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(t.languageLabel, onBack = onBack, centered = false)
        LazyColumn(Modifier.weight(1f)) {
            items(AppLanguage.all, key = { it.tag }) { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { vm.prefs.setLanguage(language.displayName) }
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (language.displayName == current) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = t.selected,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AutoCleaningScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onCategory: (String) -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val enabled by vm.prefs.autoCleanEnabled.collectAsStateWithLifecycle(initialValue = false)
    val frequency by vm.prefs.autoCleanFrequency.collectAsStateWithLifecycle(initialValue = "Daily")
    val threshold by vm.prefs.notifThresholdMb.collectAsStateWithLifecycle(initialValue = 50)
    val categories by vm.prefs.autoCleanCategories.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_AUTO_CLEAN_CATEGORIES
    )
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val accent = MaterialTheme.colorScheme.primary
    val iconBlue = Color(0xFF4DB8FF)

    val junkIds = listOf(
        JunkScanner.RESIDUAL_FILES,
        JunkScanner.INSTALLED_APKS,
        JunkScanner.AD_CACHES,
        JunkScanner.THUMBNAILS,
        JunkScanner.EMPTY_FOLDERS
    )
    val photoIds = listOf("screenshots", "optimized_originals")
    val downloadIds = listOf("dl_images", "dl_audio", "dl_video", "dl_documents", "dl_archives")

    fun selectedCount(ids: List<String>) = ids.count { it in categories }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar("", onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            FeatureTourIllustration(
                kind = FeatureTourKind.AutoCleaning,
                accent = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = tr("Automatic Cleaning"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Save time and brain power by setting up a regular cleaning schedule. " +
                    "Choose what you want to clean, how often, and then we'll do all the work for you automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            if (!premium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Amber)
                        .clickable(onClick = onUpgrade)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tr("UPGRADE OPTIONS"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1208)
                    )
                }
                Spacer(Modifier.height(24.dp))
            } else {
                ToggleRow(
                    title = tr("Automatic Cleaning"),
                    subtitle = tr("Switch it on and never worry again."),
                    checked = enabled,
                    onCheckedChange = { vm.setAutoClean(it) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = tr("SETTINGS"),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Select the items from each category that we should clean automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(8.dp))

            AutoCleanCategoryRow(
                title = tr("Junk files"),
                selected = selectedCount(junkIds),
                total = junkIds.size,
                icon = Icons.Default.CleaningServices,
                iconBlue = iconBlue,
                onClick = { onCategory("junk") }
            )
            Divider()
            AutoCleanCategoryRow(
                title = "Photos",
                selected = selectedCount(photoIds),
                total = photoIds.size,
                icon = Icons.Default.Photo,
                iconBlue = iconBlue,
                onClick = { onCategory("photos") }
            )
            Divider()
            AutoCleanCategoryRow(
                title = tr("Downloads"),
                selected = selectedCount(downloadIds),
                total = downloadIds.size,
                icon = Icons.Default.Download,
                iconBlue = iconBlue,
                onClick = { onCategory("downloads") }
            )

            Spacer(Modifier.height(8.dp))
            AutoCleanDropdownSetting(
                title = tr("Frequency"),
                subtitle = "Select how often we should clean automatically.",
                valueLabel = tr(frequency),
                options = AutoCleanFrequencyOptions,
                onSelect = { vm.setAutoCleanFrequency(it) }
            )
            AutoCleanDropdownSetting(
                title = tr("Notification"),
                subtitle = "Notify me when you clean this much or more.",
                valueLabel = autoCleanNotifLabel(threshold),
                options = AutoCleanNotifOptions.map { it.second },
                onSelect = { label ->
                    val mb = AutoCleanNotifOptions.first { it.second == label }.first
                    scope.launch { vm.prefs.setNotifThresholdMb(mb) }
                }
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = tr("ABOUT AUTO CLEANING"),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            AutoCleanFaqItem(
                question = tr("Why can't I automatically clean some categories?"),
                answer = "Auto Cleaning only works for items that are safe to delete without review. " +
                    "Since Auto Cleaning runs in the background, you can't double-check before cleaning " +
                    "items in the categories you select.\n\n" +
                    "To make sure we don't remove something you need, we don't automatically clean " +
                    "similar photos, unused apps, or large videos.",
                iconBlue = iconBlue
            )
            AutoCleanFaqItem(
                question = tr("Why can't I clean my hidden cache automatically?"),
                answerAnnotated = buildAnnotatedString {
                    append("We need Cleaner to be open to clean the ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append("hidden cache")
                    }
                    append(". This means we can't clean it automatically in the background.\n\n")
                    append("You can remove the hidden cache with ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append("Deep Clean")
                    }
                    append(" — a Premium feature.")
                },
                iconBlue = iconBlue
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AutoCleanCategoryRow(
    title: String,
    selected: Int,
    total: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBlue: Color,
    onClick: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(iconBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF0A1A28), modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = if (selected > 0) primary else colors.textSecondary,
                            fontWeight = if (selected > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    ) { append("$selected") }
                    withStyle(SpanStyle(color = colors.textSecondary)) { append(" / $total items") }
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary)
    }
}

@Composable
private fun AutoCleanFaqItem(
    question: String,
    iconBlue: Color,
    answer: String? = null,
    answerAnnotated: AnnotatedString? = null
) {
    val colors = LocalCleanerColors.current
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(iconBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HelpOutline,
                    null,
                    tint = Color(0xFF0A1A28),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = colors.textSecondary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            if (answerAnnotated != null) {
                Text(answerAnnotated, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            } else if (answer != null) {
                Text(answer, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
    }
}

@Composable
fun AutoCleanCategoryScreen(
    vm: CleanerViewModel,
    categoryId: String,
    onBack: () -> Unit
) {
    val selected by vm.prefs.autoCleanCategories.collectAsStateWithLifecycle(
        initialValue = Prefs.DEFAULT_AUTO_CLEAN_CATEGORIES
    )
    val keepDays by vm.prefs.downloadsKeepDays.collectAsStateWithLifecycle(initialValue = 7)
    val screenshotsKeep by vm.prefs.screenshotsKeepDays.collectAsStateWithLifecycle(initialValue = 7)
    val optimizedKeep by vm.prefs.optimizedOriginalsKeepDays.collectAsStateWithLifecycle(initialValue = 7)
    val colors = LocalCleanerColors.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val accent = MaterialTheme.colorScheme.primary

    var tab by androidx.compose.runtime.remember(categoryId) {
        androidx.compose.runtime.mutableStateOf(
            when (categoryId) {
                "photos" -> 1
                "downloads" -> 2
                else -> 0
            }
        )
    }

    var localSelected by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(selected)
    }
    androidx.compose.runtime.LaunchedEffect(selected) {
        localSelected = selected
    }

    fun toggle(id: String) {
        val next = !localSelected.contains(id)
        localSelected = if (next) localSelected + id else localSelected - id
        scope.launch { vm.prefs.setAutoCleanCategory(id, next) }
    }

    val title = when (tab) {
        1 -> "Photos"
        2 -> tr("Downloads")
        else -> tr("Junk files")
    }

    val junkRows = listOf(
        Triple(
            JunkScanner.RESIDUAL_FILES,
            tr("Residual files"),
            "Leftover files after you uninstall apps."
        ),
        Triple(
            JunkScanner.INSTALLED_APKS,
            tr("Installed APKs"),
            "Leftover files after you install apps."
        ),
        Triple(
            JunkScanner.AD_CACHES,
            tr("Ad caches"),
            "Temporary files that make ads work."
        ),
        Triple(
            JunkScanner.THUMBNAILS,
            tr("Thumbnails"),
            "Small preview versions of your images."
        ),
        Triple(
            JunkScanner.EMPTY_FOLDERS,
            tr("Empty folders"),
            "Folders with nothing inside."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title, onBack = onBack, centered = false)

        Row(Modifier.fillMaxWidth()) {
            listOf(
                0 to Icons.Default.CleaningServices,
                1 to Icons.Default.Photo,
                2 to Icons.Default.Download
            ).forEach { (index, icon) ->
                val active = tab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (active) accent else colors.textSecondary,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(26.dp)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (active) accent else Color.Transparent)
                    )
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (tab) {
                1 -> {
                    Text(
                        text = "Select what you'd like to delete during Auto Cleaning.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                    AutoCleanPhotoItem(
                        title = tr("Screenshots"),
                        subtitle = "Photos that show what's visible on your device display at the moment they're taken.",
                        checked = "screenshots" in localSelected,
                        keepDays = screenshotsKeep,
                        onToggle = { toggle("screenshots") },
                        onKeepDays = { days ->
                            scope.launch { vm.prefs.setScreenshotsKeepDays(days) }
                        }
                    )
                    Divider(Modifier.padding(horizontal = 20.dp))
                    AutoCleanPhotoItem(
                        title = tr("Optimized originals"),
                        subtitle = "The original photos that were used to create optimized duplicates.",
                        checked = "optimized_originals" in localSelected,
                        keepDays = optimizedKeep,
                        onToggle = { toggle("optimized_originals") },
                        onKeepDays = { days ->
                            scope.launch { vm.prefs.setOptimizedOriginalsKeepDays(days) }
                        }
                    )
                }
                2 -> {
                    Text(
                        text = "Select which file types we can delete from your Downloads folder automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                    AutoCleanKeepDaysRow(
                        keepDays = keepDays,
                        onKeepDays = { days ->
                            scope.launch { vm.prefs.setDownloadsKeepDays(days) }
                        }
                    )
                    Divider(Modifier.padding(horizontal = 20.dp))
                    val downloadRows = listOf(
                        Triple("dl_images", tr("Images"), "JPG, JPEG, GIF...") to Icons.Default.Image,
                        Triple("dl_audio", "Audio", "MP3, OGG, FLAC...") to Icons.Default.Audiotrack,
                        Triple("dl_video", "Video", "MP4, MKV, AVI...") to Icons.Default.Movie,
                        Triple("dl_documents", tr("Documents"), "TXT, PDF, HTML...") to Icons.Default.Description,
                        Triple("dl_archives", tr("Archives"), "ZIP, RAR, 7Z...") to Icons.Default.FolderZip
                    )
                    downloadRows.forEach { (triple, icon) ->
                        val (id, label, subtitle) = triple
                        val checked = id in localSelected
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { toggle(id) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            CleanerCheckbox(
                                checked = checked,
                                onCheckedChange = { toggle(id) }
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Select what we should clean automatically. All these items are safe to clean from your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                    junkRows.forEach { (id, label, subtitle) ->
                        val checked = id in localSelected
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { toggle(id) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            CleanerCheckbox(
                                checked = checked,
                                onCheckedChange = { toggle(id) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private val AutoCleanFrequencyOptions = listOf(
    "Daily",
    "Every 3 days",
    "Weekly",
    "Every 2 weeks",
    "Monthly"
)

/** -1 Never, 0 Always, else size in MB. */
private val AutoCleanNotifOptions = listOf(
    -1 to "Never",
    0 to "Always",
    50 to "50 MB",
    100 to "100 MB",
    250 to "250 MB"
)

@Composable
private fun autoCleanNotifLabel(mb: Int): String {
    val raw = AutoCleanNotifOptions.firstOrNull { it.first == mb }?.second
        ?: if (mb > 0) "$mb MB" else "50 MB"
    return if (raw.endsWith(" MB")) raw else tr(raw)
}

@Composable
private fun AutoCleanDropdownSetting(
    title: String,
    subtitle: String,
    valueLabel: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = LocalCleanerColors.current
    var menuOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Box {
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { menuOpen = true }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                )
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    offset = DpOffset(0.dp, 4.dp)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (option.endsWith(" MB")) option else tr(option)
                                )
                            },
                            onClick = {
                                onSelect(option)
                                menuOpen = false
                            }
                        )
                    }
                }
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { menuOpen = true }
        )
    }
}

private val AutoCleanKeepOptions = listOf(
    7 to "1 week",
    14 to "2 weeks",
    30 to "1 month",
    90 to "3 months",
    180 to "6 months"
)

private fun autoCleanKeepLabel(days: Int): String =
    (AutoCleanKeepOptions.firstOrNull { it.first == days }?.second ?: "1 month").uppercase()

@Composable
private fun AutoCleanKeepDaysRow(
    keepDays: Int,
    onKeepDays: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tr("Keep files for this long before cleaning"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .clickable { menuOpen = true }
                .padding(end = 12.dp)
        )
        // Anchor menu to the green value so it opens under that control (right side).
        Box {
            Text(
                text = autoCleanKeepLabel(keepDays),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { menuOpen = true }
                    .padding(vertical = 4.dp)
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                offset = DpOffset(0.dp, 4.dp)
            ) {
                AutoCleanKeepOptions.forEach { (days, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onKeepDays(days)
                            menuOpen = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoCleanPhotoItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    keepDays: Int,
    onToggle: () -> Unit,
    onKeepDays: (Int) -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                )
            }
            CleanerCheckbox(
                checked = checked,
                onCheckedChange = { onToggle() }
            )
        }
        AutoCleanKeepDaysRow(keepDays = keepDays, onKeepDays = onKeepDays)
    }
}

@Composable
fun ThemesScreen(vm: CleanerViewModel, onBack: () -> Unit, onUpgrade: () -> Unit) {
    val mode by vm.themeMode.collectAsStateWithLifecycle()
    val accent by vm.accent.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    var pendingMode by androidx.compose.runtime.remember(mode) {
        androidx.compose.runtime.mutableStateOf(mode)
    }
    var pendingAccent by androidx.compose.runtime.remember(accent) {
        androidx.compose.runtime.mutableStateOf(accent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Themes"), onBack = onBack, centered = false)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phone preview
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (pendingMode == ThemeMode.Light) Color.White else colors.card)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(pendingAccent.dark)
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) {
                        Box(
                            Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (pendingMode == ThemeMode.Light) Color(0xFFE3E8EE)
                                    else colors.cardHigh
                                )
                        )
                    }
                }
            }
        }

        // Mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.entries.forEach { option ->
                val selected = pendingMode == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary else colors.card)
                        .clickable { pendingMode = option },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color(0xFF07281A) else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Accent swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccentChoice.entries.forEach { choice ->
                val locked = choice.premium && !premium
                val selected = pendingAccent == choice
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.card)
                        .clickable { if (locked) onUpgrade() else pendingAccent = choice },
                    contentAlignment = Alignment.Center
                ) {
                    if (locked) {
                        com.replica.cleaner.ui.components.LockBadge()
                    } else {
                        Box(
                            Modifier
                                .size(if (selected) 20.dp else 14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(choice.dark)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = tr("SET THEME"),
            onClick = {
                vm.setThemeMode(pendingMode)
                vm.setAccent(pendingAccent)
                onBack()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    val colors = LocalCleanerColors.current
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}
