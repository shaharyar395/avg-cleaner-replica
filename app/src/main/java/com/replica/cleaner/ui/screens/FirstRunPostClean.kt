package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatPercent
import com.replica.cleaner.data.model.JunkItem
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.theme.Amber
import com.replica.cleaner.ui.theme.LocalCleanerColors

/**
 * In-app "Permission needed" pitch — ALLOW triggers the system Allow / Don't allow
 * dialog (media + notifications). Never leaves the app for Settings.
 */
@Composable
fun PermissionNeededScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onGranted: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        vm.refreshPermissions()
        val status = Permissions.status(context)
        if (status.canScan || status.notifications) onGranted()
        else onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title = "", onBack = onBack, closeIcon = true, centered = false)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.2f))
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = tr("Permission needed"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = tr("Photos, media & files"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE57373),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = tr(
                    "This allows us to find and clean junk on your device, and send tips about cleaning, apps, and photos."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(0.45f))
            PrimaryButton(
                text = tr("ALLOW PERMISSION"),
                onClick = {
                    val perms = Permissions.mediaPermissions()
                    if (perms.isEmpty()) onGranted()
                    else launcher.launch(perms)
                }
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(tr("NOT NOW"), onBack)
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Premium leftover issues after first Quick Clean (Deep Clean / Browser / Sleep). */
@Composable
fun AdvancedIssuesScreen(
    vm: CleanerViewModel,
    onDetailedResults: () -> Unit,
    onResolveAll: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val scan by vm.scan.collectAsStateWithLifecycle()
    val clean by vm.clean.collectAsStateWithLifecycle()
    val hiddenBytes = scan.result?.hiddenCacheBytes ?: 0L
    val cleanedCount = clean.cleanedItems.size.coerceAtLeast(clean.deleted)

    val issues = listOf(
        AdvancedIssue(
            Icons.Default.Visibility,
            if (hiddenBytes > 0) "${formatBytes(hiddenBytes)} remaining to be cleaned"
            else tr("Hidden junk remaining"),
            tr("Enable Deep Clean to remove hidden junk files.")
        ),
        AdvancedIssue(
            Icons.Default.Language,
            tr("Clean browser data"),
            tr("Clean up records that are stored when you browse or search online.")
        ),
        AdvancedIssue(
            Icons.Default.AcUnit,
            tr("Put unused apps to sleep"),
            tr("Prevent background activity and suspend notifications until you reopen those apps.")
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title = "", onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Amber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = tr("%d advanced issues").replace("%d", "${issues.size}"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Amber,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = tr("Resolve these issues with AVG Cleaner Premium"),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDetailedResults)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tr("Detailed results"),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$cleanedCount items >",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            issues.forEach { issue ->
                AdvancedIssueCard(issue)
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            PrimaryButton(tr("RESOLVE ALL"), onResolveAll)
            Spacer(Modifier.height(10.dp))
            Text(
                text = tr("SKIP FOR NOW"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSkip)
                    .padding(12.dp)
            )
        }
    }
}

private data class AdvancedIssue(
    val icon: ImageVector,
    val title: String,
    val body: String
)

@Composable
private fun AdvancedIssueCard(issue: AdvancedIssue) {
    val colors = LocalCleanerColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF4A90C8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(issue.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Box(
                Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Amber),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = tr(issue.title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = tr(issue.body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Expanded list of items removed in the last Quick Clean. */
@Composable
fun CleaningResultsScreen(vm: CleanerViewModel, onBack: () -> Unit) {
    val clean by vm.clean.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    var expanded by remember { mutableStateOf(true) }
    val items = clean.cleanedItems

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Cleaning results"), onBack = onBack, centered = false)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Cleaned"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${items.size.coerceAtLeast(clean.deleted)} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = colors.textSecondary
                )
            }
            if (expanded) {
                items.forEach { item ->
                    CleanedItemRow(item)
                }
                if (items.isEmpty()) {
                    Text(
                        tr("Junk removed from this device."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanedItemRow(item: JunkItem) {
    val colors = LocalCleanerColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF4A90C8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Folder, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                item.categoryId.replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Text(
            formatBytes(item.sizeBytes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** "You cleaned X" summary after skip / back from resolve premium. */
@Composable
fun SpaceCleanedScreen(
    vm: CleanerViewModel,
    onGoToDashboard: () -> Unit,
    onClose: () -> Unit
) {
    val clean by vm.clean.collectAsStateWithLifecycle()
    val storage by vm.storage.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    LaunchedEffect(Unit) { vm.refreshStorage() }
    val freed = clean.freedBytes.coerceAtLeast(0L)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title = "", onBack = onClose, closeIcon = true, centered = false)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tr("You cleaned %s").replace("%s", formatBytes(freed)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
            Icon(
                Icons.Default.PhoneAndroid,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(36.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${formatPercent(storage.usedFraction)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(tr("Used space"), color = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatBytes(storage.freeBytes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(tr("Free space"), color = colors.textSecondary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                tr("All done cleaning. Now you can explore all the other features."),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            PrimaryButton(tr("GO TO DASHBOARD"), onGoToDashboard)
            Spacer(Modifier.height(20.dp))
        }
    }
}
