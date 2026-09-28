package com.replica.cleaner.ui.nav

/**
 * Feature-transition ads — always the large full-screen overlay
 * (see TransitionAdOverlay). Close X after 3–5 seconds.
 */
object FeatureTransitionAds {
    private val noAdTargets = setOf(
        Routes.SPLASH,
        Routes.GET_STARTED,
        Routes.UPDATING_DB,
        Routes.INTRO,
        Routes.CONSENT,
        Routes.ONBOARDING,
        Routes.PERMISSIONS,
        Routes.PERMISSION_NEEDED,
        Routes.SCANNING,
        Routes.READY,
        Routes.ADVANCED_ISSUES,
        Routes.CLEANING_RESULTS,
        Routes.SPACE_CLEANED,
        Routes.CONTINUE_ADS,
        Routes.PREMIUM,
        Routes.PREMIUM_GATE,
        Routes.PREMIUM_RESOLVE,
        Routes.PREMIUM_FEATURES,
        Routes.SIGN_IN,
        Routes.REDEEM_SUBSCRIPTION,
        Routes.REDEEM_ACTIVATION
    )

    /** Secondary bottom tabs — system back should go Home (with an ad). */
    val tabsBackToHome = setOf(Routes.TOOLS, Routes.STORAGE, Routes.ACCOUNT)

    fun isAdTransitionRoute(route: String?): Boolean {
        if (route == null) return false
        val base = route.substringBefore('/')
        if (base in noAdTargets) return false
        if (route.startsWith("settings")) return false
        return when (base) {
            Routes.HOME,
            Routes.TOOLS,
            Routes.STORAGE,
            Routes.ACCOUNT,
            Routes.QUICK_CLEAN,
            Routes.CLEANING,
            Routes.MEDIA_OVERVIEW,
            "photo_grid",
            "storage_gate",
            Routes.APPS_OVERVIEW,
            Routes.APPS_GATE,
            "app_list",
            Routes.TIPS,
            Routes.SYSTEM_INFO,
            Routes.CLOUD_TRANSFERS,
            Routes.AUTO_CLEANING,
            "auto_cleaning",
            Routes.THEMES,
            Routes.ABOUT,
            Routes.LICENSES,
            "feature",
            "feature_upsell" -> true
            else -> false
        }
    }

    fun isExitAdRoute(route: String?): Boolean {
        if (route == null) return false
        if (route in Routes.bottomTabs) return false
        return isAdTransitionRoute(route)
    }

    fun isTabBackToHome(route: String?): Boolean = route in tabsBackToHome

    fun run(
        premium: Boolean,
        targetRoute: String? = null,
        showOverlay: (() -> Unit) -> Unit,
        action: () -> Unit
    ) {
        if (premium || (targetRoute != null && !isAdTransitionRoute(targetRoute))) {
            action()
            return
        }
        // Always the same large full-screen overlay (never mix with small interstitial creatives).
        showOverlay(action)
    }

    fun runOnExit(
        premium: Boolean,
        currentRoute: String?,
        showOverlay: (() -> Unit) -> Unit,
        action: () -> Unit
    ) {
        if (premium) {
            action()
            return
        }
        val allow = isExitAdRoute(currentRoute) || isTabBackToHome(currentRoute)
        if (!allow) {
            action()
            return
        }
        showOverlay(action)
    }
}
