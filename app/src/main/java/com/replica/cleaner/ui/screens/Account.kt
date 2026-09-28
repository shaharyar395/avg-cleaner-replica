package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.replica.cleaner.R
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerCard
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.Divider
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SectionLabel
import com.replica.cleaner.ui.components.SettingRow
import com.replica.cleaner.ui.theme.LocalCleanerColors

@Composable
fun AccountScreen(
    vm: CleanerViewModel,
    onUpgrade: () -> Unit,
    onSignIn: () -> Unit,
    onRedeem: () -> Unit,
    onFeatures: () -> Unit,
    onSettings: () -> Unit,
    onThemes: () -> Unit,
    onAbout: () -> Unit
) {
    val premium by vm.premium.collectAsStateWithLifecycle()
    val email by vm.prefs.email.collectAsStateWithLifecycle(initialValue = null)
    val totalFreed by vm.prefs.totalFreed.collectAsStateWithLifecycle(initialValue = 0L)
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val signedIn = email != null
    val t = LocalL10n.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // Sign-in header (matches AVG Account)
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF4DB8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF0A1A28),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.size(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (signedIn) email!! else tr("You're not signed in"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (signedIn) tr("Signed in on this device")
                        else tr("Sign in to use a connected subscription or receive account alerts on this device."),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            PrimaryButton(
                text = if (signedIn) tr("MANAGE ACCOUNT") else t.signIn,
                onClick = onSignIn,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionLabel(tr("Your plan"))

            CleanerCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .border(1.dp, colors.divider.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.divider.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (premium) tr("Premium") else tr("Free"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = tr("AVG Cleanup"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (premium) tr("All features unlocked.")
                            else tr("You're using the free version of Cleanup."),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    IllustrationTile(size = 72)
                }
                if (!premium) {
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(t.upgrade, onUpgrade, color = colors.amber)
                }
                if (totalFreed > 0) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = tr("You've freed %s with Cleaner so far.").replace("%s", formatBytes(totalFreed)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Each row is its own card (matches AVG Account screenshot)
            AccountMenuCard(
                title = if (signedIn) email!! else tr("Already have a subscription?"),
                subtitle = if (signedIn) tr("Signed in")
                else tr("Subscription bought elsewhere can be redeemed here"),
                icon = Icons.Default.Redeem,
                onClick = if (signedIn) onSignIn else onRedeem
            )
            AccountMenuCard(
                title = t.exploreFeatures,
                subtitle = tr("See what you can do with this app"),
                icon = Icons.Default.Explore,
                onClick = onFeatures
            )

            SectionLabel(tr("Preferences"))
            AccountMenuCard(
                title = t.settings,
                subtitle = tr("Set up preferences for all your tools"),
                icon = Icons.Default.Settings,
                onClick = onSettings
            )
            AccountMenuCard(
                title = t.themes,
                subtitle = tr("Set up theme to your liking"),
                icon = Icons.Default.Palette,
                onClick = onThemes
            )

            SectionLabel(tr("Help & info"))
            AccountMenuCard(
                title = tr("Help & Feedback"),
                subtitle = tr("Contact us on our website"),
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://support.avg.com/"))
                        )
                    }
                }
            )
            AccountMenuCard(
                title = t.about,
                icon = Icons.Default.Info,
                onClick = onAbout
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AccountMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = Icons.Default.ChevronRight
) {
    val colors = LocalCleanerColors.current
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit, onLicenses: () -> Unit) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current

    val versionLabel = remember(context) {
        val pm = context.packageManager
        val info = try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
        val name = info?.versionName ?: "1.0"
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info?.longVersionCode ?: 0L
        } else {
            @Suppress("DEPRECATION")
            info?.versionCode?.toLong() ?: 0L
        }
        "v. $name ($code)"
    }
    val appLabel = remember(context) {
        runCatching {
            context.applicationInfo.loadLabel(context.packageManager).toString()
        }.getOrDefault(context.getString(R.string.app_name))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("About this app"), onBack = onBack, centered = false)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // AVG: large centered app icon + name + version.
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1E2630)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_brand),
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = appLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = versionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = tr("Open source"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onLicenses)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = tr("End User Licence Agreement"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.avg.com/en-ww/eula")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

private data class OssLibrary(
    val name: String,
    val version: String,
    val author: String,
    val license: String
)

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val colors = LocalCleanerColors.current
    val libraries = remember {
        listOf(
            OssLibrary("Jetpack Compose", "1.7.x", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("Activity Compose", "1.9.x", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("Navigation Compose", "2.8.5", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("Lifecycle Runtime Compose", "2.8.7", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("Material3", "1.3.x", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("DataStore Preferences", "1.1.1", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("WorkManager", "2.10.0", "The Android Open Source Project", "The Apache Software License, Version 2.0"),
            OssLibrary("Coil Compose", "2.7.0", "Coil Contributors", "The Apache Software License, Version 2.0"),
            OssLibrary("Coil Video", "2.7.0", "Coil Contributors", "The Apache Software License, Version 2.0"),
            OssLibrary("Accompanist Permissions", "0.36.0", "Google", "The Apache Software License, Version 2.0"),
            OssLibrary("Kotlin Coroutines", "1.9.x", "JetBrains", "The Apache Software License, Version 2.0"),
            OssLibrary("Kotlin Stdlib", "2.0.x", "JetBrains", "The Apache Software License, Version 2.0")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CleanerTopBar(tr("Open-source libraries"), onBack = onBack, centered = false)
        LazyColumn(Modifier.weight(1f)) {
            items(libraries) { lib ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lib.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = lib.version,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = lib.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = lib.license,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF07281A),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
