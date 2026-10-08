package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.replica.cleaner.core.CloudProviders
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.data.model.AppInfo
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCheckbox
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.components.SettingRow
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CloudTransfersScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onCloudSettings: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val connectedIds by vm.prefs.connectedCloudProviders.collectAsStateWithLifecycle(
        initialValue = emptySet()
    )
    var files by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var transferring by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        files = (vm.videos(200) + vm.otherFiles(200))
            .sortedByDescending { it.sizeBytes }
            .take(80)
    }

    val hasCloud = connectedIds.isNotEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Cloud Transfers"), onBack = onBack, centered = false)

        if (!hasCloud) {
            // AVG empty state: cloud+gear, message, MANAGE CLOUD SERVICES
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.55f),
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = tr("No cloud service connected"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                PrimaryButton(
                    text = tr("MANAGE CLOUD SERVICES"),
                    onClick = onCloudSettings
                )
            }
        } else {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = tr("Connected services"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                connectedIds.forEach { id ->
                    val provider = CloudProviders.byId(id) ?: return@forEach
                    SettingRow(
                        title = provider.displayName,
                        subtitle = tr("Connected"),
                        icon = Icons.Default.Cloud,
                        trailingText = "ON",
                        showChevron = true,
                        onClick = onCloudSettings
                    )
                }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(tr("MANAGE CLOUD SERVICES"), onCloudSettings)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = tr("Large files to move"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                if (files.isEmpty()) {
                    Text(
                        tr("No large files found yet. Grant All files access and scan again."),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                } else {
                    files.take(30).forEach { file ->
                        val id = file.uri.toString()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = if (id in selected) selected - id else selected + id
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (file.name.contains('.', ignoreCase = true) &&
                                    file.name.substringAfterLast('.').lowercase() in
                                    setOf("mp4", "mkv", "mov", "avi", "webm")
                                ) Icons.Default.Movie else Icons.Default.Folder,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    formatBytes(file.sizeBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            CleanerCheckbox(checked = id in selected, onCheckedChange = null)
                        }
                    }
                }
                Spacer(Modifier.height(100.dp))
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .padding(16.dp)
            ) {
                if (transferring) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(status ?: tr("Preparing…"), color = colors.textSecondary)
                } else if (status != null) {
                    Text(status ?: "", color = colors.textSecondary)
                    Spacer(Modifier.height(8.dp))
                }
                val selectedFiles = files.filter { it.uri.toString() in selected }
                val linkedName = connectedIds.firstOrNull()?.let { CloudProviders.byId(it)?.displayName }
                PrimaryButton(
                    text = if (selectedFiles.isEmpty()) tr("SELECT FILES TO TRANSFER")
                    else tr("TRANSFER %d FILES").replace("%d", selectedFiles.size.toString()),
                    enabled = selectedFiles.isNotEmpty() && !transferring,
                    onClick = {
                        transferring = true
                        progress = 0f
                        status = "Uploading to ${linkedName ?: "cloud"}…"
                        val uris = ArrayList(selectedFiles.map { it.uri })
                        val share = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "*/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        Permissions.safeStart(
                            context,
                            Intent.createChooser(share, "Transfer with")
                        )
                        progress = 1f
                        transferring = false
                        status = "Handed ${selectedFiles.size} files to your cloud app. " +
                            "Delete local copies from Media when the upload finishes."
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoOptimizerScreen(vm: CleanerViewModel, onBack: () -> Unit, onUpgrade: () -> Unit) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val photos by vm.photos.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    var files by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var label by remember { mutableStateOf("") }
    var resultMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.analyzePhotos()
    }
    LaunchedEffect(photos) {
        files = photos?.optimizable.orEmpty().ifEmpty {
            vm.images(400).filter { it.sizeBytes >= 400_000L }
        }
        selected = files.map { it.uri.toString() }.toSet()
    }

    if (!premium) {
        OptimizerPaywall(
            title = tr("Photo Optimizer"),
            body = tr("Shrink your photos and free up storage space while keeping the memories."),
            onBack = onBack,
            onUpgrade = onUpgrade
        )
        return
    }

    val selectedFiles = files.filter { it.uri.toString() in selected }
    val beforeBytes = selectedFiles.sumOf { it.sizeBytes }
    val estimateAfter = (beforeBytes * 0.35).toLong()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Photo Optimizer"), onBack = onBack, centered = false)

        if (running) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.7f))
                Spacer(Modifier.height(12.dp))
                Text(label, color = colors.textSecondary, maxLines = 1)
            }
            return
        }

        Column(Modifier.weight(1f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatBytes(beforeBytes), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge)
                    Text(tr("Before"), color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Text("→", color = colors.textSecondary, style = MaterialTheme.typography.headlineSmall)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatBytes(estimateAfter), color = Color(0xFFFFA000),
                        style = MaterialTheme.typography.titleLarge)
                    Text(tr("After (est.)"), color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            resultMsg?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "${files.size} optimizable (${formatBytes(files.sumOf { it.sizeBytes })})",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (selected.size == files.size && files.isNotEmpty()) tr("DESELECT ALL") else tr("SELECT ALL"),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable {
                        selected = if (selected.size == files.size) emptySet()
                        else files.map { it.uri.toString() }.toSet()
                    }
                )
            }
            if (files.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(tr("No large photos found to optimize."), color = colors.textSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(files, key = { it.uri.toString() }) { file ->
                        val id = file.uri.toString()
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.cardHigh)
                                .clickable {
                                    selected = if (id in selected) selected - id else selected + id
                                }
                        ) {
                            AsyncImage(
                                model = file.uri,
                                contentDescription = file.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                formatBytes(file.sizeBytes),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            CleanerCheckbox(
                                checked = id in selected,
                                onCheckedChange = null,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.card)
                .padding(16.dp)
        ) {
            Text(
                "${selectedFiles.size} SELECTED · save ~${formatBytes(beforeBytes - estimateAfter)}",
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = tr("OPTIMIZE PHOTOS"),
                enabled = selectedFiles.isNotEmpty(),
                onClick = {
                    running = true
                    progress = 0f
                    vm.optimizePhotos(
                        files = selectedFiles,
                        onProgress = { p, name ->
                            progress = p
                            label = name
                        },
                        onDone = { freed, ok, failed ->
                            running = false
                            resultMsg = "Optimized $ok photos · freed ${formatBytes(freed)}" +
                                if (failed > 0) " · $failed skipped" else ""
                            selected = emptySet()
                            files = files.filterNot { it in selectedFiles }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun VideoOptimizerScreen(vm: CleanerViewModel, onBack: () -> Unit, onUpgrade: () -> Unit) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    var files by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var status by remember { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var label by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        files = vm.videos(300).filter { it.sizeBytes >= 8_000_000L }
            .sortedByDescending { it.sizeBytes }
        selected = files.take(10).map { it.uri.toString() }.toSet()
    }

    if (!premium) {
        OptimizerPaywall(
            title = tr("Video Optimizer"),
            body = tr("Lighter videos, same moments. Free space by reviewing oversized clips."),
            onBack = onBack,
            onUpgrade = onUpgrade
        )
        return
    }

    val selectedFiles = files.filter { it.uri.toString() in selected }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Video Optimizer"), onBack = onBack, centered = false)
        Text(
            tr("Large videos use the most space. Optimize selected clips to a smaller 720p file."),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        if (running) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.7f))
                Spacer(Modifier.height(12.dp))
                Text(label, color = colors.textSecondary, maxLines = 1)
            }
            return
        }
        status?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 20.dp))
        }
        LazyColumn(Modifier.weight(1f)) {
            items(files, key = { it.uri.toString() }) { file ->
                val id = file.uri.toString()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = if (id in selected) selected - id else selected + id
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardHigh)
                    ) {
                        AsyncImage(
                            model = file.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            file.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(formatBytes(file.sizeBytes), color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    CleanerCheckbox(checked = id in selected, onCheckedChange = null)
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.card)
                .padding(16.dp)
        ) {
            Text(
                "${selectedFiles.size} SELECTED (${formatBytes(selectedFiles.sumOf { it.sizeBytes })})",
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = tr("OPTIMIZE VIDEOS"),
                enabled = selectedFiles.isNotEmpty(),
                onClick = {
                    running = true
                    progress = 0f
                    vm.optimizeVideos(
                        files = selectedFiles,
                        onProgress = { p, name ->
                            progress = p
                            label = name
                        },
                        onDone = { freed, ok, failed ->
                            running = false
                            status = "Optimized $ok videos · freed ${formatBytes(freed)}" +
                                if (failed > 0) " · $failed skipped" else ""
                            selected = emptySet()
                            files = files.filterNot { it in selectedFiles }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun SleepModeScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        vm.loadApps()
        delay(700)
        loading = false
    }

    if (loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(Modifier.fillMaxSize()) {
                CleanerTopBar("", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        return
    }

    if (!premium) {
        FeatureUpsellPaywall(
            title = tr("Sleep Mode"),
            body = tr("Cut distractions by pausing multiple apps — no activity or notifications until you reopen them."),
            faqs = listOf(
                tr("What is the purpose of Sleep Mode?") to
                    tr("Sleep Mode uses Force stop action and puts apps to sleep until you need them again. Force stopped apps can't access system resources and thus have no effect on battery, data, memory, and storage space. You can wake up stopped apps by reopening them any time."),
                tr("How does Force stop work?") to
                    tr("Force stop stops all app processes on the spot. Usually you need to open your settings to Force stop each app one-by-one, but Sleep Mode lets you stop multiple apps at once."),
                tr("Should I be concerned about force stopping apps?") to
                    tr("Nope! When you force stop apps, there's no risk of losing data, preferences, or account info.\n\nBut don't forget — force stopped apps can't send you notifications or work in the background, so you shouldn't force stop apps that send important info (like security or messaging apps)."),
                tr("Can force stopped apps restart by themselves?") to
                    tr("There are a few exception apps that can wake themselves up without being opened (like Facebook Messenger and some system apps), but most apps will stay force stopped until you need them.")
            ),
            onBack = onBack,
            onPurchased = { vm.setPremium(true) },
            onAlreadyPurchased = onAlreadyPurchased
        )
        return
    }

    // Premium unlocked — app picker. Back leaves Sleep Mode to the previous screen (Home/Tools).
    val list: List<AppInfo> = apps?.installed.orEmpty()
        .sortedByDescending { it.screenTimeMillis }
        .take(80)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Sleep Mode"), onBack = onBack, centered = false)
        Text(
            tr("Pick apps to force-stop. Android will confirm each one in system settings."),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyColumn(Modifier.weight(1f)) {
            items(list, key = { it.packageName }) { app ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = if (app.packageName in selected) selected - app.packageName
                            else selected + app.packageName
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = app.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                        Text(
                            formatBytes(app.totalBytes),
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    CleanerCheckbox(checked = app.packageName in selected, onCheckedChange = null)
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.card)
                .padding(16.dp)
        ) {
            PrimaryButton(
                text = if (selected.isEmpty()) tr("SELECT APPS") else tr("PUT %d APPS TO SLEEP").replace("%d", selected.size.toString()),
                enabled = selected.isNotEmpty(),
                onClick = {
                    selected.forEach { pkg -> vm.openAppSettings(pkg) }
                }
            )
        }
    }
}

@Composable
fun DeepCleanScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onQuickClean: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    if (!premium) {
        FeatureUpsellScreen(
            vm = vm,
            featureId = "deep_clean",
            onBack = onBack,
            onAlreadyPurchased = onAlreadyPurchased
        )
        return
    }
    FeatureActionScreen(
        title = tr("Deep Clean"),
        body = tr("Deep Clean walks hidden caches and browser leftovers. Start Quick Clean to review and remove what Android allows."),
        cta = tr("OPEN QUICK CLEAN"),
        onBack = onBack,
        onCta = onQuickClean
    )
}

@Composable
fun BrowserCleanerScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onQuickClean: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    if (!premium) {
        FeatureUpsellScreen(
            vm = vm,
            featureId = "browser_cleaner",
            onBack = onBack,
            onAlreadyPurchased = onAlreadyPurchased
        )
        return
    }
    FeatureActionScreen(
        title = tr("Browser Cleaner"),
        body = tr("Browser data sizes are measured in Quick Clean. Open the locked Browser data row to jump into each browser's storage screen."),
        cta = tr("OPEN QUICK CLEAN"),
        onBack = onBack,
        onCta = onQuickClean
    )
}

@Composable
private fun OptimizerPaywall(
    title: String,
    body: String,
    onBack: () -> Unit,
    onUpgrade: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title, onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tr("Premium feature"), color = colors.amber, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(12.dp))
            IllustrationTile(size = 140)
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Text(body, color = colors.textSecondary, textAlign = TextAlign.Center)
        }
        Column(Modifier.padding(16.dp)) {
            PrimaryButton(tr("UPGRADE"), onUpgrade)
        }
    }
}

@Composable
private fun FeatureActionScreen(
    title: String,
    body: String,
    cta: String,
    onBack: () -> Unit,
    onCta: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title, onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IllustrationTile(size = 140)
            Spacer(Modifier.height(20.dp))
            Text(body, color = colors.textSecondary, textAlign = TextAlign.Center)
        }
        Column(Modifier.padding(16.dp)) {
            PrimaryButton(cta, onCta)
        }
    }
}
