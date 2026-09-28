package com.replica.cleaner.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.replica.cleaner.ui.theme.AccentChoice
import com.replica.cleaner.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("cleaner_prefs")

/**
 * Every toggle the Settings tree exposes, plus the small amount of app state
 * (onboarding done, premium flag, signed-in email) that has to survive restarts.
 */
class Prefs(private val context: Context) {

    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val consented = booleanPreferencesKey("consented")
        val premium = booleanPreferencesKey("premium")
        val email = stringPreferencesKey("account_email")
        val passwordHash = stringPreferencesKey("account_password_hash")
        val authProvider = stringPreferencesKey("auth_provider")

        val themeMode = stringPreferencesKey("theme_mode")
        val accent = stringPreferencesKey("accent")
        val language = stringPreferencesKey("language")

        // Quick Clean categories that are ticked by default
        val enabledCategories = stringSetPreferencesKey("qc_enabled_categories")

        // Analysis preferences
        val findUnwantedPhotos = booleanPreferencesKey("find_unwanted_photos")
        val scanSdCard = booleanPreferencesKey("scan_sd_card")
        val tipPriority = stringPreferencesKey("tip_priority")

        // Notifications
        val notificationsMaster = booleanPreferencesKey("notif_master")
        val notifThresholdMb = intPreferencesKey("notif_threshold_mb")
        val notifChannels = stringSetPreferencesKey("notif_channels")
        val reportDay = stringPreferencesKey("report_day")

        // Auto cleaning
        val autoCleanEnabled = booleanPreferencesKey("auto_clean_enabled")
        val autoCleanFrequency = stringPreferencesKey("auto_clean_frequency")
        val autoCleanCategories = stringSetPreferencesKey("auto_clean_categories")
        val downloadsKeepDays = intPreferencesKey("downloads_keep_days")
        val screenshotsKeepDays = intPreferencesKey("screenshots_keep_days")
        val optimizedOriginalsKeepDays = intPreferencesKey("optimized_originals_keep_days")

        // Cloud + privacy + realtime
        val deleteAfterTransfer = booleanPreferencesKey("delete_after_transfer")
        val wifiOnlyUpload = booleanPreferencesKey("wifi_only_upload")
        val connectedCloudProviders = stringSetPreferencesKey("connected_cloud_providers")
        val realtimeDetection = booleanPreferencesKey("realtime_detection") // legacy
        val appLeftovers = booleanPreferencesKey("app_leftovers")
        val batteryMonitoring = booleanPreferencesKey("battery_monitoring")
        val personalisedAds = booleanPreferencesKey("personalised_ads")
        val shareUsageAvg = booleanPreferencesKey("share_usage_avg")
        val shareUsageThirdParty = booleanPreferencesKey("share_usage_third_party")

