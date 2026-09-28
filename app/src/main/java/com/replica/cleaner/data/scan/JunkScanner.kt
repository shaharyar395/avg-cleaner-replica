package com.replica.cleaner.data.scan

import android.app.usage.StorageStatsManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.data.model.CategoryGroup
import com.replica.cleaner.data.model.JunkCategory
import com.replica.cleaner.data.model.JunkItem
import com.replica.cleaner.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * Builds the Quick Clean list from real on-device files.
 *
 * Uses MediaStore + public folders (works without full Android/data access on
 * Android 11+) and falls back to a deep File walk when MANAGE_EXTERNAL_STORAGE
 * is granted.
 */
class JunkScanner(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private val resolver get() = context.contentResolver

    companion object {
        private const val TAG = "JunkScanner"
        private const val MAX_DEPTH = 8
        private const val MAX_WALK_FILES = 30_000
        private const val MAX_SCAN_MS = 16_000L
        private const val MAX_DIR_SIZE_NODES = 50_000

        const val HIDDEN_CACHES = "hidden_caches"
        const val VISIBLE_CACHES = "visible_caches"
        const val BROWSER_DATA = "browser_data"
        const val RESIDUAL_FILES = "residual_files"
        const val INSTALLED_APKS = "installed_apks"
        const val AD_CACHES = "ad_caches"
        const val THUMBNAILS = "thumbnails"
        const val EMPTY_FOLDERS = "empty_folders"
        const val DOWNLOADS = "downloads"
        const val TEMP_FILES = "temp_files"
        const val APP_DATA = "app_data"

        private val AD_CACHE_HINTS = listOf(
            "ads", "admob", "adcache", ".ad", "applovin", "unityads",
            "mopub", "vungle", "ironsource", "chartboost", "fyber"
        )
        private val BROWSER_PACKAGES = listOf(
            "com.android.chrome", "org.mozilla.firefox", "com.opera.browser",
            "com.brave.browser", "com.microsoft.emmx", "com.sec.android.app.sbrowser",
            "com.UCMobile.intl", "com.android.browser", "com.miui.browser"
        )
        private val TEMP_EXTENSIONS = setOf(
            "tmp", "temp", "log", "bak", "old", "part", "crdownload", "download"
        )
        private val CACHE_DIR_NAMES = setOf(
            "cache", ".cache", "caches", ".caches", "tmp", "temp", ".temp",
            "tmp_files", "log", "logs", "code_cache", ".nomedia_cache"
        )
        private val THUMB_DIR_NAMES = setOf(".thumbnails", "thumbnails", ".thumb", "thumbs")
    }

    suspend fun scan(
        includePhotosAnalysis: Boolean = false,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ScanResult = withContext(Dispatchers.IO) {
        val scanStartedAt = System.currentTimeMillis()
        fun timedOut(): Boolean = System.currentTimeMillis() - scanStartedAt > MAX_SCAN_MS

        val root = Environment.getExternalStorageDirectory()
        val hasFiles = Permissions.hasAllFilesAccess(context)

        val categories = mutableListOf<JunkCategory>()
        val visible = mutableListOf<JunkItem>()
        val ads = mutableListOf<JunkItem>()
        val residual = mutableListOf<JunkItem>()
        val apks = mutableListOf<JunkItem>()
        val thumbs = mutableListOf<JunkItem>()
        val empties = mutableListOf<JunkItem>()
        val temps = mutableListOf<JunkItem>()
        val downloads = mutableListOf<JunkItem>()

        onProgress(0.05f, "Reading app caches")
        categories += hiddenCaches()

        // Always include our own caches — we can delete these without special access.
        onProgress(0.12f, "Reading Cleaner cache")
        addOwnCaches(visible)

        onProgress(0.18f, "Scanning visible caches")
        scanVisibleCacheTargets(root, visible, ads)

        onProgress(0.28f, "Scanning residual leftovers")
        scanResidualTargets(root, residual)

        onProgress(0.38f, "Scanning thumbnails")
        thumbs += collectThumbnails()

        onProgress(0.48f, "Scanning MediaStore")
        downloads += mediaStoreDownloads()
        apks += mediaStoreApks()

        onProgress(0.55f, "Scanning Downloads & Documents")
        scanPublicRoots(visible, ads, apks, thumbs, empties, temps, downloads)

        if (!timedOut() && hasFiles && root.isDirectory) {
            onProgress(0.62f, "Scanning shared storage")
            tryScanAndroidData(root, visible, ads, residual)

            if (!timedOut()) {
                walk(root, MAX_DEPTH, deadlineMs = scanStartedAt + MAX_SCAN_MS) { file, depth ->
                    coroutineContext.ensureActive()
                    classifyFile(file, depth, visible, ads, apks, thumbs, empties, temps, downloads)
                }
            }
        } else if (!timedOut()) {
            onProgress(0.62f, "Scanning public folders")
            publicRoots().forEach { publicRoot ->
                if (timedOut()) return@forEach
                if (publicRoot.isDirectory) {
                    walk(publicRoot, 5, deadlineMs = scanStartedAt + MAX_SCAN_MS) { file, depth ->
                        coroutineContext.ensureActive()
                        classifyFile(file, depth, visible, ads, apks, thumbs, empties, temps, downloads)
                    }
                }
            }
        }

        if (!timedOut()) {
            onProgress(0.82f, "Finding empty folders")
            findEmptyFolders(empties, deadlineMs = scanStartedAt + MAX_SCAN_MS)
        }

        onProgress(0.90f, "Checking browser data")
        categories += browserData()

        fun dedupe(list: List<JunkItem>): List<JunkItem> =
            list.distinctBy { it.path ?: it.uri?.toString() ?: it.id }
                .filter { it.sizeBytes > 0L || it.categoryId == EMPTY_FOLDERS }
                .sortedByDescending { it.sizeBytes }

        val visibleMerged = dedupe(visible + ads + temps)
        val residualItems = dedupe(residual)
        val apkItems = dedupe(apks)
        val downloadItems = dedupe(downloads)
        val thumbItems = dedupe(thumbs)

        onProgress(1f, "Done")
        val ordered = listOf(
            categories.first { it.id == HIDDEN_CACHES },
            JunkCategory(
                VISIBLE_CACHES, "Visible caches",
                "Temporary files that can be recreated.",
                CategoryGroup.Unneeded, items = visibleMerged
            ),
            categories.first { it.id == BROWSER_DATA },
            JunkCategory(
                RESIDUAL_FILES, "Residual files",
                "Leftover files after you uninstall apps from your device.",
                CategoryGroup.Unneeded,
                locked = false,
                items = residualItems.take(200)
            ),
            JunkCategory(
                INSTALLED_APKS, "Installed APKs",
                "Leftover installation files after new apps are installed.",
                CategoryGroup.Unneeded, items = apkItems.take(300)
            ),
            JunkCategory(
                THUMBNAILS, "Thumbnails",
                "Small preview versions of your photos.",
                CategoryGroup.Unneeded,
                locked = false,
                items = thumbItems.take(800)
            ),
            JunkCategory(
                EMPTY_FOLDERS, "Empty folders",
                "Folders with nothing inside.",
                CategoryGroup.Unneeded, items = dedupe(empties).take(200)
            ),
            JunkCategory(
                DOWNLOADS, "Downloads",
                "Files you downloaded and may no longer need.",
                CategoryGroup.Review,
                items = downloadItems.take(500)
            )
        )
        ScanResult(categories = ordered, scannedAt = System.currentTimeMillis())
    }

    private val CATEGORY_ORDER = listOf(
        HIDDEN_CACHES, VISIBLE_CACHES, BROWSER_DATA, RESIDUAL_FILES, INSTALLED_APKS, THUMBNAILS,
        EMPTY_FOLDERS, DOWNLOADS
    )

    /**
     * App-generated media/data folders users should review before deleting
     * (WhatsApp, Telegram, Android/media, etc.).
     */
    private fun scanAppData(): List<JunkItem> {
        val out = mutableListOf<JunkItem>()
        val root = Environment.getExternalStorageDirectory()
        val candidates = listOfNotNull(
            File(root, "WhatsApp"),
            File(root, "WhatsApp/Media"),
            File(root, "Android/media"),
            File(root, "Telegram"),
            File(root, "Telegram/Telegram Images"),
            File(root, "Telegram/Telegram Video"),
            File(root, "Telegram/Telegram Documents"),
            File(root, "DCIM/.shared"),
            File(root, "Pictures/Telegram"),
            File(root, "Movies/Telegram"),
            File(root, "MIUI/Gallery/cloud"),
            File(root, "tencent"),
            File(root, "ByteDance"),
            File(root, "UCDownloads")
        )
        candidates.forEach { base ->
            if (!base.exists()) return@forEach
            collectAppDataFiles(base, out, maxFiles = 250)
        }
        return out.sortedByDescending { it.sizeBytes }
    }

    private fun collectAppDataFiles(dir: File, out: MutableList<JunkItem>, maxFiles: Int, depth: Int = 0) {
        if (out.size >= maxFiles || depth > 6) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (out.size >= maxFiles) return
            when {
                child.isFile && child.length() > 8_192L -> {
                    out += JunkItem(
                        id = "appdata:${child.absolutePath}",
                        label = child.name,
                        path = child.absolutePath,
                        uri = null,
                        sizeBytes = child.length(),
                        categoryId = APP_DATA,
                        lastModified = child.lastModified()
                    )
                }
                child.isDirectory && !isSymlink(child) -> {
                    collectAppDataFiles(child, out, maxFiles, depth + 1)
                }
            }
        }
    }

    /** Walk common public trees for truly empty user folders. */
    private fun findEmptyFolders(out: MutableList<JunkItem>, deadlineMs: Long = Long.MAX_VALUE) {
        val roots = publicRoots() + listOfNotNull(Environment.getExternalStorageDirectory())
        roots.forEach { root ->
            if (System.currentTimeMillis() > deadlineMs) return
            if (!root.isDirectory) return@forEach
            walk(root, 3, deadlineMs = deadlineMs) { file, depth ->
                if (file.isDirectory && depth > 0 && isRemovableEmptyDir(file)) {
                    val id = "empty:${file.absolutePath}"
                    if (out.none { it.id == id }) {
                        out += JunkItem(
                            id = id,
                            label = file.name,
                            path = file.absolutePath,
                            uri = null,
                            sizeBytes = 4096L,
                            categoryId = EMPTY_FOLDERS,
                            lastModified = file.lastModified()
                        )
                    }
                }
            }
        }
    }

    private fun publicRoots(): List<File> = listOfNotNull(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        File(Environment.getExternalStorageDirectory(), "WhatsApp"),
        File(Environment.getExternalStorageDirectory(), "Telegram"),
        File(Environment.getExternalStorageDirectory(), "Download"),
        File(Environment.getExternalStorageDirectory(), ".thumbnails"),
        context.getExternalFilesDir(null)?.parentFile // /Android/data/<us>/ — sibling caches
    )

    private fun addOwnCaches(visible: MutableList<JunkItem>) {
        listOfNotNull(context.cacheDir, context.externalCacheDir, context.codeCacheDir).forEach { dir ->
            val size = dirSize(dir)
            if (size > 0) {
                visible += JunkItem(
                    id = "owncache:${dir.absolutePath}",
                    label = "AVG Cleaner+ cache",
                    path = dir.absolutePath,
                    uri = null,
                    sizeBytes = size,
                    categoryId = VISIBLE_CACHES,
                    lastModified = dir.lastModified()
                )
            }
        }
        context.externalCacheDirs?.forEach { dir ->
            if (dir == null) return@forEach
            val size = dirSize(dir)
            if (size <= 0) return@forEach
            val id = "owncache:${dir.absolutePath}"
            if (visible.none { it.id == id }) {
                visible += JunkItem(
                    id = id,
                    label = "App cache (${dir.name})",
                    path = dir.absolutePath,
                    uri = null,
                    sizeBytes = size,
                    categoryId = VISIBLE_CACHES,
                    lastModified = dir.lastModified()
                )
            }
        }
    }

    /**
     * Visible caches: open known per-app cache paths (works even when Android/data
     * cannot be listed on Android 11+) plus common public cache folders.
     */
    private fun scanVisibleCacheTargets(
        root: File,
        visible: MutableList<JunkItem>,
        ads: MutableList<JunkItem>
    ) {
        val installed = installedApplications()
        // Direct path open — listing Android/data is often blocked, but known pkg paths work.
        installed.take(350).forEach { app ->
            val pkg = app.packageName
            val label = runCatching { pm.getApplicationLabel(app).toString() }.getOrDefault(pkg)
            listOf(
                File(root, "Android/data/$pkg/cache"),
                File(root, "Android/data/$pkg/code_cache")
            ).forEach { cache ->
                if (!cache.isDirectory) return@forEach
                val size = dirSize(cache)
                if (size <= 8_192L) return@forEach
                val id = "cache:${cache.absolutePath}"
                if (visible.any { it.id == id } || ads.any { it.id == id }) return@forEach
                val isAd = AD_CACHE_HINTS.any { pkg.lowercase().contains(it) || label.lowercase().contains(it) }
                val item = JunkItem(
                    id = id,
                    label = "$label cache",
                    path = cache.absolutePath,
                    uri = null,
                    sizeBytes = size,
                    categoryId = if (isAd) AD_CACHES else VISIBLE_CACHES,
                    lastModified = cache.lastModified()
                )
                if (isAd) ads += item else visible += item
            }
        }

        val publicCacheHints = listOf(
            File(root, ".cache"),
            File(root, "cache"),
            File(root, "Cache"),
            File(root, "temp"),
            File(root, "tmp"),
            File(root, "LOST.DIR"),
            File(root, "WhatsApp/Databases"),
            File(root, "Telegram/Telegram Documents/.cache"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ".cache"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".cache"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".cache")
        )
        publicCacheHints.forEach { dir ->
            if (dir.isDirectory) addCacheTree(dir, visible, ads, maxEntries = 40)
        }

        // Shallow walk for named cache folders under public roots.
        publicRoots().forEach { pub ->
            if (!pub.isDirectory) return@forEach
            walk(pub, 3, deadlineMs = System.currentTimeMillis() + 2_000L) { file, _ ->
                if (file.isDirectory && CACHE_DIR_NAMES.contains(file.name.lowercase())) {
                    addCacheTree(file, visible, ads, maxEntries = 20)
                }
            }
        }
    }

    private fun addCacheTree(
        dir: File,
        visible: MutableList<JunkItem>,
        ads: MutableList<JunkItem>,
        maxEntries: Int
    ) {
        if (!dir.exists()) return
        val children = dir.listFiles() ?: run {
            // Directory exists but listing failed — still count whole dir if readable size.
            val size = dirSize(dir)
            if (size > 8_192L) {
                val id = "cache:${dir.absolutePath}"
                if (visible.none { it.id == id } && ads.none { it.id == id }) {
                    visible += JunkItem(
                        id = id,
                        label = dir.name,
                        path = dir.absolutePath,
                        uri = null,
                        sizeBytes = size,
                        categoryId = VISIBLE_CACHES,
                        lastModified = dir.lastModified()
                    )
                }
            }
            return
        }
        var n = 0
        for (entry in children) {
            if (n >= maxEntries) break
            val size = if (entry.isDirectory) dirSize(entry) else entry.length()
            if (size <= 0L) continue
            val isAd = AD_CACHE_HINTS.any { entry.name.lowercase().contains(it) }
            val id = "cache:${entry.absolutePath}"
            if (visible.any { it.id == id } || ads.any { it.id == id }) continue
            val item = JunkItem(
                id = id,
                label = entry.name,
                path = entry.absolutePath,
                uri = null,
                sizeBytes = size,
                categoryId = if (isAd) AD_CACHES else VISIBLE_CACHES,
                lastModified = entry.lastModified()
            )
            if (isAd) ads += item else visible += item
            n++
        }
    }

    /**
     * Residual leftovers from uninstalled apps — Android/data|obb when listable,
     * top-level package-named folders, and MediaStore paths under Android/data.
     */
    private fun scanResidualTargets(root: File, residual: MutableList<JunkItem>) {
        val installed = installedPackageNames()

        fun addResidual(dir: File, label: String = dir.name) {
            if (!dir.isDirectory) return
            val size = measurableDirBytes(dir)
            if (size <= 0L) return
            val id = "residual:${dir.absolutePath}"
            if (residual.any { it.id == id }) return
            residual += JunkItem(
                id = id,
                label = label,
                path = dir.absolutePath,
                uri = null,
                sizeBytes = size,
                categoryId = RESIDUAL_FILES,
                lastModified = dir.lastModified()
            )
        }

        listOf(File(root, "Android/data"), File(root, "Android/obb"), File(root, "Android/media")).forEach { base ->
            val children = base.listFiles()
            if (children != null) {
                children.forEach { pkgDir ->
                    if (!pkgDir.isDirectory) return@forEach
                    if (pkgDir.name in installed) return@forEach
                    if (!pkgDir.name.contains('.')) return@forEach
                    addResidual(pkgDir)
                }
            }
        }

        // Top-level folders that look like leftover package dirs / app dumps.
        root.listFiles().orEmpty().forEach { child ->
            if (!child.isDirectory) return@forEach
            val name = child.name
            val looksLikePkg = name.contains('.') &&
                name.all { it.isLetterOrDigit() || it == '.' || it == '_' }
            if (!looksLikePkg) return@forEach
            if (name in installed) return@forEach
            if (name.equals("Android", true) || name.equals("WhatsApp", true)) return@forEach
            addResidual(child)
        }

        // Common leftover folders after uninstall / failed installs.
        listOf(
            File(root, ".leftover"),
            File(root, "backups"),
            File(root, "Backup"),
            File(root, "app_backup"),
            File(root, "AppBackup"),
            File(root, "tencent/MicroMsg/WebviewCache"),
            File(root, "UCDownloads"),
            File(root, "browser")
        ).forEach { addResidual(it) }

        // MediaStore may still index files left under Android/data|obb|media after uninstall.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val seenPkgs = mutableSetOf<String>()
            listOf("Android/data/%", "Android/obb/%", "Android/media/%").forEach { pattern ->
                try {
                    resolver.query(
                        MediaStore.Files.getContentUri("external"),
                        arrayOf(
                            MediaStore.MediaColumns._ID,
                            MediaStore.MediaColumns.SIZE,
                            MediaStore.MediaColumns.DATE_MODIFIED,
                            MediaStore.MediaColumns.RELATIVE_PATH
                        ),
                        "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?",
                        arrayOf(pattern),
                        "${MediaStore.MediaColumns.SIZE} DESC"
                    )?.use { cursor ->
                        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                        val modCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                        val relCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                        var n = 0
                        while (cursor.moveToNext() && n++ < 500) {
                            val rel = cursor.getString(relCol) ?: continue
                            val parts = rel.trim('/').split('/')
                            // Android / data|obb|media / <package> / ...
                            if (parts.size < 3) continue
                            val pkg = parts[2]
                            if (pkg.isBlank() || pkg in installed || !pkg.contains('.')) continue
                            if (!seenPkgs.add(pkg)) continue
                            val baseKind = parts[1]
                            val pkgDir = File(root, "Android/$baseKind/$pkg")
                            if (!pkgDir.exists()) continue
                            val size = cursor.getLong(sizeCol)
                            val modified = cursor.getLong(modCol) * 1000
                            val bytes = measurableDirBytes(pkgDir).coerceAtLeast(size).coerceAtLeast(4096L)
                            residual += JunkItem(
                                id = "residual-ms:$baseKind:$pkg",
                                label = pkg,
                                path = pkgDir.absolutePath,
                                uri = null,
                                sizeBytes = bytes,
                                categoryId = RESIDUAL_FILES,
                                lastModified = modified
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "residual MediaStore scan failed ($pattern): ${e.message}")
                }
            }
        }
    }

    /** Bytes under a dir; if unlistable but present, return a small placeholder so it stays cleanable. */
    private fun measurableDirBytes(dir: File): Long {
        if (!dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        val sized = dirSize(dir)
        if (sized > 0L) return sized
        val kids = dir.listFiles() ?: return 4096L
        if (kids.isEmpty()) return 0L
        var sum = 0L
        for (k in kids) sum += if (k.isFile) k.length() else measurableDirBytes(k).coerceAtMost(50_000_000L)
        return if (sum > 0L) sum else 4096L
    }

    /** Thumbnails from disk dirs + MediaStore .thumbnails paths. */
    private fun collectThumbnails(): List<JunkItem> {
        val out = mutableListOf<JunkItem>()
        val root = Environment.getExternalStorageDirectory()
        val dirs = mutableListOf(
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), ".thumbnails"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".thumbnails"),
            File(root, ".thumbnails"),
            File(root, "DCIM/.thumbnails"),
            File(root, "Pictures/.thumbnails"),
            File(root, "Movies/.thumbnails"),
            File(root, "WhatsApp/Media/.Statuses"),
            File(root, "WhatsApp/Media/.Thumbs"),
            File(root, "WhatsApp/Media/.thumbnails"),
            File(root, "Telegram/Telegram Images/.thumbnails"),
            File(root, "Android/media/.thumbnails")
        )
        listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        ).forEach { base ->
            base?.listFiles().orEmpty().forEach { child ->
                if (child.isDirectory && THUMB_DIR_NAMES.contains(child.name.lowercase())) {
                    dirs += child
                }
            }
        }

        dirs.distinctBy { it.absolutePath }.forEach { dir ->
            if (!dir.exists()) return@forEach
            val before = out.size
            collectThumbnailFiles(dir, out, maxFiles = 500)
            if (out.size == before) {
                // Dir exists but files couldn't be listed — still expose as one cleanable item.
                val size = measurableDirBytes(dir)
                if (size > 0L) {
                    out += JunkItem(
                        id = "thumbdir:${dir.absolutePath}",
                        label = dir.name,
                        path = dir.absolutePath,
                        uri = null,
                        sizeBytes = size,
                        categoryId = THUMBNAILS,
                        lastModified = dir.lastModified()
                    )
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryMediaFiles(
                MediaStore.Files.getContentUri("external"),
                selection = "(${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?)",
                selectionArgs = arrayOf(
                    "%/.thumbnails/%",
                    "%.thumbnails/%",
                    "%/Thumbnails/%",
                    "%.thumb"
                ),
                limit = 500
            ) { id, name, size, modified, uri, path ->
                val realSize = when {
                    size > 0L -> size
                    path != null -> File(path).takeIf { it.isFile }?.length() ?: 0L
                    else -> 0L
                }
                if (realSize <= 0L) return@queryMediaFiles
                val itemId = "msthumb:$id"
                if (out.none { it.id == itemId }) {
                    out += JunkItem(
                        id = itemId,
                        label = name,
                        path = path,
                        uri = uri,
                        sizeBytes = realSize,
                        categoryId = THUMBNAILS,
                        lastModified = modified
                    )
                }
            }
        }

        queryMediaFiles(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            selection = "${MediaStore.MediaColumns.SIZE} > 0 AND ${MediaStore.MediaColumns.SIZE} < ?",
            selectionArgs = arrayOf("51200"),
            limit = 200
        ) { id, name, size, modified, uri, path ->
            val lower = ((path ?: "") + name).lowercase()
            if (!lower.contains("thumb")) return@queryMediaFiles
            val itemId = "msthumb-img:$id"
            if (out.none { it.id == itemId }) {
                out += JunkItem(
                    id = itemId,
                    label = name,
                    path = path,
                    uri = uri,
                    sizeBytes = size,
                    categoryId = THUMBNAILS,
                    lastModified = modified
                )
            }
        }
        return out
    }

    private fun collectThumbnailFiles(dir: File, out: MutableList<JunkItem>, maxFiles: Int, depth: Int = 0) {
        if (!dir.isDirectory || out.size >= maxFiles || depth > 3) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (out.size >= maxFiles) return
            when {
                child.isFile && child.length() > 0L -> {
                    val id = "thumb:${child.absolutePath}"
                    if (out.none { it.id == id }) out += fileItem(child, THUMBNAILS, "thumb")
                }
                child.isDirectory && !isSymlink(child) -> {
                    if (THUMB_DIR_NAMES.contains(child.name.lowercase()) || depth == 0) {
                        collectThumbnailFiles(child, out, maxFiles, depth + 1)
                    }
                }
            }
        }
    }

    private fun tryScanAndroidData(
        root: File,
        visible: MutableList<JunkItem>,
        ads: MutableList<JunkItem>,
        residual: MutableList<JunkItem>
    ) {
        val androidData = File(root, "Android/data")
        val androidObb = File(root, "Android/obb")
        if (!androidData.isDirectory) return
        val children = androidData.listFiles() ?: return
        val installed = installedPackageNames()
        children.forEach { pkgDir ->
            if (!pkgDir.isDirectory) return@forEach
            if (pkgDir.name !in installed) {
                val size = dirSize(pkgDir)
                if (size > 0) {
                    residual += JunkItem(
                        id = "residual:${pkgDir.absolutePath}",
                        label = pkgDir.name,
                        path = pkgDir.absolutePath,
                        uri = null,
                        sizeBytes = size,
                        categoryId = RESIDUAL_FILES
                    )
                }
                return@forEach
            }
            val cache = File(pkgDir, "cache")
            if (cache.isDirectory) {
                cache.listFiles().orEmpty().forEach { entry ->
                    val size = if (entry.isDirectory) dirSize(entry) else entry.length()
                    if (size <= 0) return@forEach
                    val isAd = AD_CACHE_HINTS.any { entry.name.lowercase().contains(it) }
                    val item = JunkItem(
                        id = "cache:${entry.absolutePath}",
                        label = entry.name,
                        path = entry.absolutePath,
                        uri = null,
                        sizeBytes = size,
                        categoryId = if (isAd) AD_CACHES else VISIBLE_CACHES,
                        lastModified = entry.lastModified()
                    )
                    if (isAd) ads += item else visible += item
                }
            }
        }
        if (androidObb.isDirectory) {
            androidObb.listFiles().orEmpty().forEach { obb ->
                if (obb.isDirectory && obb.name !in installed) {
                    val size = dirSize(obb)
                    if (size > 0) {
                        residual += JunkItem(
                            id = "residual:${obb.absolutePath}",
                            label = "${obb.name} (obb)",
                            path = obb.absolutePath,
                            uri = null,
                            sizeBytes = size,
                            categoryId = RESIDUAL_FILES
                        )
                    }
                }
            }
        }
    }

    private fun scanPublicRoots(
        visible: MutableList<JunkItem>,
        ads: MutableList<JunkItem>,
        apks: MutableList<JunkItem>,
        thumbs: MutableList<JunkItem>,
        empties: MutableList<JunkItem>,
        temps: MutableList<JunkItem>,
        downloads: MutableList<JunkItem>
    ) {
        publicRoots().forEach { root ->
            if (!root.exists()) return@forEach
            // Direct children first for speed
            root.listFiles().orEmpty().forEach { child ->
                classifyFile(child, 1, visible, ads, apks, thumbs, empties, temps, downloads)
            }
        }
    }

    private fun classifyFile(
        file: File,
        depth: Int,
        visible: MutableList<JunkItem>,
        ads: MutableList<JunkItem>,
        apks: MutableList<JunkItem>,
        thumbs: MutableList<JunkItem>,
        empties: MutableList<JunkItem>,
        temps: MutableList<JunkItem>,
        downloads: MutableList<JunkItem>
    ) {
        when {
            file.isFile && file.name.endsWith(".apk", ignoreCase = true) && file.length() > 0 -> {
                apks += fileItem(file, INSTALLED_APKS, "apk")
            }
            file.isFile && isTempFile(file) && file.length() > 0 -> {
                temps += fileItem(file, TEMP_FILES, "temp")
            }
            file.isDirectory && THUMB_DIR_NAMES.contains(file.name.lowercase()) -> {
                collectThumbnailFiles(file, thumbs, maxFiles = 300)
            }
            file.isDirectory && CACHE_DIR_NAMES.contains(file.name.lowercase()) -> {
                file.listFiles().orEmpty().forEach { entry ->
                    val size = if (entry.isDirectory) dirSize(entry) else entry.length()
                    if (size <= 0) return@forEach
                    val isAd = AD_CACHE_HINTS.any { entry.name.lowercase().contains(it) }
                    val item = JunkItem(
                        id = "cache:${entry.absolutePath}",
                        label = entry.name,
                        path = entry.absolutePath,
                        uri = null,
                        sizeBytes = size,
                        categoryId = if (isAd) AD_CACHES else VISIBLE_CACHES,
                        lastModified = entry.lastModified()
                    )
                    if (isAd) ads += item else visible += item
                }
            }
            file.isDirectory && depth > 0 && isRemovableEmptyDir(file) -> {
                empties += JunkItem(
                    id = "empty:${file.absolutePath}",
                    label = file.name,
                    path = file.absolutePath,
                    uri = null,
                    sizeBytes = 4096L,
                    categoryId = EMPTY_FOLDERS,
                    lastModified = file.lastModified()
                )
            }
            file.isFile &&
                file.parentFile?.name.equals("Download", true) == true &&
                file.length() > 0 -> {
                downloads += fileItem(file, DOWNLOADS, "dl")
            }
            file.isFile &&
                file.parentFile?.name.equals("Downloads", true) == true &&
                file.length() > 0 -> {
                downloads += fileItem(file, DOWNLOADS, "dl")
            }
        }
    }

    private fun fileItem(file: File, categoryId: String, prefix: String) = JunkItem(
        id = "$prefix:${file.absolutePath}",
        label = file.name,
        path = file.absolutePath,
        uri = null,
        sizeBytes = file.length(),
        categoryId = categoryId,
        lastModified = file.lastModified()
    )

    private fun isTempFile(file: File): Boolean {
        val name = file.name.lowercase()
        val ext = name.substringAfterLast('.', "")
        return ext in TEMP_EXTENSIONS ||
            name.endsWith(".tmp") ||
            name.contains(".tmp.") ||
            name.startsWith(".") && (name.endsWith(".log") || name.endsWith(".bak"))
    }

    private fun mediaStoreDownloads(): List<JunkItem> {
        val out = mutableListOf<JunkItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        queryMediaFiles(collection, limit = 400) { id, name, size, modified, uri, path ->
            if (size > 0 && !name.endsWith(".apk", ignoreCase = true)) {
                out += JunkItem(
                    id = "msdl:$id",
                    label = name,
                    path = path,
                    uri = uri,
                    sizeBytes = size,
                    categoryId = DOWNLOADS,
                    lastModified = modified
                )
            }
        }
        // Also Files table filtered to Download path
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryMediaFiles(
                MediaStore.Files.getContentUri("external"),
                selection = "(${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?)",
                selectionArgs = arrayOf("Download/%", "Downloads/%"),
                limit = 400
            ) { id, name, size, modified, uri, path ->
                if (size > 0 &&
                    !name.endsWith(".apk", ignoreCase = true) &&
                    out.none { it.id == "msdl:$id" }
                ) {
                    out += JunkItem(
                        id = "msdl:$id",
                        label = name,
                        path = path,
                        uri = uri,
                        sizeBytes = size,
                        categoryId = DOWNLOADS,
                        lastModified = modified
                    )
                }
            }
        }
        return out
    }

    private fun mediaStoreApks(): List<JunkItem> {
        val out = mutableListOf<JunkItem>()
        queryMediaFiles(
            MediaStore.Files.getContentUri("external"),
            selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
            selectionArgs = arrayOf("%.apk"),
            limit = 200
        ) { id, name, size, modified, uri, path ->
            val realSize = when {
                size > 0L -> size
                path != null -> File(path).takeIf { it.isFile }?.length() ?: 0L
                else -> 0L
            }
            if (realSize > 0) {
                out += JunkItem(
                    id = "msapk:$id",
                    label = name,
                    path = path,
                    uri = uri,
                    sizeBytes = realSize,
                    categoryId = INSTALLED_APKS,
                    lastModified = modified
                )
            }
        }
        return out
    }

    private fun mediaStoreThumbnails(): List<JunkItem> = collectThumbnails()

    private inline fun queryMediaFiles(
        collection: Uri,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        limit: Int = 300,
        onRow: (id: Long, name: String, size: Long, modified: Long, uri: Uri, path: String?) -> Unit
    ) {
        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        @Suppress("DEPRECATION")
        val dataColName = MediaStore.MediaColumns.DATA
        // Prefer DATA when readable (all-files / older APIs) so deletes can use real paths.
        projection += dataColName
        try {
            resolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                selectionArgs,
                "${MediaStore.MediaColumns.SIZE} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val modCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                @Suppress("DEPRECATION")
                val pathCol = cursor.getColumnIndex(dataColName)
                var n = 0
                while (cursor.moveToNext() && n++ < limit) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "file"
                    val size = cursor.getLong(sizeCol)
                    val modified = cursor.getLong(modCol) * 1000
                    val uri = ContentUris.withAppendedId(collection, id)
                    val path = if (pathCol >= 0) cursor.getString(pathCol)?.takeIf { it.isNotBlank() } else null
                    onRow(id, name, size, modified, uri, path)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore query failed: ${e.message}")
            // Retry without DATA if the provider rejected it.
            if (projection.contains(dataColName)) {
                try {
                    resolver.query(
                        collection,
                        arrayOf(
                            MediaStore.MediaColumns._ID,
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            MediaStore.MediaColumns.SIZE,
                            MediaStore.MediaColumns.DATE_MODIFIED
                        ),
                        selection,
                        selectionArgs,
                        "${MediaStore.MediaColumns.SIZE} DESC"
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                        val modCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                        var n = 0
                        while (cursor.moveToNext() && n++ < limit) {
                            val id = cursor.getLong(idCol)
                            val name = cursor.getString(nameCol) ?: "file"
                            val size = cursor.getLong(sizeCol)
                            val modified = cursor.getLong(modCol) * 1000
                            val uri = ContentUris.withAppendedId(collection, id)
                            onRow(id, name, size, modified, uri, null)
                        }
                    }
                } catch (e2: Exception) {
                    Log.w(TAG, "MediaStore retry failed: ${e2.message}")
                }
            }
        }
    }

    private fun hiddenCaches(): JunkCategory {
        var total = 0L
        val items = mutableListOf<JunkItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Permissions.hasUsageAccess(context)) {
            val stats = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            val user = Process.myUserHandle()
            if (stats != null) {
                installedApplications().forEach { app ->
                    try {
                        val s = stats.queryStatsForPackage(
                            StorageManager.UUID_DEFAULT, app.packageName, user
                        )
                        if (s.cacheBytes > 0) {
                            total += s.cacheBytes
                            items += JunkItem(
                                id = "hidden:${app.packageName}",
                                label = pm.getApplicationLabel(app).toString(),
                                path = app.packageName,
                                uri = null,
                                sizeBytes = s.cacheBytes,
                                categoryId = HIDDEN_CACHES
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "cache stats unavailable for ${app.packageName}: ${e.message}")
                    }
                }
            }
        }

        val ownCache = dirSize(context.cacheDir) + dirSize(context.externalCacheDir)
        if (ownCache > 0 && items.none { it.path == context.packageName }) {
            total += ownCache
        }

        return JunkCategory(
            id = HIDDEN_CACHES,
            title = "Hidden caches",
            description = "Temporary files that are deep in your app settings and more difficult to remove.",
            group = CategoryGroup.Unneeded,
            locked = true,
            items = items.sortedByDescending { it.sizeBytes },
            reportedSizeBytes = total
        )
    }

    private fun browserData(): JunkCategory {
        var total = 0L
        val items = mutableListOf<JunkItem>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Permissions.hasUsageAccess(context)) {
            val stats = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            val user = Process.myUserHandle()
            BROWSER_PACKAGES.forEach { pkg ->
                try {
                    val s = stats?.queryStatsForPackage(StorageManager.UUID_DEFAULT, pkg, user)
                        ?: return@forEach
                    val bytes = s.dataBytes
                    if (bytes > 0) {
                        total += bytes
                        items += JunkItem(
                            id = "browser:$pkg",
                            label = runCatching {
                                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                            }.getOrDefault(pkg),
                            path = pkg,
                            uri = null,
                            sizeBytes = bytes,
                            categoryId = BROWSER_DATA
                        )
                    }
                } catch (_: Exception) { /* not installed */ }
            }
        }
        return JunkCategory(
            id = BROWSER_DATA,
            title = "Browser data",
            description = "Saved data collected by your browsers when you browse or search online.",
            group = CategoryGroup.Unneeded,
            locked = true,
            items = items.sortedByDescending { it.sizeBytes },
            reportedSizeBytes = total
        )
    }

    data class CleanOutcome(val freedBytes: Long, val deleted: Int, val failed: Int)

    suspend fun clean(
        items: List<JunkItem>,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): CleanOutcome = withContext(Dispatchers.IO) {
        var freed = 0L
        var ok = 0
        var failed = 0
        val total = items.size.coerceAtLeast(1)
        items.forEachIndexed { index, item ->
            coroutineContext.ensureActive()
            onProgress(index / total.toFloat(), item.label)
            if (item.categoryId == HIDDEN_CACHES || item.categoryId == BROWSER_DATA) {
                // Soft-clean: only our own cache when hidden was selected (handled below).
                return@forEachIndexed
            }
            val before = measureItemBytes(item)
            val success = deleteJunkItem(item)
            // Also clear MediaStore entry when we deleted by path.
            if (success && item.uri != null && item.path != null) {
                runCatching { deleteUri(item.uri) }
            }
            if (success) {
                val after = measureItemBytes(item)
                val delta = when {
                    before > 0L -> (before - after).coerceAtLeast(0L)
                    item.sizeBytes > 0L -> item.sizeBytes
                    else -> 0L
                }
                freed += delta
                ok++
            } else {
                failed++
            }
        }
        if (items.any { it.categoryId == HIDDEN_CACHES }) {
            val ownCache = dirSize(context.cacheDir) + dirSize(context.externalCacheDir)
            deleteChildren(context.cacheDir)
            context.externalCacheDir?.let { deleteChildren(it) }
            context.codeCacheDir?.let { deleteChildren(it) }
            if (ownCache > 0) {
                freed += ownCache
                ok++
            }
        }
        onProgress(1f, "")
        CleanOutcome(freed, ok, failed)
    }

    /** Real delete for residual folders, thumbnail files, visible caches, etc. */
    private fun deleteJunkItem(item: JunkItem): Boolean {
        val path = item.path
        val uri = item.uri
        if (path != null) {
            val f = File(path)
            if (isOwnCacheDir(f)) {
                deleteChildren(f)
                return true
            }
            if (f.isDirectory) {
                deleteChildren(f)
                return when (item.categoryId) {
                    VISIBLE_CACHES, AD_CACHES, TEMP_FILES -> true
                    else -> deleteRecursively(f) || !f.exists()
                }
            }
            if (f.isFile) {
                val deleted = f.delete() || !f.exists()
                if (deleted) return true
            }
            if (!f.exists() && uri != null) return deleteUri(uri)
            if (!f.exists()) return true
        }
        if (uri != null) return deleteUri(uri)
        return false
    }

    private fun isOwnCacheDir(f: File): Boolean {
        val path = f.absolutePath
        return path == context.cacheDir.absolutePath ||
            path == context.externalCacheDir?.absolutePath ||
            path == context.codeCacheDir.absolutePath ||
            context.externalCacheDirs.orEmpty().any { it != null && path == it.absolutePath }
    }

    private fun measureItemBytes(item: JunkItem): Long {
        item.path?.let { p ->
            val f = File(p)
            if (f.exists()) return if (f.isDirectory) dirSize(f) else f.length()
        }
        return item.sizeBytes.coerceAtLeast(0L)
    }

    private fun deleteUri(uri: Uri): Boolean = try {
        resolver.delete(uri, null, null) > 0
    } catch (e: Exception) {
        Log.w(TAG, "delete uri failed: ${e.message}")
        false
    }

    private fun installedApplications(): List<ApplicationInfo> = try {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
    } catch (_: Exception) {
        emptyList()
    }

    private fun installedPackageNames(): Set<String> =
        installedApplications().map { it.packageName }.toSet()

    private fun isRemovableEmptyDir(dir: File): Boolean {
        val path = dir.absolutePath
        if (path.contains("/Android/data") || path.contains("/Android/obb")) return false
        // Keep media/.thumbnails style empties out of this bucket.
        if (dir.name.equals(".thumbnails", true)) return false
        val children = dir.listFiles() ?: return false
        return children.isEmpty()
    }

    private inline fun walk(
        root: File,
        maxDepth: Int,
        deadlineMs: Long = Long.MAX_VALUE,
        visit: (File, Int) -> Unit
    ) {
        val stack = ArrayDeque<Pair<File, Int>>()
        stack.addLast(root to 0)
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_WALK_FILES) {
            if (System.currentTimeMillis() > deadlineMs) break
            val (dir, depth) = stack.removeLast()
            if (depth > maxDepth) continue
            val children = dir.listFiles() ?: continue
            for (child in children) {
                visited++
                if (visited > MAX_WALK_FILES || System.currentTimeMillis() > deadlineMs) break
                visit(child, depth + 1)
                if (child.isDirectory && !isSymlink(child) && !shouldSkipDir(child)) {
                    stack.addLast(child to depth + 1)
                }
            }
        }
    }

    private fun shouldSkipDir(dir: File): Boolean {
        val name = dir.name.lowercase()
        val parent = dir.parentFile?.absolutePath
        val extRoot = Environment.getExternalStorageDirectory()?.absolutePath
        // Skip the Android/ tree during generic walks — handled by dedicated scanners.
        if (name == "android" && parent == extRoot) return true
        // Skip huge media libraries, but never skip thumbnail folders inside them.
        if (THUMB_DIR_NAMES.contains(name)) return false
        if (name in setOf("dcim", "pictures", "movies", "music", "alarms", "notifications", "ringtones")) {
            return parent == extRoot
        }
        return false
    }

    private fun isSymlink(file: File): Boolean = try {
        file.canonicalFile != file.absoluteFile
    } catch (_: Exception) {
        true
    }

    private fun dirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        var total = 0L
        val stack = ArrayDeque<File>()
        stack.addLast(dir)
        var guard = 0
        while (stack.isNotEmpty() && guard++ < MAX_DIR_SIZE_NODES) {
            val current = stack.removeLast()
            val children = current.listFiles() ?: continue
            for (child in children) {
                if (child.isDirectory) {
                    if (!isSymlink(child)) stack.addLast(child)
                } else {
                    total += child.length()
                }
            }
        }
        return total
    }

    private fun deleteRecursively(file: File): Boolean {
        if (!file.exists()) return false
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        return file.delete()
    }

    private fun deleteChildren(dir: File?) {
        dir?.listFiles()?.forEach { deleteRecursively(it) }
    }
}
