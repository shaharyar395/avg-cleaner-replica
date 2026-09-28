package com.replica.cleaner.data.scan

import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.data.model.AppInfo
import com.replica.cleaner.data.model.AppsSummary
import com.replica.cleaner.data.model.UsageDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Backs the Apps Overview and App Manager screens.
 *
 * Sizes come from StorageStatsManager and usage from UsageStatsManager; both
 * need the "Usage access" special permission, which is exactly why the reference
 * app blocks those screens behind a GRANT ACCESS button.
 */
class AppScanner(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    suspend fun scan(
        deviceTotalBytes: Long,
        loadIcons: Boolean = true
    ): AppsSummary = withContext(Dispatchers.IO) {
        val hasUsage = Permissions.hasUsageAccess(context)
        val statsManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            else null
        val user = Process.myUserHandle()

        val usage = if (hasUsage) usageByPackage(30) else emptyMap()

        val all = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList()
        }

        val infos = all.mapNotNull { app ->
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            var appBytes = 0L
            var dataBytes = 0L
            var cacheBytes = 0L
            if (statsManager != null && hasUsage) {
                try {
                    val s = statsManager.queryStatsForPackage(
                        StorageManager.UUID_DEFAULT, app.packageName, user
                    )
                    appBytes = s.appBytes
                    dataBytes = s.dataBytes
                    cacheBytes = s.cacheBytes
                } catch (_: Exception) {
                    appBytes = apkSize(app)
                }
            } else {
                appBytes = apkSize(app)
            }

            val pkgInfo = try {
                pm.getPackageInfo(app.packageName, 0)
            } catch (_: Exception) {
                null
            }

            val u = usage[app.packageName]
            AppInfo(
                packageName = app.packageName,
                label = runCatching { pm.getApplicationLabel(app).toString() }
                    .getOrDefault(app.packageName),
                isSystem = isSystem,
                appBytes = appBytes,
                dataBytes = dataBytes,
                cacheBytes = cacheBytes,
                firstInstall = pkgInfo?.firstInstallTime ?: 0L,
                lastUsed = u?.lastUsed ?: 0L,
                screenTimeMillis = u?.screenTime ?: 0L,
                launchCount = u?.launches ?: 0,
                icon = if (loadIcons) runCatching { pm.getApplicationIcon(app) }.getOrNull() else null
            )
        }

        AppsSummary(
            installed = infos.filter { !it.isSystem }.sortedByDescending { it.totalBytes },
            system = infos.filter { it.isSystem }.sortedByDescending { it.totalBytes },
            deviceTotalBytes = deviceTotalBytes
        )
    }

    private fun apkSize(app: ApplicationInfo): Long =
        runCatching { java.io.File(app.sourceDir).length() }.getOrDefault(0L)

    private data class Usage(val lastUsed: Long, val screenTime: Long, val launches: Int)

    private fun usageByPackage(days: Int): Map<String, Usage> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()
        val end = System.currentTimeMillis()
        val start = end - TimeUnit.DAYS.toMillis(days.toLong())

        val aggregated = try {
            usm.queryAndAggregateUsageStats(start, end)
        } catch (_: Exception) {
            return emptyMap()
        }

        val launches = launchCounts(usm, start, end)

        return aggregated.mapValues { (pkg, stats) ->
            Usage(
                lastUsed = stats.lastTimeUsed,
                screenTime = stats.totalTimeInForeground,
                launches = launches[pkg] ?: 0
            )
        }
    }

    /** UsageStats has no launch counter before API 29, so count resume events. */
    private fun launchCounts(usm: UsageStatsManager, start: Long, end: Long): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        try {
            val events = usm.queryEvents(start, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val resumed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                } else {
                    @Suppress("DEPRECATION")
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                }
                if (resumed) {
                    counts[event.packageName] = (counts[event.packageName] ?: 0) + 1
                }
            }
        } catch (_: Exception) { /* usage access revoked mid-scan */ }
        return counts
    }

    /** Screen-time per weekday for the bar chart on Apps Overview. */
    suspend fun usageLastWeek(): List<UsageDay> = withContext(Dispatchers.IO) {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@withContext emptyList()
        if (!Permissions.hasUsageAccess(context)) return@withContext emptyList()

        val labels = arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        val out = mutableListOf<UsageDay>()
        val cal = Calendar.getInstance()

        for (offset in 6 downTo 0) {
            val day = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.DAY_OF_YEAR, -offset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = day.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
            val total = try {
                usm.queryAndAggregateUsageStats(dayStart, dayEnd)
                    .values.sumOf { it.totalTimeInForeground }
            } catch (_: Exception) {
                0L
            }
            out += UsageDay(labels[day.get(Calendar.DAY_OF_WEEK) - 1], total)
        }
        out
    }

    /**
     * Uninstall is a system confirmation dialog — the app cannot remove another
     * package itself, so this fires the standard intent for each selection.
     */
    fun uninstallIntent(packageName: String): Intent =
        Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun launchIntent(packageName: String): Intent? =
        pm.getLaunchIntentForPackage(packageName)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun isInstalled(packageName: String): Boolean = try {
        pm.getPackageInfo(packageName, 0)
        true
    } catch (_: Exception) {
        false
    }
}
