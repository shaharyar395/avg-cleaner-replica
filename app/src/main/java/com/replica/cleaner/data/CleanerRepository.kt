package com.replica.cleaner.data

import android.content.Context
import com.replica.cleaner.core.Prefs
import com.replica.cleaner.data.model.AppsSummary
import com.replica.cleaner.data.model.JunkItem
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.data.model.MediaSummary
import com.replica.cleaner.data.model.PhotoAnalysis
import com.replica.cleaner.data.model.ScanResult
import com.replica.cleaner.data.model.StorageSnapshot
import com.replica.cleaner.data.model.SystemInfo
import com.replica.cleaner.data.model.UsageDay
import com.replica.cleaner.data.scan.AppScanner
import com.replica.cleaner.data.scan.DeviceScanner
import com.replica.cleaner.data.scan.JunkScanner
import com.replica.cleaner.data.scan.MediaScanner
import com.replica.cleaner.data.scan.PhotoAnalyzer
import com.replica.cleaner.data.scan.PhotoOptimizer

/**
 * One place the UI talks to. Holds the last scan in memory so moving between
 * Home, Quick Clean, Storage and Tips does not re-walk the filesystem.
 */
class CleanerRepository(context: Context) {

    private val appContext = context.applicationContext

    val prefs = Prefs(appContext)
    private val junkScanner = JunkScanner(appContext)
    private val mediaScanner = MediaScanner(appContext)
    private val photoAnalyzer = PhotoAnalyzer(appContext)
    private val photoOptimizer = PhotoOptimizer(appContext)
    val appScanner = AppScanner(appContext)
    private val deviceScanner = DeviceScanner(appContext)

    @Volatile var lastScan: ScanResult? = null
        private set
    @Volatile var lastMedia: MediaSummary? = null
        private set
    @Volatile var lastPhotos: PhotoAnalysis? = null
        private set
    @Volatile var lastApps: AppsSummary? = null
        private set

    fun storage(): StorageSnapshot = deviceScanner.storage()

    suspend fun systemInfo(): SystemInfo = deviceScanner.systemInfo()

    suspend fun scanJunk(onProgress: (Float, String) -> Unit = { _, _ -> }): ScanResult =
        junkScanner.scan(onProgress = onProgress).also { lastScan = it }

    suspend fun clean(
        items: List<JunkItem>,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): JunkScanner.CleanOutcome {
        val outcome = junkScanner.clean(items, onProgress)
        if (outcome.freedBytes > 0) prefs.recordClean(outcome.freedBytes)
        // Drop the cached scan so the next visit reflects what is actually left.
        lastScan = null
        return outcome
    }

    suspend fun scanMedia(): MediaSummary =
        mediaScanner.summary(storage().totalBytes).also { lastMedia = it }

    suspend fun analyzePhotos(onProgress: (Float) -> Unit = {}): PhotoAnalysis {
        val images = mediaScanner.images()
        return photoAnalyzer.analyze(images, onProgress = onProgress).also { lastPhotos = it }
    }

    suspend fun images(limit: Int = 2000): List<MediaFile> = mediaScanner.images(limit)
    suspend fun videos(limit: Int = 1000): List<MediaFile> = mediaScanner.videos(limit)
    suspend fun audio(limit: Int = 1000): List<MediaFile> = mediaScanner.audio(limit)
    suspend fun otherFiles(limit: Int = 500): List<MediaFile> = mediaScanner.otherFiles(limit)

    suspend fun deleteMedia(files: List<MediaFile>): Long =
        mediaScanner.delete(files).also { if (it > 0) prefs.recordClean(it) }

    suspend fun optimizePhotos(
        files: List<MediaFile>,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): PhotoOptimizer.Outcome =
        photoOptimizer.optimize(files, onProgress = onProgress).also {
            if (it.freedBytes > 0) {
                prefs.recordClean(it.freedBytes)
                lastPhotos = null
                lastMedia = null
            }
        }

    suspend fun scanApps(): AppsSummary =
        appScanner.scan(storage().totalBytes).also { lastApps = it }

    suspend fun usageLastWeek(): List<UsageDay> = appScanner.usageLastWeek()

    fun invalidate() {
        lastScan = null
        lastMedia = null
        lastPhotos = null
        lastApps = null
    }

    companion object {
        @Volatile private var instance: CleanerRepository? = null

        fun get(context: Context): CleanerRepository =
            instance ?: synchronized(this) {
                instance ?: CleanerRepository(context).also { instance = it }
            }
    }
}
