package com.replica.cleaner.data.model

import android.graphics.drawable.Drawable
import android.net.Uri

/** One deletable thing found by a scan. */
data class JunkItem(
    val id: String,
    val label: String,
    val path: String?,
    val uri: Uri?,
    val sizeBytes: Long,
    val categoryId: String,
    val icon: Drawable? = null,
    val lastModified: Long = 0L
)

/**
 * A Quick Clean row. `locked` marks the two categories the reference app puts
 * behind its paywall (hidden caches, browser data).
 */
data class JunkCategory(
    val id: String,
    val title: String,
    val description: String,
    val group: CategoryGroup,
    val locked: Boolean = false,
    val items: List<JunkItem> = emptyList(),
    val reportedSizeBytes: Long = items.sumOf { it.sizeBytes }
) {
    val totalBytes: Long get() = if (items.isEmpty()) reportedSizeBytes else items.sumOf { it.sizeBytes }
    val itemCount: Int get() = items.size
}

enum class CategoryGroup { Unneeded, Review }

/** Result of a full Quick Clean scan. */
data class ScanResult(
    val categories: List<JunkCategory> = emptyList(),
    val scannedAt: Long = 0L
) {
    val unneeded get() = categories.filter { it.group == CategoryGroup.Unneeded }
    val review get() = categories.filter { it.group == CategoryGroup.Review }
    val unneededBytes get() = unneeded.filter { !it.locked }.sumOf { it.totalBytes }
    val hiddenCacheBytes get() = categories.firstOrNull { it.id == "hidden_caches" }?.totalBytes ?: 0L
    val reviewBytes get() = review.sumOf { it.totalBytes }
    val cleanableBytes get() = unneededBytes + hiddenCacheBytes + reviewBytes
}

data class StorageSnapshot(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L
) {
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0L)
    val usedFraction: Float get() = if (totalBytes == 0L) 0f else usedBytes.toFloat() / totalBytes
}

data class MediaBucket(
    val count: Int = 0,
    val bytes: Long = 0L
)

data class MediaSummary(
    val photos: MediaBucket = MediaBucket(),
    val video: MediaBucket = MediaBucket(),
    val audio: MediaBucket = MediaBucket(),
    val others: MediaBucket = MediaBucket(),
    val totalDeviceBytes: Long = 0L
) {
    val mediaBytes: Long get() = photos.bytes + video.bytes + audio.bytes
    val mediaFraction: Float
        get() = if (totalDeviceBytes == 0L) 0f else mediaBytes.toFloat() / totalDeviceBytes
}

data class MediaFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val bucket: String,
    val width: Int = 0,
    val height: Int = 0
)

/** Output of the on-device photo analysis (blur / duplicate / age heuristics). */
data class PhotoAnalysis(
    val similar: List<List<MediaFile>> = emptyList(),
    val badQuality: List<MediaFile> = emptyList(),
    val old: List<MediaFile> = emptyList(),
    val screenshots: List<MediaFile> = emptyList(),
    val optimizable: List<MediaFile> = emptyList()
) {
    val similarFlat: List<MediaFile> get() = similar.flatMap { it.drop(1) }
    fun bytesOf(files: List<MediaFile>) = files.sumOf { it.sizeBytes }
}

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val appBytes: Long,
    val dataBytes: Long,
    val cacheBytes: Long,
    val firstInstall: Long,
    val lastUsed: Long,
    val screenTimeMillis: Long,
    val launchCount: Int,
    val icon: Drawable? = null
) {
    val totalBytes: Long get() = appBytes + dataBytes + cacheBytes
}

data class AppsSummary(
    val installed: List<AppInfo> = emptyList(),
    val system: List<AppInfo> = emptyList(),
    val deviceTotalBytes: Long = 0L
) {
    val installedCount get() = installed.size
    val systemCount get() = system.size
    val appsBytes get() = (installed + system).sumOf { it.totalBytes }
    val appsFraction: Float
        get() = if (deviceTotalBytes == 0L) 0f else appsBytes.toFloat() / deviceTotalBytes
    val unused: List<AppInfo>
        get() = installed.filter {
            it.lastUsed > 0L && System.currentTimeMillis() - it.lastUsed > 30L * 24 * 3600 * 1000
        }
    val dataDrainer: AppInfo? get() = installed.maxByOrNull { it.dataBytes }
    val storageDrainer: AppInfo? get() = (installed + system).maxByOrNull { it.totalBytes }
    val batteryDrainer: AppInfo? get() = installed.maxByOrNull { it.screenTimeMillis }
}

data class SystemInfo(
    val androidVersion: String = "",
    val androidCodename: String = "",
    val uptimeMillis: Long = 0L,
    val model: String = "",
    val wifiEnabled: Boolean = false,
    val ssid: String? = null,
    val ipAddress: String? = null,
    val bluetoothOn: Boolean = false,
    val mobileDataOn: Boolean = false,
    val ramUsedBytes: Long = 0L,
    val ramAvailableBytes: Long = 0L,
    val storageUsedBytes: Long = 0L,
    val storageAvailableBytes: Long = 0L,
    val sdCardUsedBytes: Long = 0L,
    val sdCardAvailableBytes: Long = 0L,
    val sdCardPresent: Boolean = false,
    val batteryPercent: Int = 0,
    val batteryCelsius: Float = 0f,
    val cpuUsedPercent: Int = 0
) {
    val ramTotal get() = ramUsedBytes + ramAvailableBytes
    val storageTotal get() = storageUsedBytes + storageAvailableBytes
    val sdCardTotal get() = sdCardUsedBytes + sdCardAvailableBytes
    val batteryFahrenheit: Float get() = batteryCelsius * 9f / 5f + 32f
    val cpuIdlePercent: Int get() = (100 - cpuUsedPercent).coerceIn(0, 100)
}

/** One card on the "space saving tips" screen. */
data class Tip(
    val id: String,
    val index: Int,
    val title: String,
    val subtitle: String? = null,
    val kind: TipKind,
    val payloadBytes: Long = 0L,
    val premium: Boolean = false
)

enum class TipKind {
    UnnecessaryData, AppDiary, RarelyUsed, NotUsed, BadPhotos,
    Screenshots, SimilarPhotos, OldPhotos, LargeVideos, BigFiles,
    Downloads, EmptyFolders, OptimizableImages
}

data class UsageDay(val label: String, val millis: Long)
