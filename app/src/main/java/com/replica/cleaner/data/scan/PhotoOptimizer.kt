package com.replica.cleaner.data.scan

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.replica.cleaner.data.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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
        quality: Int = 58,
        maxEdge: Int = 1600,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Outcome = withContext(Dispatchers.IO) {
        var freed = 0L
        var ok = 0
        var failed = 0
        val total = files.size.coerceAtLeast(1)
        files.forEachIndexed { index, file ->
            onProgress(index / total.toFloat(), file.name)
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
        openStream(file)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return 0L

        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        val sample = Integer.highestOneBit((longest / maxEdge).coerceAtLeast(1))
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample.coerceAtLeast(1)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = openStream(file)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return 0L

        val scaled = scaleDown(decoded, maxEdge)
        if (scaled != decoded) decoded.recycle()

        fun jpegBytes(q: Int): ByteArray {
            val bytes = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, q.coerceIn(35, 92), bytes)
            return bytes.toByteArray()
        }

        var data = jpegBytes(quality)
        if (data.size >= originalSize) data = jpegBytes(48)
        if (data.size >= originalSize) {
            val tighter = scaleDown(scaled, 1280)
            if (tighter != scaled) {
                scaled.recycle()
                data = ByteArrayOutputStream().also {
                    tighter.compress(Bitmap.CompressFormat.JPEG, 45, it)
                }.toByteArray()
                tighter.recycle()
            } else {
                scaled.recycle()
            }
        } else {
            scaled.recycle()
        }
        if (data.isEmpty() || data.size >= originalSize) return 0L

        val path = resolvePath(file.uri)
        if (!path.isNullOrBlank()) {
            val target = File(path)
            if (target.exists() && target.canWrite()) {
                val tmp = File(target.parentFile, ".opt_${target.name}.tmp")
                FileOutputStream(tmp).use { it.write(data) }
                if (!tmp.renameTo(target)) {
                    target.delete()
                    tmp.renameTo(target)
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(target.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
                return originalSize - target.length()
            }
        }

        val wroteInPlace = try {
            resolver.openOutputStream(file.uri, "rwt")?.use { out ->
                out.write(data)
                out.flush()
            } != null
        } catch (_: Exception) {
            try {
                resolver.openOutputStream(file.uri)?.use { out ->
                    out.write(data)
                    out.flush()
                } != null
            } catch (_: Exception) {
                false
            }
        }
        if (wroteInPlace) return originalSize - data.size

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
            runCatching { resolver.delete(file.uri, null, null) }
            originalSize - data.size
        } catch (_: Exception) {
            runCatching { resolver.delete(target, null, null) }
            0L
        }
    }

    private fun openStream(file: MediaFile) =
        context.contentResolver.openInputStream(file.uri)
            ?: file.uri.path?.let { File(it).takeIf { f -> f.exists() }?.inputStream() }

    private fun resolvePath(uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()?.takeIf { !it.isNullOrBlank() && File(it).exists() }
            ?: runCatching {
                if (uri.authority?.contains("media") == true) {
                    val id = ContentUris.parseId(uri)
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.MediaColumns.DATA),
                        "${MediaStore.Images.Media._ID}=?",
                        arrayOf(id.toString()),
                        null
                    )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
                } else null
            }.getOrNull()
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
