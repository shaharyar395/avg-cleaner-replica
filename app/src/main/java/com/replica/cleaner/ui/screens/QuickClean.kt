package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.os.Build
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.data.model.JunkCategory
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCheckbox
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.CleanerTriStateCheckbox
import com.replica.cleaner.ui.components.LockBadge
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.RingProgress
import com.replica.cleaner.ui.components.SectionLabel
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.delay

@Composable
fun QuickCleanScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onUpgrade: () -> Unit,
    onCleaning: () -> Unit
) {
    val scanState by vm.scan.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val premium by vm.premium.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        vm.refreshPermissions()
        vm.startScan(force = true)
    }

    LaunchedEffect(Unit) {
        vm.refreshPermissions()
        vm.prefs.ensureAlwaysEnabledQcCategories()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Permissions.hasAllFilesAccess(context)
        ) {
            mediaPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
        vm.startScan(force = true)
    }

    val expanded = remember { mutableStateOf<Set<String>>(emptySet()) }
    val result = scanState.result

    val selectedItems = remember(selected, result, premium) { vm.selectedItems() }
    val selectedBytes = selectedItems.sumOf { it.sizeBytes }
    val selectedCount = selectedItems.size

    // Only re-scan when all-files access is newly granted (avoids stuck "Finding junk…").
    var hadAllFiles by remember { mutableStateOf(permissions.allFiles) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshPermissions()
                val nowAllFiles = Permissions.hasAllFilesAccess(context)
                if (nowAllFiles && !hadAllFiles) {
                    hadAllFiles = true
                    vm.startScan(force = true)
                } else {
                    hadAllFiles = nowAllFiles
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(
            title = tr("Quick Clean"),
            onBack = onBack,
            centered = false
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Settings, tr("Quick Clean settings"), tint = colors.textSecondary)
            }
        }

        if (!permissions.allFiles) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardHigh)
                    .clickable {
                        Permissions.safeStart(
                            context,
                            Permissions.allFilesIntent(context),
                            Permissions.allFilesFallbackIntent()
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("Allow all files access so Quick Clean can find and delete real junk."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = tr("ALLOW"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (scanState.running || result == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (scanState.error != null && !scanState.running) {
                    Text(
                        tr("Couldn't finish the scan"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        scanState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        tr("TRY AGAIN"),
                        onClick = { vm.startScan(force = true) },
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                } else {
                    RingProgress(scanState.progress, Modifier.size(140.dp), strokeWidth = 5.dp) {
                        Text(
                            "${(scanState.progress * 100).toInt()}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        tr("Finding junk…"),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        tr(scanState.label),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            item {
                SectionLabel(
                    tr("Unneeded files"),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(result.unneeded, key = { it.id }) { category ->
                CategoryBlock(
                    category = category,
                    selectedIds = selected,
                    expanded = category.id in expanded.value,
                    premium = premium,
                    onToggleExpand = {
                        expanded.value = if (category.id in expanded.value) {
                            expanded.value - category.id
                        } else {
                            expanded.value + category.id
                        }
                    },
                    onToggleCategory = { vm.toggleCategory(category.id) },
                    onToggleItem = { vm.toggleItem(it) },
                    onUpgrade = onUpgrade
                )
            }

            item { SectionLabel(tr("Files to review")) }

            items(result.review, key = { it.id }) { category ->
                CategoryBlock(
                    category = category,
                    selectedIds = selected,
                    expanded = category.id in expanded.value,
                    premium = premium,
                    onToggleExpand = {
                        expanded.value = if (category.id in expanded.value) {
                            expanded.value - category.id
                        } else {
                            expanded.value + category.id
                        }
                    },
                    onToggleCategory = { vm.toggleCategory(category.id) },
                    onToggleItem = { vm.toggleItem(it) },
                    onUpgrade = onUpgrade
                )
            }

            item { Spacer(Modifier.height(96.dp)) }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "$selectedCount SELECTED ( > ${formatBytes(selectedBytes)})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = tr("FINISH CLEANING"),
                onClick = {
                    vm.finishCleaning()
                    onCleaning()
                },
                enabled = selectedItems.isNotEmpty()
            )
        }
    }
}

@Composable
private fun CategoryBlock(
    category: JunkCategory,
    selectedIds: Set<String>,
    expanded: Boolean,
    premium: Boolean,
    onToggleExpand: () -> Unit,
    onToggleCategory: () -> Unit,
    onToggleItem: (String) -> Unit,
    onUpgrade: () -> Unit
) {
    val colors = LocalCleanerColors.current
    // Only Hidden caches / Browser data are premium-locked.
    // Visible caches + Residual stay unlocked and always toggleable (Home + Tools).
    val needsPremium = category.locked && !premium
    val selectableItems = category.items.filter {
        it.sizeBytes > 0L || it.categoryId == "empty_folders"
    }
    val ids = selectableItems.map { it.id }
    val selectedInCategory = ids.count { it in selectedIds }
    val selectedBytes = selectableItems.filter { it.id in selectedIds }.sumOf { it.sizeBytes }
    val totalForUi = ids.size
    // Free categories always accept taps — even at 0/0 — so Tools Quick Clean
    // matches Home (empty rows use a category-id selection marker).
    val canSelect = !needsPremium
    val checkboxEnabled = !needsPremium
    val emptySelected = ids.isEmpty() && category.id in selectedIds

    val state = when {
        needsPremium -> ToggleableState.Off
        ids.isEmpty() -> if (emptySelected) ToggleableState.On else ToggleableState.Off
        selectedInCategory == ids.size -> ToggleableState.On
        selectedInCategory == 0 -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    val accentGreen = canSelect && (selectedInCategory > 0 || emptySelected)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .then(if (needsPremium) Modifier.clickable(onClick = onUpgrade) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        when {
                            needsPremium -> colors.divider
                            accentGreen -> MaterialTheme.colorScheme.primary
                            else -> colors.divider
                        }
                    )
            )
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .size(44.dp)
                    .clickable(enabled = checkboxEnabled) {
                        when {
                            needsPremium -> onUpgrade()
                            canSelect -> onToggleCategory()
                            else -> onToggleExpand()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                CleanerTriStateCheckbox(
                    state = state,
                    onClick = when {
                        canSelect -> onToggleCategory
                        checkboxEnabled -> onToggleExpand
                        else -> null
                    },
                    enabled = checkboxEnabled
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable {
                        when {
                            needsPremium -> onUpgrade()
                            canSelect -> onToggleCategory()
                            else -> onToggleExpand()
                        }
                    }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = tr(category.title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!needsPremium) {
                    Text(
                        text = "${formatBytes(selectedBytes)}/${formatBytes(category.totalBytes)} · " +
                            "$selectedInCategory/$totalForUi ${tr("items")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                } else if (category.totalBytes > 0) {
                    Text(
                        text = formatBytes(category.totalBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            if (needsPremium) {
                LockBadge()
            } else if (category.items.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onToggleExpand)
                        .padding(8.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded && !needsPremium && selectableItems.isNotEmpty()) {
            Column {
                selectableItems.take(200).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleItem(item.id) }
                            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardHigh)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatBytes(item.sizeBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        CleanerCheckbox(
                            checked = item.id in selectedIds,
                            onCheckedChange = null,
                            enabled = true
                        )
                    }
                }
                if (selectableItems.size > 200) {
                    Text(
                        text = "+ ${selectableItems.size - 200} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
/** Progress while deleting; when finished advances to Advanced issues (first-run style). */
@Composable
fun CleaningScreen(vm: CleanerViewModel, onFinished: () -> Unit) {
    val clean by vm.clean.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) {
        if (!clean.running && clean.freedBytes < 0) {
            vm.finishCleaning()
        }
    }

    LaunchedEffect(clean.running, clean.freedBytes) {
        if (!clean.running && clean.freedBytes >= 0) {
            delay(700)
            onFinished()
        }
    }

    val showProgress = clean.running || clean.freedBytes < 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showProgress) {
            RingProgress(clean.progress.coerceAtLeast(0.02f), Modifier.size(160.dp), strokeWidth = 6.dp) {
                Text(
                    "${(clean.progress * 100).toInt()}",
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                tr("Cleaning junk…"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                clean.label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        } else {
            IllustrationTile(size = 150)
            Spacer(Modifier.height(28.dp))
            Text(
                text = formatBytes(clean.freedBytes.coerceAtLeast(0)),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tr("freed up"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
