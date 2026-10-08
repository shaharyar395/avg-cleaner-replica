package com.replica.cleaner.data.scan

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.replica.cleaner.data.model.MediaFile
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** Re-encodes large videos at 720p / ~1.5 Mbps so Video Optimizer frees real space. */
class VideoOptimizer(private val context: Context) {

    data class Outcome(
        val freedBytes: Long,
        val optimized: Int,
        val failed: Int
    )

    suspend fun optimize(
        files: List<MediaFile>,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Outcome = withContext(Dispatchers.Main) {
        var freed = 0L
        var ok = 0
        var failed = 0
        val total = files.size.coerceAtLeast(1)
        files.forEachIndexed { index, file ->
            onProgress(index / total.toFloat(), file.name)
            val result = runCatching { transcode(file) }.getOrNull() ?: 0L
            if (result > 0) {
                freed += result
                ok++
            } else {
                failed++
            }
        }
        onProgress(1f, "")
        Outcome(freed, ok, failed)
    }

    private suspend fun transcode(file: MediaFile): Long {
        val originalSize = file.sizeBytes.coerceAtLeast(1L)
        val outFile = File(context.cacheDir, "opt_${System.currentTimeMillis()}.mp4")
        val ok = runTransformer(file.uri, outFile)
        if (!ok || !outFile.exists() || outFile.length() <= 0L) {
            outFile.delete()
            return 0L
        }
        if (outFile.length() >= originalSize) {
            outFile.delete()
            return 0L
        }
        val replaced = withContext(Dispatchers.IO) {
            replaceOriginal(file, outFile, originalSize)
        }
        outFile.delete()
        return replaced
    }

    private suspend fun runTransformer(uri: Uri, outFile: File): Boolean =
        suspendCancellableCoroutine { cont ->
            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(
                    VideoEncoderSettings.Builder()
                        .setBitrate(1_500_000)
                        .build()
                )
                .build()
            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setEncoderFactory(encoderFactory)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        if (cont.isActive) cont.resume(false)
                    }
                })
                .build()
            val edited = EditedMediaItem.Builder(MediaItem.fromUri(uri))
                .setEffects(
                    Effects(
                        emptyList(),
                        listOf(Presentation.createForHeight(720))
                    )
                )
                .build()
            val composition = Composition.Builder(
                EditedMediaItemSequence(edited)
            ).build()
            transformer.start(composition, outFile.absolutePath)
            cont.invokeOnCancellation { transformer.cancel() }
        }

    private fun replaceOriginal(file: MediaFile, compressed: File, originalSize: Long): Long {
        val resolver = context.contentResolver
        val path = resolvePath(file.uri)
        if (!path.isNullOrBlank()) {
            val target = File(path)
            if (target.parentFile?.canWrite() == true || target.canWrite()) {
                val tmp = File(target.parentFile, ".opt_${target.name}.tmp")
                compressed.copyTo(tmp, overwrite = true)
                if (target.exists()) target.delete()
                tmp.renameTo(target)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(target.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                return (originalSize - target.length()).coerceAtLeast(1L)
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, optimizedName(file.name))
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CleanerOptimized")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val inserted = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: return 0L
        return try {
            resolver.openOutputStream(inserted)?.use { out ->
                FileInputStream(compressed).use { it.copyTo(out) }
            } ?: run {
                resolver.delete(inserted, null, null)
                return 0L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(inserted, values, null, null)
            }
            runCatching { resolver.delete(file.uri, null, null) }
            originalSize - compressed.length()
        } catch (_: Exception) {
            runCatching { resolver.delete(inserted, null, null) }
            0L
        }
    }

    private fun resolvePath(uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull()?.takeIf { !it.isNullOrBlank() && File(it).exists() }
    }

    private fun optimizedName(name: String): String {
        val base = name.substringBeforeLast('.', name)
        return "${base}_opt.mp4"
    }
}
