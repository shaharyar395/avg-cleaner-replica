package com.replica.cleaner.data.scan

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.replica.cleaner.data.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Re-encodes photos as JPEG at a lower quality / capped long edge so the
 * Photo Optimizer screen frees real bytes on device.
 */
class PhotoOptimizer(private val context: Context) {

    data class Outcome(
        val freedBytes: Long,
        val optimized: Int,
        val failed: Int
    )

    suspend fun optimize(
        files: List<MediaFile>,
        quality: Int = 72,
        maxEdge: Int = 1920,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Outcome = withContext(Dispatchers.IO) {
        var freed = 0L
        var ok = 0
        var failed = 0
        files.forEachIndexed { index, file ->
            onProgress(index / files.size.coerceAtLeast(1).toFloat(), file.name)
            val result = runCatching { recompress(file, quality, maxEdge) }.getOrNull()
            if (result != null && result > 0) {
                freed += result
                ok++
            } else {
                failed++
            }
        }
        onProgress(1f, "")
        Outcome(freed, ok, failed)
    }

    /** Returns bytes freed, or 0 if the rewrite was not smaller / failed. */
    private fun recompress(file: MediaFile, quality: Int, maxEdge: Int): Long {
        val resolver = context.contentResolver
        val originalSize = file.sizeBytes.coerceAtLeast(1L)

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return 0L

        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        val sample = Integer.highestOneBit((longest / maxEdge).coerceAtLeast(1))
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample.coerceAtLeast(1)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = resolver.openInputStream(file.uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return 0L

        val scaled = scaleDown(decoded, maxEdge)
        if (scaled != decoded) decoded.recycle()

        val bytes = ByteArrayOutputStream()
        val compressed = scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(40, 95), bytes)
        scaled.recycle()
        if (!compressed) return 0L
        val data = bytes.toByteArray()
        if (data.size >= originalSize) return 0L

        // Prefer in-place overwrite when the provider allows it.
        val wroteInPlace = try {
            resolver.openOutputStream(file.uri, "wt")?.use { out ->
                out.write(data)
                out.flush()
            } != null
        } catch (_: Exception) {
            false
        }

        if (wroteInPlace) return originalSize - data.size

        // Fallback: insert a new optimized image and delete the original.
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, optimizedName(file.name))
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CleanerOptimized")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return 0L
        return try {
            resolver.openOutputStream(target)?.use { it.write(data) }
                ?: run {
                    resolver.delete(target, null, null)
                    return 0L
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(target, values, null, null)
            }
            resolver.delete(file.uri, null, null)
            originalSize - data.size
        } catch (_: Exception) {
            runCatching { resolver.delete(target, null, null) }
            0L
        }
    }

    private fun scaleDown(src: Bitmap, maxEdge: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxEdge) return src
        val scale = maxEdge.toFloat() / longest
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }

    private fun optimizedName(name: String): String {
        val base = name.substringBeforeLast('.', name)
        return "${base}_opt.jpg"
    }
}