        val lastCleanAt = longPreferencesKey("last_clean_at")
        val totalFreed = longPreferencesKey("total_freed")
    }

    private val store get() = context.dataStore

    val onboarded: Flow<Boolean> = store.data.map { it[K.onboarded] ?: false }
    val consented: Flow<Boolean> = store.data.map { it[K.consented] ?: false }
    val premium: Flow<Boolean> = store.data.map { it[K.premium] ?: false }
    val email: Flow<String?> = store.data.map { it[K.email] }
    val passwordHash: Flow<String?> = store.data.map { it[K.passwordHash] }
    val authProvider: Flow<String?> = store.data.map { it[K.authProvider] }

    val themeMode: Flow<ThemeMode> = store.data.map {
        runCatching { ThemeMode.valueOf(it[K.themeMode] ?: "Dark") }.getOrDefault(ThemeMode.Dark)
    }
    val accent: Flow<AccentChoice> = store.data.map {
        runCatching { AccentChoice.valueOf(it[K.accent] ?: "Green") }.getOrDefault(AccentChoice.Green)
    }
    val language: Flow<String> = store.data.map {
        it[K.language] ?: com.replica.cleaner.l10n.AppLanguage.DEFAULT.displayName
    }

    val enabledCategories: Flow<Set<String>> = store.data.map {
        ((it[K.enabledCategories] ?: DEFAULT_QUICK_CLEAN_CATEGORIES) + ALWAYS_ENABLED_QC_CATEGORIES)
    }

    val findUnwantedPhotos: Flow<Boolean> = store.data.map { it[K.findUnwantedPhotos] ?: true }
    val scanSdCard: Flow<Boolean> = store.data.map { it[K.scanSdCard] ?: false }
    val tipPriority: Flow<List<String>> = store.data.map {
        (it[K.tipPriority] ?: DEFAULT_TIP_PRIORITY.joinToString("|")).split("|").filter { s -> s.isNotBlank() }
    }

    val notificationsMaster: Flow<Boolean> = store.data.map { it[K.notificationsMaster] ?: true }
    val notifThresholdMb: Flow<Int> = store.data.map { it[K.notifThresholdMb] ?: 50 }
    val notifChannels: Flow<Set<String>> = store.data.map { it[K.notifChannels] ?: DEFAULT_NOTIF_CHANNELS }
    val reportDay: Flow<String> = store.data.map { it[K.reportDay] ?: "Friday" }

    val autoCleanEnabled: Flow<Boolean> = store.data.map { it[K.autoCleanEnabled] ?: false }
    val autoCleanFrequency: Flow<String> = store.data.map { it[K.autoCleanFrequency] ?: "Daily" }
    val autoCleanCategories: Flow<Set<String>> = store.data.map {
        it[K.autoCleanCategories] ?: DEFAULT_AUTO_CLEAN_CATEGORIES
    }
    val downloadsKeepDays: Flow<Int> = store.data.map { it[K.downloadsKeepDays] ?: 7 }
    val screenshotsKeepDays: Flow<Int> = store.data.map { it[K.screenshotsKeepDays] ?: 7 }
    val optimizedOriginalsKeepDays: Flow<Int> = store.data.map { it[K.optimizedOriginalsKeepDays] ?: 7 }

    val deleteAfterTransfer: Flow<Boolean> = store.data.map { it[K.deleteAfterTransfer] ?: true }
    val wifiOnlyUpload: Flow<Boolean> = store.data.map { it[K.wifiOnlyUpload] ?: true }
    val connectedCloudProviders: Flow<Set<String>> = store.data.map {
        it[K.connectedCloudProviders] ?: emptySet()
    }
    val realtimeDetection: Flow<Boolean> = store.data.map { it[K.realtimeDetection] ?: true }
    val appLeftovers: Flow<Boolean> = store.data.map { it[K.appLeftovers] ?: true }
    val batteryMonitoring: Flow<Boolean> = store.data.map { it[K.batteryMonitoring] ?: false }
    val personalisedAds: Flow<Boolean> = store.data.map { it[K.personalisedAds] ?: true }
    val shareUsageAvg: Flow<Boolean> = store.data.map { it[K.shareUsageAvg] ?: true }
    val shareUsageThirdParty: Flow<Boolean> = store.data.map { it[K.shareUsageThirdParty] ?: false }

    val lastCleanAt: Flow<Long> = store.data.map { it[K.lastCleanAt] ?: 0L }
    val totalFreed: Flow<Long> = store.data.map { it[K.totalFreed] ?: 0L }

    suspend fun setOnboarded(v: Boolean) = store.edit { it[K.onboarded] = v }
    suspend fun setConsented(v: Boolean) = store.edit { it[K.consented] = v }
    suspend fun setPremium(v: Boolean) = store.edit { it[K.premium] = v }
    suspend fun setEmail(v: String?) = store.edit { p -> if (v == null) p.remove(K.email) else p[K.email] = v }

    suspend fun setPasswordHash(v: String?) = store.edit { p ->
        if (v == null) p.remove(K.passwordHash) else p[K.passwordHash] = v
    }

    suspend fun setAuthProvider(v: String?) = store.edit { p ->
        if (v == null) p.remove(K.authProvider) else p[K.authProvider] = v
    }

    suspend fun clearAccount() = store.edit { p ->
        p.remove(K.email)
        p.remove(K.passwordHash)
        p.remove(K.authProvider)
    }

    suspend fun setThemeMode(v: ThemeMode) = store.edit { it[K.themeMode] = v.name }
    suspend fun setAccent(v: AccentChoice) = store.edit { it[K.accent] = v.name }
    suspend fun setLanguage(v: String) = store.edit { it[K.language] = v }

    suspend fun setCategoryEnabled(id: String, enabled: Boolean) = store.edit { p ->
        // Free Quick Clean staples (Visible caches, Residual, …) stay on — same as Home.
        if (id in ALWAYS_ENABLED_QC_CATEGORIES && !enabled) return@edit
        val current = (p[K.enabledCategories] ?: DEFAULT_QUICK_CLEAN_CATEGORIES).toMutableSet()
        if (enabled) current.add(id) else current.remove(id)
        current.addAll(ALWAYS_ENABLED_QC_CATEGORIES)
        p[K.enabledCategories] = current
    }

    /** Re-assert free Quick Clean categories so Tools / Home always scan them. */
    suspend fun ensureAlwaysEnabledQcCategories() = store.edit { p ->
        val current = (p[K.enabledCategories] ?: DEFAULT_QUICK_CLEAN_CATEGORIES).toMutableSet()
        current.addAll(ALWAYS_ENABLED_QC_CATEGORIES)
        p[K.enabledCategories] = current
    }

    suspend fun setFindUnwantedPhotos(v: Boolean) = store.edit { it[K.findUnwantedPhotos] = v }
    suspend fun setScanSdCard(v: Boolean) = store.edit { it[K.scanSdCard] = v }
    suspend fun setTipPriority(order: List<String>) = store.edit { it[K.tipPriority] = order.joinToString("|") }

    suspend fun setNotificationsMaster(v: Boolean) = store.edit { it[K.notificationsMaster] = v }
    suspend fun setNotifThresholdMb(v: Int) = store.edit { it[K.notifThresholdMb] = v }
    suspend fun setNotifChannel(id: String, enabled: Boolean) = store.edit { p ->
        val current = (p[K.notifChannels] ?: DEFAULT_NOTIF_CHANNELS).toMutableSet()
        if (enabled) current.add(id) else current.remove(id)
        p[K.notifChannels] = current
    }
    suspend fun setReportDay(v: String) = store.edit { it[K.reportDay] = v }

    suspend fun setAutoCleanEnabled(v: Boolean) = store.edit { it[K.autoCleanEnabled] = v }
    suspend fun setAutoCleanFrequency(v: String) = store.edit { it[K.autoCleanFrequency] = v }
    suspend fun setAutoCleanCategory(id: String, enabled: Boolean) = store.edit { p ->
        val current = (p[K.autoCleanCategories] ?: DEFAULT_AUTO_CLEAN_CATEGORIES).toMutableSet()
        if (enabled) current.add(id) else current.remove(id)
        p[K.autoCleanCategories] = current
    }
    suspend fun setDownloadsKeepDays(v: Int) = store.edit { it[K.downloadsKeepDays] = v }
    suspend fun setScreenshotsKeepDays(v: Int) = store.edit { it[K.screenshotsKeepDays] = v }
    suspend fun setOptimizedOriginalsKeepDays(v: Int) = store.edit { it[K.optimizedOriginalsKeepDays] = v }

    suspend fun setDeleteAfterTransfer(v: Boolean) = store.edit { it[K.deleteAfterTransfer] = v }
    suspend fun setWifiOnlyUpload(v: Boolean) = store.edit { it[K.wifiOnlyUpload] = v }
    suspend fun setCloudProviderConnected(id: String, connected: Boolean) = store.edit { p ->
        val current = (p[K.connectedCloudProviders] ?: emptySet()).toMutableSet()
        if (connected) current.add(id) else current.remove(id)
        p[K.connectedCloudProviders] = current
    }
    suspend fun setRealtimeDetection(v: Boolean) = store.edit { it[K.realtimeDetection] = v }
    suspend fun setAppLeftovers(v: Boolean) = store.edit { it[K.appLeftovers] = v }
    suspend fun setBatteryMonitoring(v: Boolean) = store.edit { it[K.batteryMonitoring] = v }
    suspend fun setPersonalisedAds(v: Boolean) = store.edit { it[K.personalisedAds] = v }
    suspend fun setShareUsageAvg(v: Boolean) = store.edit { it[K.shareUsageAvg] = v }
    suspend fun setShareUsageThirdParty(v: Boolean) = store.edit { it[K.shareUsageThirdParty] = v }

    suspend fun recordClean(freedBytes: Long) = store.edit { p ->
        p[K.lastCleanAt] = System.currentTimeMillis()
        p[K.totalFreed] = (p[K.totalFreed] ?: 0L) + freedBytes
    }

    companion object {
        val DEFAULT_QUICK_CLEAN_CATEGORIES = setOf(
            "visible_caches", "residual_files", "installed_apks", "ad_caches",
            "thumbnails", "empty_folders", "trash", "downloads", "large_old_files"
        )
        /** Always scanned / toggleable from Home and Tools Quick Clean (never premium-locked). */
        val ALWAYS_ENABLED_QC_CATEGORIES = setOf(
            "visible_caches", "residual_files", "installed_apks", "ad_caches",
            "thumbnails", "empty_folders"
        )
        /** Matches AVG: Residual, Installed APKs, Ad caches, Empty folders on; Thumbnails off. */
        val DEFAULT_AUTO_CLEAN_CATEGORIES = setOf(
            "residual_files", "installed_apks", "ad_caches", "empty_folders"
        )
        val DEFAULT_NOTIF_CHANNELS = setOf(
            "junk_cleaning", "applications", "photos", "other_files"
        )
        val DEFAULT_TIP_PRIORITY = listOf(
            "Junk cleaning", "Device memory", "Apps", "Photos and video", "Other files"
        )
    }
}
