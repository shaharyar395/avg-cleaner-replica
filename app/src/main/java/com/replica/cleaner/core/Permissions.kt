package com.replica.cleaner.core

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * The three permissions the onboarding screen counts. Two of them are "special
 * access" grants that can only be toggled in system settings, which is why the
 * reference app shows GO TO SETTINGS rather than a runtime dialog.
 */
enum class CleanerPermission { AllFiles, UsageAccess, Notifications }

data class PermissionStatus(
    val allFiles: Boolean,
    val mediaAccess: Boolean,
    val usageAccess: Boolean,
    val notifications: Boolean
) {
    val missingCount: Int get() =
        listOf(allFiles || mediaAccess, usageAccess, notifications).count { !it }
    val allGranted: Boolean get() = missingCount == 0
    /** Enough access to run a meaningful junk scan (media runtime OR all-files). */
    val canScan: Boolean get() = allFiles || mediaAccess
    val accessGranted: Boolean get() = canScan || notifications
}

object Permissions {

    fun status(context: Context) = PermissionStatus(
        allFiles = hasAllFilesAccess(context),
        mediaAccess = hasMediaAccess(context),
        usageAccess = hasUsageAccess(context),
        notifications = hasNotificationPermission(context)
    )

    fun hasMediaAccess(context: Context): Boolean {
        if (hasAllFilesAccess(context)) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ).any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun mediaPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else emptyArray()

    fun hasAllFilesAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    fun allFilesIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // The per-app variant fails on a few OEM builds, so the caller falls
            // back to the global list if this one cannot be resolved.
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            appDetailsIntent(context)
        }

    fun allFilesFallbackIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

    fun usageAccessIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun notificationSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun appDetailsIntent(context: Context, packageName: String = context.packageName): Intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )

    /** Launches an intent, falling back if the OEM does not expose that screen. */
    fun safeStart(context: Context, intent: Intent, fallback: Intent? = null): Boolean {
        val flagged = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(flagged)
            true
        } catch (_: Exception) {
            if (fallback != null) safeStart(context, fallback, null) else false
        }
    }
}
