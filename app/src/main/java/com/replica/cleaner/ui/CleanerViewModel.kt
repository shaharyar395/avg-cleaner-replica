package com.replica.cleaner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.replica.cleaner.core.PermissionStatus
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.data.CleanerRepository
import com.replica.cleaner.data.TipsEngine
import com.replica.cleaner.data.model.AppsSummary
import com.replica.cleaner.data.model.JunkItem
import com.replica.cleaner.data.model.MediaFile
import com.replica.cleaner.data.model.MediaSummary
import com.replica.cleaner.data.model.PhotoAnalysis
import com.replica.cleaner.data.model.ScanResult
import com.replica.cleaner.data.model.StorageSnapshot
import com.replica.cleaner.data.model.SystemInfo
import com.replica.cleaner.data.model.Tip
import com.replica.cleaner.data.model.UsageDay
import com.replica.cleaner.ui.theme.AccentChoice
import com.replica.cleaner.ui.theme.ThemeMode
import com.replica.cleaner.work.AutoCleanWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val running: Boolean = false,
    val progress: Float = 0f,
    val label: String = "",
    val result: ScanResult? = null,
    val error: String? = null
)

data class CleanUiState(
    val running: Boolean = false,
    val progress: Float = 0f,
    val label: String = "",
    val freedBytes: Long = -1L,
    val deleted: Int = 0,
    val failed: Int = 0,
    val cleanedItems: List<JunkItem> = emptyList()
) {
    val finished: Boolean get() = freedBytes >= 0 && !running
}

class CleanerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CleanerRepository.get(app)
    val prefs = repo.prefs

    private val _permissions = MutableStateFlow(Permissions.status(app))
    val permissions: StateFlow<PermissionStatus> = _permissions.asStateFlow()

    private val _storage = MutableStateFlow(repo.storage())
    val storage: StateFlow<StorageSnapshot> = _storage.asStateFlow()

    private val _scan = MutableStateFlow(ScanUiState())
    val scan: StateFlow<ScanUiState> = _scan.asStateFlow()

    private val _clean = MutableStateFlow(CleanUiState())
    val clean: StateFlow<CleanUiState> = _clean.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    private val _media = MutableStateFlow<MediaSummary?>(null)
    val media: StateFlow<MediaSummary?> = _media.asStateFlow()

    private val _photos = MutableStateFlow<PhotoAnalysis?>(null)
    val photos: StateFlow<PhotoAnalysis?> = _photos.asStateFlow()

    private val _photoProgress = MutableStateFlow(1f)
    val photoProgress: StateFlow<Float> = _photoProgress.asStateFlow()

    private val _apps = MutableStateFlow<AppsSummary?>(null)
    val apps: StateFlow<AppsSummary?> = _apps.asStateFlow()

    private val _appsLoading = MutableStateFlow(false)
    val appsLoading: StateFlow<Boolean> = _appsLoading.asStateFlow()

    private val _usageWeek = MutableStateFlow<List<UsageDay>>(emptyList())
    val usageWeek: StateFlow<List<UsageDay>> = _usageWeek.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()

    private val _tips = MutableStateFlow<List<Tip>>(emptyList())
    val tips: StateFlow<List<Tip>> = _tips.asStateFlow()

    val premium: StateFlow<Boolean> =
        prefs.premium.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val themeMode: StateFlow<ThemeMode> =
        prefs.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.Dark)
    val accent: StateFlow<AccentChoice> =
        prefs.accent.stateIn(viewModelScope, SharingStarted.Eagerly, AccentChoice.Green)

    private var scanJob: Job? = null
    private var cleanJob: Job? = null

    init {
        refreshStorage()
    }

    // ---- permissions ----------------------------------------------------

    fun refreshPermissions() {
        _permissions.value = Permissions.status(getApplication())
    }

    // ---- storage --------------------------------------------------------

    fun refreshStorage() {
        _storage.value = repo.storage()
    }

    // ---- junk scan ------------------------------------------------------

    fun startScan(force: Boolean = false) {
        if (_scan.value.running && !force) return
        if (!force) {
            repo.lastScan?.let {
                _scan.value = ScanUiState(result = it)
                applyDefaultSelection(it)
                return
            }
        }
        scanJob?.cancel()
        _scan.value = ScanUiState(running = true)
        scanJob = viewModelScope.launch {
            try {
                prefs.ensureAlwaysEnabledQcCategories()
                val result = repo.scanJunk { progress, label ->
                    _scan.update { it.copy(progress = progress, label = label) }
                }
                _scan.value = ScanUiState(running = false, progress = 1f, result = result)
                applyDefaultSelection(result)
                refreshStorage()
                rebuildTips()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Leave result null so Quick Clean can show a retry state instead
                // of spinning forever on "Finding junk…".
                _scan.value = ScanUiState(
                    running = false,
                    progress = 1f,
                    result = null,
                    error = e.message?.takeIf { it.isNotBlank() } ?: "Scan failed. Try again."
                )
            }
        }
    }

    /**
     * Pre-select every deletable Unneeded item (Downloads stay unticked like AVG).
     * Empty free categories keep a category-id marker so Visible / Residual checkboxes
     * still toggle from Tools and Home.
     */
    private fun applyDefaultSelection(result: ScanResult) {
        val next = mutableSetOf<String>()
        result.unneeded.filter { !it.locked }.forEach { category ->
            val itemIds = category.items
                .filter { it.sizeBytes > 0L || it.categoryId == "empty_folders" }
                .map { it.id }
            if (itemIds.isNotEmpty()) {
                next += itemIds
            } else {
                next += category.id
            }
        }
        _selected.value = next
    }

    fun toggleItem(id: String) {
        val result = _scan.value.result ?: return
        val category = result.categories.firstOrNull { c -> c.items.any { it.id == id } } ?: return
        if (category.locked && !premium.value) return
        // Only allow selecting real deletable junk.
        val item = category.items.firstOrNull { it.id == id } ?: return
        if (item.sizeBytes <= 0L && item.categoryId != "empty_folders") return
        _selected.update { current ->
            val withoutMarker = current - category.id
            if (id in withoutMarker) withoutMarker - id else withoutMarker + id
        }
    }

    fun toggleCategory(categoryId: String) {
        val result = _scan.value.result ?: return
        val category = result.categories.firstOrNull { it.id == categoryId } ?: return
        if (category.locked && !premium.value) return
        val ids = category.items
            .filter { it.sizeBytes > 0L || it.categoryId == "empty_folders" }
            .map { it.id }
        if (ids.isEmpty()) {
            // Empty free row: toggle category marker so the checkbox still works.
            _selected.update { current ->
                if (categoryId in current) current - categoryId else current + categoryId
            }
            return
        }
        val allSelected = ids.all { it in _selected.value }
        _selected.update { current ->
            val cleared = current - categoryId
            if (allSelected) cleared - ids.toSet() else cleared + ids.toSet()
        }
    }

    fun selectedItems(): List<JunkItem> {
        val result = _scan.value.result ?: return emptyList()
        val ids = _selected.value
        val allowLocked = premium.value
        val categoryIds = result.categories.map { it.id }.toSet()
        return result.categories
            .filter { !it.locked || allowLocked }
            .flatMap { it.items }
            .filter {
                it.id in ids &&
                    it.id !in categoryIds &&
                    (it.sizeBytes > 0L || it.categoryId == "empty_folders")
            }
    }

    val selectedBytes: Long get() = selectedItems().sumOf { it.sizeBytes }

    // ---- cleaning -------------------------------------------------------

    fun finishCleaning() {
        if (_clean.value.running) return
        val items = selectedItems()
        if (items.isEmpty()) return
        cleanJob?.cancel()
        _clean.value = CleanUiState(running = true)
        cleanJob = viewModelScope.launch {
            try {
                val freeBefore = repo.storage().freeBytes
                val outcome = repo.clean(items) { progress, label ->
                    _clean.update { it.copy(progress = progress, label = label) }
                }
                // Let the filesystem / MediaStore settle, then read real free space.
                kotlinx.coroutines.delay(600)
                refreshStorage()
                kotlinx.coroutines.delay(400)
                refreshStorage()
                val freeAfter = _storage.value.freeBytes
                val diskFreed = (freeAfter - freeBefore).coerceAtLeast(0L)
                val reported = maxOf(outcome.freedBytes, diskFreed)
                _clean.value = CleanUiState(
                    running = false,
                    progress = 1f,
                    freedBytes = reported,
                    deleted = outcome.deleted,
                    failed = outcome.failed,
                    cleanedItems = items
                )
                _selected.value = emptySet()
                // Force a fresh junk scan so Home / Quick Clean show real leftovers.
                startScan(force = true)
                refreshStorage()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _clean.value = CleanUiState(
                    running = false,
                    progress = 1f,
                    freedBytes = 0L,
                    deleted = 0,
                    failed = items.size,
                    cleanedItems = emptyList()
                )
            }
        }
    }

    fun dismissCleanResult() {
        _clean.value = CleanUiState()
    }

    // ---- media ----------------------------------------------------------

    fun loadMedia(force: Boolean = false) {
        if (_media.value != null && !force) return
        viewModelScope.launch {
            _media.value = repo.scanMedia()
            rebuildTips()
        }
    }

    /** Clears a stale empty cache after the user grants storage/media access. */
    fun reloadMediaIfNeeded() {
        val current = _media.value
        val empty = current == null ||
            (current.photos.count == 0 && current.video.count == 0 &&
                current.audio.count == 0 && current.others.count == 0)
        if (empty) loadMedia(force = true)
    }

    fun analyzePhotos(force: Boolean = false) {
        if (_photos.value != null && !force) return
        viewModelScope.launch {
            _photoProgress.value = 0f
            _photos.value = repo.analyzePhotos { _photoProgress.value = it }
            _photoProgress.value = 1f
            rebuildTips()
        }
    }

    suspend fun images(limit: Int = 2000): List<MediaFile> = repo.images(limit)
    suspend fun videos(limit: Int = 1000): List<MediaFile> = repo.videos(limit)
    suspend fun audio(limit: Int = 1000): List<MediaFile> = repo.audio(limit)
    suspend fun otherFiles(limit: Int = 500): List<MediaFile> = repo.otherFiles(limit)

    fun deleteMedia(files: List<MediaFile>, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val freed = repo.deleteMedia(files)
            _media.value = null
            _photos.value = null
            loadMedia()
            refreshStorage()
            onDone(freed)
        }
    }

    fun optimizePhotos(files: List<MediaFile>, onProgress: (Float, String) -> Unit, onDone: (Long, Int, Int) -> Unit) {
        viewModelScope.launch {
            val outcome = repo.optimizePhotos(files, onProgress)
            _media.value = null
            _photos.value = null
            loadMedia()
            analyzePhotos(force = true)
            refreshStorage()
            onDone(outcome.freedBytes, outcome.optimized, outcome.failed)
        }
    }

    fun optimizeVideos(files: List<MediaFile>, onProgress: (Float, String) -> Unit, onDone: (Long, Int, Int) -> Unit) {
        viewModelScope.launch {
            val outcome = repo.optimizeVideos(files, onProgress)
            _media.value = null
            loadMedia(force = true)
            refreshStorage()
            onDone(outcome.freedBytes, outcome.optimized, outcome.failed)
        }
    }

    // ---- apps -----------------------------------------------------------

    fun loadApps(force: Boolean = false) {
        if (_apps.value != null && !force) return
        if (_appsLoading.value && !force) return
        viewModelScope.launch {
            _appsLoading.value = true
            try {
                _apps.value = repo.scanApps()
                _usageWeek.value = repo.usageLastWeek()
                rebuildTips()
            } finally {
                _appsLoading.value = false
            }
        }
    }

    fun uninstall(packageName: String) {
        val intent = repo.appScanner.uninstallIntent(packageName)
        getApplication<Application>().startActivity(intent)
    }

    fun openAppSettings(packageName: String) {
        Permissions.safeStart(getApplication(), Permissions.appDetailsIntent(getApplication(), packageName))
    }

    fun isInstalled(packageName: String): Boolean = repo.appScanner.isInstalled(packageName)

    /** Opens the Play listing, falling back to the web page when Play is absent. */
    fun openPlayStore(context: android.content.Context, packageName: String) {
        val market = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("market://details?id=$packageName")
        )
        val web = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        Permissions.safeStart(context, market, web)
    }

    // ---- system info ----------------------------------------------------

    fun loadSystemInfo() {
        viewModelScope.launch { _systemInfo.value = repo.systemInfo() }
    }

    // ---- tips -----------------------------------------------------------

    fun rebuildTips() {
        viewModelScope.launch {
            _tips.value = TipsEngine.build(
                scan = _scan.value.result ?: repo.lastScan,
                media = _media.value,
                photos = _photos.value,
                apps = _apps.value,
                priority = prefs.tipPriority.first()
            )
        }
    }

    /** Tips are the union of every scan, so the screen warms all of them up. */
    fun loadEverythingForTips() {
        loadMedia()
        loadApps()
        analyzePhotos()
        if (_scan.value.result == null && repo.lastScan == null) startScan()
        else rebuildTips()
    }

    // ---- settings -------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setAccent(accent: AccentChoice) = viewModelScope.launch { prefs.setAccent(accent) }
    fun setPremium(value: Boolean) = viewModelScope.launch { prefs.setPremium(value) }
    fun setConsented() = viewModelScope.launch { prefs.setConsented(true) }
    fun setOnboarded() = viewModelScope.launch { prefs.setOnboarded(true) }

    fun setAutoClean(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAutoCleanEnabled(enabled)
            val context = getApplication<Application>()
            if (enabled) AutoCleanWorker.schedule(context, prefs.autoCleanFrequency.first())
            else AutoCleanWorker.cancel(context)
        }
    }

    fun setAutoCleanFrequency(frequency: String) {
        viewModelScope.launch {
            prefs.setAutoCleanFrequency(frequency)
            if (prefs.autoCleanEnabled.first()) {
                AutoCleanWorker.schedule(getApplication(), frequency)
            }
        }
    }

    fun signIn(email: String, provider: String = "password") = viewModelScope.launch {
        prefs.setEmail(email)
        prefs.setAuthProvider(provider)
    }

    fun signInWithPassword(email: String, password: String) = viewModelScope.launch {
        prefs.setEmail(email)
        prefs.setPasswordHash(hashPassword(password))
        prefs.setAuthProvider("password")
    }

    fun registerAccount(email: String, password: String) = viewModelScope.launch {
        prefs.setEmail(email)
        prefs.setPasswordHash(hashPassword(password))
        prefs.setAuthProvider("password")
    }

    fun resetPassword(email: String, password: String) = viewModelScope.launch {
        prefs.setEmail(email)
        prefs.setPasswordHash(hashPassword(password))
        prefs.setAuthProvider("password")
    }

    suspend fun verifyPassword(email: String, password: String): Boolean {
        val storedEmail = prefs.email.first()
        val hash = prefs.passwordHash.first()
        val provider = prefs.authProvider.first()
        if (storedEmail == null) {
            // First-time email sign-in: create local account.
            prefs.setEmail(email)
            prefs.setPasswordHash(hashPassword(password))
            prefs.setAuthProvider("password")
            return true
        }
        if (!storedEmail.equals(email, ignoreCase = true)) return false
        if (provider == "google" && hash == null) return true
        if (hash == null) {
            prefs.setPasswordHash(hashPassword(password))
            prefs.setAuthProvider("password")
            return true
        }
        return hash == hashPassword(password)
    }

    fun signOut() = viewModelScope.launch { prefs.clearAccount() }

    companion object {
        fun hashPassword(password: String): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            return digest.digest(password.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
    }
}
