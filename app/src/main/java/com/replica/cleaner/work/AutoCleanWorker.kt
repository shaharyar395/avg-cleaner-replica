package com.replica.cleaner.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.data.CleanerRepository
import com.replica.cleaner.data.model.CategoryGroup
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Settings > Automatic Cleaning. Runs on the chosen cadence, cleans only the
 * categories the user ticked, and posts a notification when it frees more than
 * the configured threshold.
 *
 * "Files to review" is never touched automatically — same as the reference app.
 */
class AutoCleanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo =
        ForegroundInfo(NOTIF_ID_PROGRESS, Notifications.progressNotification(applicationContext, "Cleaning…"))

    override suspend fun doWork(): Result {
        val repo = CleanerRepository.get(applicationContext)
        val prefs = repo.prefs

        if (!prefs.autoCleanEnabled.first()) return Result.success()
        if (!Permissions.hasAllFilesAccess(applicationContext)) return Result.success()

        val allowed = prefs.autoCleanCategories.first()
        val keepDays = prefs.downloadsKeepDays.first()
        val cutoff = System.currentTimeMillis() - keepDays * 24L * 3600 * 1000

        return try {
            val scan = repo.scanJunk()
            val items = scan.categories
                .filter { !it.locked && it.id in allowed && it.group == CategoryGroup.Unneeded }
                .flatMap { it.items }
                // Only age-filter downloads (and similar review items if ever enabled).
                .filter { item ->
                    if (item.categoryId == "downloads") {
                        item.lastModified == 0L || item.lastModified < cutoff
                    } else {
                        true
                    }
                }

            if (items.isEmpty()) return Result.success()

            val outcome = repo.clean(items)
            val thresholdMb = prefs.notifThresholdMb.first()
            // -1 Never, 0 Always, else notify when freed >= threshold MB
            val shouldNotify = when {
                thresholdMb < 0 -> false
                thresholdMb == 0 -> true
                else -> outcome.freedBytes >= thresholdMb * 1_000_000L
            }

            if (shouldNotify &&
                prefs.notificationsMaster.first() &&
                "junk_cleaning" in prefs.notifChannels.first()
            ) {
                Notifications.notify(
                    applicationContext,
                    Notifications.CHANNEL_CLEANING,
                    NOTIF_ID_RESULT,
                    "Automatic Cleaning finished",
                    "Freed ${formatBytes(outcome.freedBytes)} across ${outcome.deleted} items."
                )
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "auto_clean"
        private const val NOTIF_ID_PROGRESS = 1001
        private const val NOTIF_ID_RESULT = 1002

        fun schedule(context: Context, frequency: String) {
            val hours = when (frequency) {
                "Daily" -> 24L
                "Every 3 days" -> 24L * 3
                "Weekly" -> 24L * 7
                "Every 2 weeks" -> 24L * 14
                "Monthly" -> 24L * 30
                else -> 24L * 7
            }
            val request = PeriodicWorkRequestBuilder<AutoCleanWorker>(hours, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
