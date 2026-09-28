package com.replica.cleaner.ui.nav

object Routes {
    // First run (clear-data / fresh install)
    const val SPLASH = "splash"
    const val GET_STARTED = "get_started"
    const val UPDATING_DB = "updating_db"
    const val INTRO = "intro"
    const val CONSENT = "consent"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val PERMISSION_NEEDED = "permission_needed"
    const val SCANNING = "scanning"
    const val READY = "ready"

    // Bottom navigation
    const val HOME = "home"
    const val TOOLS = "tools"
    const val STORAGE = "storage"
    const val ACCOUNT = "account"

    // Cleaning
    const val QUICK_CLEAN = "quick_clean"
    const val CLEANING = "cleaning"
    const val ADVANCED_ISSUES = "advanced_issues"
    const val CLEANING_RESULTS = "cleaning_results"
    const val SPACE_CLEANED = "space_cleaned"
    const val CONTINUE_ADS = "continue_ads"

    // Storage detail
    const val MEDIA_OVERVIEW = "media_overview"
    const val PHOTO_GRID = "photo_grid/{kind}"
    fun photoGrid(kind: String) = "photo_grid/$kind"
    /** Storage category gate: commercial break + ads before content. */
    const val STORAGE_GATE = "storage_gate/{kind}"
    fun storageGate(kind: String) = "storage_gate/$kind"

    // Apps
    const val APPS_OVERVIEW = "apps_overview"
    /** Storage → Apps: commercial break + ads, then All apps. */
    const val APPS_GATE = "apps_gate"
    const val APP_LIST = "app_list/{kind}"
    fun appList(kind: String) = "app_list/$kind"

    const val TIPS = "tips"
    const val SYSTEM_INFO = "system_info"
    const val CLOUD_TRANSFERS = "cloud_transfers"

    // Settings tree
    const val SETTINGS = "settings"
    const val SETTINGS_QUICK_CLEAN = "settings/quick_clean"
    const val SETTINGS_ANALYSIS = "settings/analysis"
    const val SETTINGS_NOTIFICATIONS = "settings/notifications"
    const val SETTINGS_NOTIFICATION_CATEGORY = "settings/notifications/{id}"
    fun notificationCategory(id: String) = "settings/notifications/$id"
    const val SETTINGS_REALTIME = "settings/realtime"
    const val SETTINGS_CLOUD = "settings/cloud"
    const val SETTINGS_PRIVACY = "settings/privacy"
    const val SETTINGS_LANGUAGE = "settings/language"
    const val AUTO_CLEANING = "auto_cleaning"
    const val AUTO_CLEAN_CATEGORY = "auto_cleaning/{id}"
    fun autoCleanCategory(id: String) = "auto_cleaning/$id"
    const val THEMES = "themes"

    // Account
    const val PREMIUM = "premium"
    /** First-run paywall; closing continues to Continue-with-ads. */
    const val PREMIUM_GATE = "premium_gate"
    /** Resolve-all paywall after first clean; closing continues to Space cleaned. */
    const val PREMIUM_RESOLVE = "premium_resolve"
    const val PREMIUM_FEATURES = "premium_features"
    const val SIGN_IN = "sign_in"
    const val REDEEM_SUBSCRIPTION = "redeem_subscription"
    const val REDEEM_ACTIVATION = "redeem_activation"
    const val ABOUT = "about"
    const val LICENSES = "licenses"

    // Premium feature landing pages
    const val FEATURE_DETAIL = "feature/{id}"
    fun featureDetail(id: String) = "feature/$id"

    /** Feature-specific premium upsell (Learn More from Explore features tour). */
    const val FEATURE_UPSELL = "feature_upsell/{id}"
    fun featureUpsell(id: String) = "feature_upsell/$id"

    val bottomTabs = listOf(HOME, TOOLS, STORAGE, ACCOUNT)
}
