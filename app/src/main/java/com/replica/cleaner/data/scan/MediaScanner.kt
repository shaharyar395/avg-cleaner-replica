package com.replica.cleaner.data.scan

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.data.model.MediaBucket
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.data.model.MediaSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Reads counts and sizes straight out of MediaStore for the Storage tab. */
class MediaScanner(private val context: Context) {

    suspend fun summary(totalDeviceBytes: Long): MediaSummary = withContext(Dispatchers.IO) {
        var photos = bucket(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.SIZE)
        var video = bucket(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.SIZE)
        var audio = bucket(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.SIZE)
        var others = otherFilesBucket()

        // When MediaStore is empty (missing READ_MEDIA_* / scoped storage) but the
        // user granted All files access, count real files on disk like AVG does.
        if (Permissions.hasAllFilesAccess(context)) {
            if (photos.count == 0) {
                photos = walkBucket(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                )
            }
            if (video.count == 0) {
                video = walkBucket(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        ?.resolve("Camera"),
                    extensions = VIDEO_EXT
                )
            }
            if (audio.count == 0) {
                audio = walkBucket(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
                    extensions = AUDIO_EXT
                )
            }
            if (others.count == 0) {
                others = otherFilesBucket()
            }
        }

        MediaSummary(
            photos = photos,
            video = video,
            audio = audio,
            others = others,
            totalDeviceBytes = totalDeviceBytes
        )
    }

