package com.replica.cleaner.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.data.model.PhotoAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

/**
 * The "Photo analysis" panel on the Media Overview screen — Similar, Bad quality,
 * Sensitive, Old.
 *
 * Everything is computed on device from a 32x32 grayscale thumbnail, so a few
 * hundred photos analyse in a couple of seconds and nothing leaves the phone:
 *
 *  - Similar    : difference hash, grouped by Hamming distance
 *  - Bad quality: variance of the Laplacian (blur) plus mean luminance
 *  - Old        : added more than a year ago
 *  - Optimizable: large pixel dimensions with a large file, worth re-encoding
 */
class PhotoAnalyzer(private val context: Context) {

    companion object {
        private const val HASH_SIZE = 8          // 8x9 samples -> 64-bit dHash
        private const val ANALYSIS_SIZE = 32
        private const val SIMILAR_DISTANCE = 6   // bits differing, out of 64
        private const val BLUR_THRESHOLD = 90.0  // Laplacian variance
        private const val DARK_THRESHOLD = 38.0
        private const val BRIGHT_THRESHOLD = 225.0
        private const val OPTIMIZABLE_MIN_BYTES = 1_500_000L
        private const val OPTIMIZABLE_MIN_EDGE = 1800
        private const val ONE_YEAR_SECONDS = 365L * 24 * 3600
    }

    suspend fun analyze(
        photos: List<MediaFile>,
        maxToDecode: Int = 400,
        onProgress: (Float) -> Unit = {}
    ): PhotoAnalysis = withContext(Dispatchers.IO) {
        val nowSeconds = System.currentTimeMillis() / 1000

        val old = photos.filter { it.dateAdded > 0 && nowSeconds - it.dateAdded > ONE_YEAR_SECONDS }
        val screenshots = photos.filter {
            it.bucket.equals("Screenshots", true) || it.name.startsWith("Screenshot", true)
        }
        val optimizable = photos.filter {
            it.sizeBytes >= OPTIMIZABLE_MIN_BYTES &&
                (it.width >= OPTIMIZABLE_MIN_EDGE || it.height >= OPTIMIZABLE_MIN_EDGE)
        }

        // Decoding is the expensive part, so cap it at the largest N photos.
        val toDecode = photos.sortedByDescending { it.sizeBytes }.take(maxToDecode)
        val hashes = LinkedHashMap<MediaFile, Long>()
        val bad = mutableListOf<MediaFile>()

        toDecode.forEachIndexed { index, file ->
            coroutineContext.ensureActive()
            onProgress(index / toDecode.size.coerceAtLeast(1).toFloat())
            val gray = grayscale(file) ?: return@forEachIndexed
            hashes[file] = dHash(gray)
            val variance = laplacianVariance(gray)
            val mean = gray.average()
            if (variance < BLUR_THRESHOLD || mean < DARK_THRESHOLD || mean > BRIGHT_THRESHOLD) {
                bad += file
            }
        }
        onProgress(1f)

        PhotoAnalysis(
            similar = groupSimilar(hashes),
            badQuality = bad,
            old = old,
            screenshots = screenshots,
            optimizable = optimizable
        )
    }

    /** Greedy grouping: good enough here and avoids an O(n²) clustering pass. */
    private fun groupSimilar(hashes: Map<MediaFile, Long>): List<List<MediaFile>> {
        val entries = hashes.entries.toList()
        val used = BooleanArray(entries.size)
        val groups = mutableListOf<List<MediaFile>>()
        for (i in entries.indices) {
            if (used[i]) continue
            val group = mutableListOf(entries[i].key)
            for (j in i + 1 until entries.size) {
                if (used[j]) continue
                if (hamming(entries[i].value, entries[j].value) <= SIMILAR_DISTANCE) {
                    group += entries[j].key
                    used[j] = true
                }
            }
            used[i] = true
            if (group.size > 1) groups += group
        }
        return groups
    }

    private fun hamming(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    /** Decodes to a small grayscale array; null when the file cannot be read. */
    private fun grayscale(file: MediaFile): DoubleArray? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(file.uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0) return null

            val sample = maxOf(1, minOf(bounds.outWidth, bounds.outHeight) / ANALYSIS_SIZE)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = Integer.highestOneBit(sample.coerceAtLeast(1))
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(file.uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            val scaled = Bitmap.createScaledBitmap(decoded, ANALYSIS_SIZE, ANALYSIS_SIZE, true)
            if (scaled != decoded) decoded.recycle()

            val pixels = IntArray(ANALYSIS_SIZE * ANALYSIS_SIZE)
            scaled.getPixels(pixels, 0, ANALYSIS_SIZE, 0, 0, ANALYSIS_SIZE, ANALYSIS_SIZE)
            scaled.recycle()

            DoubleArray(pixels.size) { i ->
                val p = pixels[i]
                0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)
            }
        } catch (_: Throwable) {
            null
        }
    }

    /** Difference hash over an 8x8 window sampled from the 32x32 grayscale. */
    private fun dHash(gray: DoubleArray): Long {
        val step = ANALYSIS_SIZE / HASH_SIZE
        var hash = 0L
        var bit = 0
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                val left = gray[(y * step) * ANALYSIS_SIZE + (x * step)]
                val rightX = ((x + 1) * step).coerceAtMost(ANALYSIS_SIZE - 1)
                val right = gray[(y * step) * ANALYSIS_SIZE + rightX]
                if (left > right) hash = hash or (1L shl bit)
                bit++
            }
        }
        return hash
    }

    /** Variance of a 3x3 Laplacian — the standard cheap blur score. */
    private fun laplacianVariance(gray: DoubleArray): Double {
        val n = ANALYSIS_SIZE
        val values = ArrayList<Double>((n - 2) * (n - 2))
        for (y in 1 until n - 1) {
            for (x in 1 until n - 1) {
                val c = gray[y * n + x]
                val lap = 4 * c -
                    gray[(y - 1) * n + x] -
                    gray[(y + 1) * n + x] -
                    gray[y * n + (x - 1)] -
                    gray[y * n + (x + 1)]
                values += abs(lap)
            }
        }
        if (values.isEmpty()) return Double.MAX_VALUE
        val mean = values.average()
        return values.sumOf { (it - mean) * (it - mean) } / values.size
    }
}
