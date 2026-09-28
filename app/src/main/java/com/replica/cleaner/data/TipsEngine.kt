package com.replica.cleaner.data

import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.data.model.AppsSummary
import com.replica.cleaner.data.model.MediaSummary
import com.replica.cleaner.data.model.PhotoAnalysis
import com.replica.cleaner.data.model.ScanResult
import com.replica.cleaner.data.model.Tip
import com.replica.cleaner.data.model.TipKind

/**
 * Builds the numbered cards on the "space saving tips" screen.
 *
 * Tips are only emitted when they have something real to report, which is why
 * the header count moves ("13 space saving tips") instead of being fixed. The
 * order follows the user's list in Settings > Analysis preferences.
 */
object TipsEngine {

    fun build(
        scan: ScanResult?,
        media: MediaSummary?,
        photos: PhotoAnalysis?,
        apps: AppsSummary?,
        priority: List<String>
    ): List<Tip> {
        val candidates = mutableListOf<Pair<String, Tip>>()

        scan?.let { s ->
            if (s.cleanableBytes > 0) {
                candidates += "Junk cleaning" to Tip(
                    id = "unnecessary_data",
                    index = 0,
                    title = "Unnecessary data",
                    kind = TipKind.UnnecessaryData,
                    payloadBytes = s.cleanableBytes
                )
            }
            s.categories.firstOrNull { it.id == "empty_folders" }?.let { c ->
                if (c.itemCount > 0) {
                    candidates += "Junk cleaning" to Tip(
                        id = "empty_folders",
                        index = 0,
                        title = "${c.itemCount} empty folders",
                        subtitle = "Folders with nothing inside",
                        kind = TipKind.EmptyFolders,
                        payloadBytes = c.totalBytes
                    )
                }
            }
            s.categories.firstOrNull { it.id == "downloads" }?.let { c ->
                if (c.itemCount > 0) {
                    candidates += "Other files" to Tip(
                        id = "downloads",
                        index = 0,
                        title = "${c.itemCount} files in Downloads",
                        subtitle = "${formatBytes(c.totalBytes)} can be reviewed",
                        kind = TipKind.Downloads,
                        payloadBytes = c.totalBytes
                    )
                }
            }
        }

        apps?.let { a ->
            if (a.installed.isNotEmpty()) {
                candidates += "Apps" to Tip(
                    id = "app_diary",
                    index = 0,
                    title = "Your app diary",
                    subtitle = "How much time you spent in each app?",
                    kind = TipKind.AppDiary
                )
            }
            val rarely = a.unused
            if (rarely.isNotEmpty()) {
                candidates += "Apps" to Tip(
                    id = "rarely_used",
                    index = 0,
                    title = "Rarely used apps",
                    subtitle = "Free up to ${formatBytes(rarely.sumOf { it.totalBytes })}",
                    kind = TipKind.RarelyUsed,
                    payloadBytes = rarely.sumOf { it.totalBytes }
                )
                candidates += "Apps" to Tip(
                    id = "not_used",
                    index = 0,
                    title = "Not used",
                    subtitle = "You have spent least time using ${rarely.first().label}",
                    kind = TipKind.NotUsed,
                    payloadBytes = rarely.first().totalBytes
                )
            }
        }

        photos?.let { p ->
            if (p.badQuality.isNotEmpty()) {
                candidates += "Photos and video" to Tip(
                    id = "bad_photos",
                    index = 0,
                    title = "${p.badQuality.size} bad photos found",
                    subtitle = "${formatBytes(p.bytesOf(p.badQuality))} can be cleaned",
                    kind = TipKind.BadPhotos,
                    payloadBytes = p.bytesOf(p.badQuality)
                )
            }
            if (p.screenshots.isNotEmpty()) {
                candidates += "Photos and video" to Tip(
                    id = "screenshots",
                    index = 0,
                    title = "${p.screenshots.size} screenshots found",
                    subtitle = "${formatBytes(p.bytesOf(p.screenshots))} can be cleaned",
                    kind = TipKind.Screenshots,
                    payloadBytes = p.bytesOf(p.screenshots)
                )
            }
            val dupes = p.similarFlat
            if (dupes.isNotEmpty()) {
                candidates += "Photos and video" to Tip(
                    id = "similar_photos",
                    index = 0,
                    title = "${dupes.size} similar photos",
                    subtitle = "Keep the best, drop the rest",
                    kind = TipKind.SimilarPhotos,
                    payloadBytes = p.bytesOf(dupes)
                )
            }
            if (p.old.isNotEmpty()) {
                candidates += "Photos and video" to Tip(
                    id = "old_photos",
                    index = 0,
                    title = "${p.old.size} old photos",
                    subtitle = "Added more than a year ago",
                    kind = TipKind.OldPhotos,
                    payloadBytes = p.bytesOf(p.old)
                )
            }
            if (p.optimizable.isNotEmpty()) {
                candidates += "Photos and video" to Tip(
                    id = "optimizable",
                    index = 0,
                    title = "${p.optimizable.size} optimizable images",
                    subtitle = "Get ${formatBytes((p.bytesOf(p.optimizable) * 0.6).toLong())} more space",
                    kind = TipKind.OptimizableImages,
                    payloadBytes = p.bytesOf(p.optimizable),
                    premium = true
                )
            }
        }

        media?.let { m ->
            if (m.video.bytes > 0) {
                candidates += "Photos and video" to Tip(
                    id = "large_videos",
                    index = 0,
                    title = "Large videos",
                    subtitle = "${m.video.count} videos using ${formatBytes(m.video.bytes)}",
                    kind = TipKind.LargeVideos,
                    payloadBytes = m.video.bytes
                )
            }
            if (m.others.bytes > 0) {
                candidates += "Other files" to Tip(
                    id = "big_files",
                    index = 0,
                    title = "Big files",
                    subtitle = "Found a few big items. Take a look.",
                    kind = TipKind.BigFiles,
                    payloadBytes = m.others.bytes
                )
            }
        }

        // Sort by the user's category order, then by how much each tip can save.
        val ordered = candidates.sortedWith(
            compareBy<Pair<String, Tip>> { (group, _) ->
                priority.indexOf(group).let { if (it == -1) priority.size else it }
            }.thenByDescending { (_, tip) -> tip.payloadBytes }
        )

        return ordered.mapIndexed { index, (_, tip) -> tip.copy(index = index + 1) }
    }
}