    suspend fun images(limit: Int = 2000): List<MediaFile> = withContext(Dispatchers.IO) {
        val fromStore = query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
            ),
            "${MediaStore.Images.Media.DATE_ADDED} DESC",
            limit
        )
        if (fromStore.isNotEmpty() || !Permissions.hasAllFilesAccess(context)) return@withContext fromStore
        walkFiles(
            listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            ),
            IMAGE_EXT,
            limit
        )
    }

    suspend fun videos(limit: Int = 1000): List<MediaFile> = withContext(Dispatchers.IO) {
        val fromStore = query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
            ),
            "${MediaStore.Video.Media.SIZE} DESC",
            limit
        )
        if (fromStore.isNotEmpty() || !Permissions.hasAllFilesAccess(context)) return@withContext fromStore
        walkFiles(
            listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.resolve("Camera")
            ),
            VIDEO_EXT,
            limit
        )
    }

    suspend fun audio(limit: Int = 1000): List<MediaFile> = withContext(Dispatchers.IO) {
        val fromStore = query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM
            ),
            "${MediaStore.Audio.Media.SIZE} DESC",
            limit
        )
        if (fromStore.isNotEmpty() || !Permissions.hasAllFilesAccess(context)) return@withContext fromStore
        walkFiles(
            listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
            ),
            AUDIO_EXT,
            limit
        )
    }

    /** Downloads, documents and archives — the "Others" row on the Storage tab. */
    suspend fun otherFiles(limit: Int = 500): List<MediaFile> = withContext(Dispatchers.IO) {
        val roots = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        )
        roots.flatMap { root ->
            root.walkTopDown().maxDepth(3).filter { it.isFile }.map { f ->
                MediaFile(
                    uri = Uri.fromFile(f),
                    name = f.name,
                    sizeBytes = f.length(),
                    dateAdded = f.lastModified() / 1000,
                    bucket = root.name
                )
            }
        }.sortedByDescending { it.sizeBytes }.take(limit)
    }

    suspend fun screenshots(): List<MediaFile> = withContext(Dispatchers.IO) {
        images(5000).filter {
            it.bucket.equals("Screenshots", true) || it.name.startsWith("Screenshot", true)
        }
    }

    /** Deletes real files. Returns the number of bytes actually reclaimed. */
    suspend fun delete(files: List<MediaFile>): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        files.forEach { file ->
            val removed = try {
                context.contentResolver.delete(file.uri, null, null) > 0
            } catch (_: Exception) {
                file.uri.path?.let { File(it).delete() } ?: false
            }
            if (removed) freed += file.sizeBytes
        }
        freed
    }

    private fun bucket(uri: Uri, sizeColumn: String): MediaBucket {
        var count = 0
        var bytes = 0L
        try {
            context.contentResolver.query(uri, arrayOf(sizeColumn), null, null, null)?.use { c ->
                val sizeIdx = c.getColumnIndexOrThrow(sizeColumn)
                while (c.moveToNext()) {
                    count++
                    bytes += c.getLong(sizeIdx).coerceAtLeast(0L)
                }
            }
        } catch (_: SecurityException) {
            // Missing READ_MEDIA_* — caller may fall back to filesystem.
        } catch (_: Exception) {
        }
        return MediaBucket(count, bytes)
    }

    private fun otherFilesBucket(): MediaBucket {
        var count = 0
        var bytes = 0L
        listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        ).forEach { root ->
            try {
                root.walkTopDown().maxDepth(3).filter { it.isFile }.forEach {
                    count++
                    bytes += it.length()
                }
            } catch (_: Exception) {
            }
        }
        return MediaBucket(count, bytes)
    }

    private fun walkBucket(
        vararg roots: File?,
        extensions: Set<String>? = IMAGE_EXT
    ): MediaBucket {
        var count = 0
        var bytes = 0L
        roots.filterNotNull().forEach { root ->
            try {
                root.walkTopDown().maxDepth(6).filter { it.isFile }.forEach { f ->
                    if (extensions == null || f.extension.lowercase() in extensions) {
                        count++
                        bytes += f.length()
                    }
                }
            } catch (_: Exception) {
            }
        }
        return MediaBucket(count, bytes)
    }

    private fun walkFiles(roots: List<File>, extensions: Set<String>, limit: Int): List<MediaFile> {
        val out = ArrayList<MediaFile>()
        roots.forEach { root ->
            try {
                root.walkTopDown().maxDepth(6).filter { it.isFile }.forEach { f ->
                    if (out.size >= limit) return@forEach
                    if (f.extension.lowercase() in extensions) {
                        out += MediaFile(
                            uri = Uri.fromFile(f),
                            name = f.name,
                            sizeBytes = f.length(),
                            dateAdded = f.lastModified() / 1000,
                            bucket = root.name
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
        return out.sortedByDescending { it.sizeBytes }
    }

    private fun query(
        collection: Uri,
        projection: Array<String>,
        sort: String,
        limit: Int
    ): List<MediaFile> {
        val out = ArrayList<MediaFile>(minOf(limit, 512))
        val cursor = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val args = Bundle().apply {
                    putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, limit)
                    putStringArray(
                        android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(sort.removeSuffix(" DESC").removeSuffix(" ASC").trim())
                    )
                    putInt(
                        android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        if (sort.endsWith("ASC", ignoreCase = true))
                            android.content.ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
                        else
                            android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }
                try {
                    context.contentResolver.query(collection, projection, args, null)
                } catch (_: Exception) {
                    context.contentResolver.query(collection, projection, null, null, sort)
                }
            } else {
                @Suppress("DEPRECATION")
                context.contentResolver.query(collection, projection, null, null, "$sort LIMIT $limit")
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }

        cursor?.use { c ->
            val idIdx = c.getColumnIndexOrThrow(projection[0])
            val nameIdx = c.getColumnIndexOrThrow(projection[1])
            val sizeIdx = c.getColumnIndexOrThrow(projection[2])
            val dateIdx = c.getColumnIndexOrThrow(projection[3])
            val bucketIdx = if (projection.size > 4) c.getColumnIndex(projection[4]) else -1
            val wIdx = if (projection.size > 5) c.getColumnIndex(projection[5]) else -1
            val hIdx = if (projection.size > 6) c.getColumnIndex(projection[6]) else -1
            while (c.moveToNext() && out.size < limit) {
                val id = c.getLong(idIdx)
                out += MediaFile(
                    uri = ContentUris.withAppendedId(collection, id),
                    name = c.getString(nameIdx) ?: "unnamed",
                    sizeBytes = c.getLong(sizeIdx).coerceAtLeast(0L),
                    dateAdded = c.getLong(dateIdx),
                    bucket = if (bucketIdx >= 0) c.getString(bucketIdx) ?: "" else "",
                    width = if (wIdx >= 0) c.getInt(wIdx) else 0,
                    height = if (hIdx >= 0) c.getInt(hIdx) else 0
                )
            }
        }
        return out
    }

    companion object {
        private val IMAGE_EXT = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "bmp")
        private val VIDEO_EXT = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v")
        private val AUDIO_EXT = setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "wma", "opus")
    }
}
