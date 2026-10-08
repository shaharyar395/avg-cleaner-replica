package com.replica.cleaner.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.replica.cleaner.ads.InterstitialAdManager
import com.replica.cleaner.ads.findActivity
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerBottomBar
import com.replica.cleaner.ui.components.HomeBannerAd
import com.replica.cleaner.ui.components.TransitionAdOverlay
import com.replica.cleaner.ui.screens.AboutScreen
import com.replica.cleaner.ui.screens.AccountScreen
import com.replica.cleaner.ui.screens.AnalysisPreferencesScreen
import com.replica.cleaner.ui.screens.AppListScreen
import com.replica.cleaner.ui.screens.AppsGateScreen
import com.replica.cleaner.ui.screens.ContentGateScreen
import com.replica.cleaner.ui.screens.AppsOverviewScreen
import com.replica.cleaner.ui.screens.AutoCleanCategoryScreen
import com.replica.cleaner.ui.screens.AutoCleaningScreen
import com.replica.cleaner.ui.screens.BrowserCleanerScreen
import com.replica.cleaner.ui.screens.CleaningScreen
import com.replica.cleaner.ui.screens.CloudServicesScreen
import com.replica.cleaner.ui.screens.CloudTransfersScreen
import com.replica.cleaner.ui.screens.ConsentScreen
import com.replica.cleaner.ui.screens.DeepCleanScreen
import com.replica.cleaner.ui.screens.FeatureDetailScreen
import com.replica.cleaner.ui.screens.FeatureTourKind
import com.replica.cleaner.ui.screens.FeatureUpsellScreen
import com.replica.cleaner.ui.screens.GetStartedScreen
import com.replica.cleaner.ui.screens.HomeScreen
import com.replica.cleaner.ui.screens.toUpsellId
import com.replica.cleaner.ui.screens.IntroCarouselScreen
import com.replica.cleaner.ui.screens.LanguageScreen
import com.replica.cleaner.ui.screens.LicensesScreen
import com.replica.cleaner.ui.screens.MediaGridScreen
import com.replica.cleaner.ui.screens.MediaOverviewScreen
import com.replica.cleaner.ui.screens.NotificationCategoryScreen
import com.replica.cleaner.ui.screens.NotificationsSettingsScreen
import com.replica.cleaner.ui.screens.OnboardingScreen
import com.replica.cleaner.ui.screens.PermissionNeededScreen
import com.replica.cleaner.ui.screens.PermissionsScreen
import com.replica.cleaner.ui.screens.PersonalPrivacyScreen
import com.replica.cleaner.ui.screens.PhotoOptimizerScreen
import com.replica.cleaner.ui.screens.PremiumFeaturesScreen
import com.replica.cleaner.ui.screens.PremiumScreen
import com.replica.cleaner.ui.screens.UpdatingDatabaseScreen
import com.replica.cleaner.ui.screens.QuickCleanScreen
import com.replica.cleaner.ui.screens.QuickCleanSettingsScreen
import com.replica.cleaner.ui.screens.ReadyScreen
import com.replica.cleaner.ui.screens.RealtimeDetectionScreen
import com.replica.cleaner.ui.screens.RedeemSubscriptionScreen
import com.replica.cleaner.ui.screens.ActivationCodeScreen
import com.replica.cleaner.ui.screens.AdvancedIssuesScreen
import com.replica.cleaner.ui.screens.CleaningResultsScreen
import com.replica.cleaner.ui.screens.ScanningScreen
import com.replica.cleaner.ui.screens.SettingsRoutes
import com.replica.cleaner.ui.screens.SettingsScreen
import com.replica.cleaner.ui.screens.SignInScreen
import com.replica.cleaner.ui.screens.SleepModeScreen
import com.replica.cleaner.ui.screens.SpaceCleanedScreen
import com.replica.cleaner.ui.screens.SplashScreen
import com.replica.cleaner.ui.screens.StorageScreen
import com.replica.cleaner.ui.screens.SystemInfoScreen
import com.replica.cleaner.ui.screens.ThemesScreen
import com.replica.cleaner.ui.screens.TipsScreen
import com.replica.cleaner.ui.screens.ToolsScreen
import com.replica.cleaner.ui.screens.VideoOptimizerScreen

private val settingsRoutes = SettingsRoutes(
    quickClean = Routes.SETTINGS_QUICK_CLEAN,
    analysis = Routes.SETTINGS_ANALYSIS,
    notifications = Routes.SETTINGS_NOTIFICATIONS,
    realtime = Routes.SETTINGS_REALTIME,
    cloud = Routes.SETTINGS_CLOUD,
    privacy = Routes.SETTINGS_PRIVACY,
    language = Routes.SETTINGS_LANGUAGE
)

@Composable
fun AppNavHost(
    vm: CleanerViewModel,
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomTabs
    val premium by vm.premium.collectAsStateWithLifecycle()
    val showHomeBanner = currentRoute == Routes.HOME && !premium
    val context = LocalContext.current
    val activity = context.findActivity()
    var pendingAfterAd by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        InterstitialAdManager.preload(context)
        // NavHost must not steal the system back button — we handle it below.
        navController.enableOnBackPressed(false)
    }

    /** Open a feature — always the same large full-screen ad. */
    fun goFeature(route: String, builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {}) {
        FeatureTransitionAds.run(
            premium = premium,
            targetRoute = route,
            showOverlay = { action -> pendingAfterAd = action },
            action = { navController.navigate(route, builder) }
        )
    }

    /** Leave a feature (toolbar / system back) — large ad then pop. */
    fun leaveFeature() {
        FeatureTransitionAds.runOnExit(
            premium = premium,
            currentRoute = currentRoute,
            showOverlay = { action -> pendingAfterAd = action },
            action = { navController.popBackStack() }
        )
    }

    /** Tools / Storage / Account system back → large ad → Home. */
    fun leaveTabToHome() {
        FeatureTransitionAds.runOnExit(
            premium = premium,
            currentRoute = currentRoute,
            showOverlay = { action -> pendingAfterAd = action },
            action = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
            ) {
            // ---- first run (resets when user clears app data) ----
            composable(Routes.SPLASH) {
                SplashScreen(vm) { splashConsented, splashOnboarded ->
                    val next = when {
                        !splashConsented -> Routes.GET_STARTED
                        !splashOnboarded -> Routes.ONBOARDING
                        else -> Routes.HOME
                    }
                    navController.navigate(next) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }

            composable(Routes.GET_STARTED) {
                GetStartedScreen(
                    onGetStarted = {
                        navController.navigate(Routes.UPDATING_DB) {
                            popUpTo(Routes.GET_STARTED) { inclusive = true }
                        }
                    },
                    onAlreadyPurchased = {
                        navController.navigate(Routes.PREMIUM_GATE)
                    }
                )
            }

            composable(Routes.UPDATING_DB) {
                UpdatingDatabaseScreen {
                    navController.navigate(Routes.INTRO) {
                        popUpTo(Routes.UPDATING_DB) { inclusive = true }
                    }
                }
            }

            composable(Routes.INTRO) {
                IntroCarouselScreen {
                    navController.navigate(Routes.PREMIUM_GATE) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                    }
                }
            }

            composable(Routes.PREMIUM_GATE) {
                PremiumScreen(
                    vm = vm,
                    firstRun = true,
                    onClose = {
                        // Match recording: after plans → START HERE (consent/continue-with-ads after first clean).
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.PREMIUM_GATE) { inclusive = true }
                        }
                    },
                    onAlreadyPurchased = {
                        navController.navigate(Routes.REDEEM_SUBSCRIPTION)
                    }
                )
            }

            composable(Routes.CONSENT) {
                ConsentScreen(
                    onContinue = {
                        vm.setConsented()
                        vm.setOnboarded()
                        navController.navigate(Routes.HOME) {
                            popUpTo(0)
                        }
                    },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    vm = vm,
                    onSeeResults = { navController.navigate(Routes.PERMISSIONS) },
                    onClose = {
                        vm.setOnboarded()
                        navController.navigate(Routes.CONTINUE_ADS) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(Routes.PERMISSIONS) { entry ->
                val scanDone by entry.savedStateHandle
                    .getStateFlow("scan_done", false)
                    .collectAsStateWithLifecycle()
                PermissionsScreen(
                    vm = vm,
                    scanDone = scanDone,
                    onGiveAccess = { navController.navigate(Routes.PERMISSION_NEEDED) },
                    onScanJunk = { navController.navigate(Routes.SCANNING) },
                    onSeeResults = {
                        navController.navigate(Routes.QUICK_CLEAN) {
                            popUpTo(Routes.PERMISSIONS) { inclusive = true }
                        }
                    },
                    onSkip = { navController.navigate(Routes.SCANNING) },
                    onClose = {
                        vm.setOnboarded()
                        navController.navigate(Routes.CONTINUE_ADS) { popUpTo(0) }
                    }
                )
            }

            composable(Routes.PERMISSION_NEEDED) {
                PermissionNeededScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onGranted = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.SCANNING) {
                ScanningScreen(vm) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scan_done", true)
                    navController.popBackStack()
                }
            }

            composable(Routes.READY) {
                ReadyScreen(
                    vm = vm,
                    onSeeResults = {
                        navController.navigate(Routes.QUICK_CLEAN) {
                            popUpTo(Routes.READY) { inclusive = true }
                        }
                    },
                    onClose = {
                        vm.setOnboarded()
                        navController.navigate(Routes.CONTINUE_ADS) { popUpTo(0) }
                    }
                )
            }

            // ---- bottom tabs ----
            composable(Routes.HOME) {
                val homeCtx = LocalContext.current
                HomeScreen(
                    vm = vm,
                    onQuickClean = { goFeature(Routes.QUICK_CLEAN) },
                    onTips = { goFeature(Routes.TIPS) },
                    onMedia = { goFeature(Routes.MEDIA_OVERVIEW) },
                    onApps = { goFeature(Routes.APPS_GATE) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onAutoCleaning = { goFeature(Routes.AUTO_CLEANING) },
                    onCustomize = { navController.navigate(Routes.SETTINGS_QUICK_CLEAN) },
                    onSleepMode = { goFeature(Routes.featureDetail("sleep_mode")) },
                    onInstallAntivirus = { vm.openPlayStore(homeCtx, "com.antivirus") },
                    onInstallVpn = { vm.openPlayStore(homeCtx, "com.avg.android.vpn") }
                )
            }

            composable(Routes.TOOLS) {
                ToolsScreen(
                    vm = vm,
                    onQuickClean = { goFeature(Routes.QUICK_CLEAN) },
                    onAutoCleaning = { goFeature(Routes.AUTO_CLEANING) },
                    onCloudTransfers = { goFeature(Routes.CLOUD_TRANSFERS) },
                    onSystemInfo = { goFeature(Routes.SYSTEM_INFO) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onFeature = { id ->
                        when (id) {
                            "deep_clean", "browser_cleaner" ->
                                goFeature(Routes.featureUpsell(id))
                            else ->
                                goFeature(Routes.featureDetail(id))
                        }
                    }
                )
            }

            composable(Routes.STORAGE) {
                StorageScreen(
                    vm = vm,
                    onTips = { goFeature(Routes.TIPS) },
                    onApps = { goFeature(Routes.APPS_GATE) },
                    onOpen = { kind -> goFeature(Routes.storageGate(kind)) }
                )
            }

            composable(Routes.ACCOUNT) {
                AccountScreen(
                    vm = vm,
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onSignIn = { navController.navigate(Routes.SIGN_IN) },
                    onRedeem = { navController.navigate(Routes.REDEEM_SUBSCRIPTION) },
                    onFeatures = { navController.navigate(Routes.PREMIUM_FEATURES) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onThemes = { goFeature(Routes.THEMES) },
                    onAbout = { goFeature(Routes.ABOUT) }
                )
            }

            // ---- cleaning ----
            composable(Routes.QUICK_CLEAN) {
                QuickCleanScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onSettings = { navController.navigate(Routes.SETTINGS_QUICK_CLEAN) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onCleaning = {
                        navController.navigate(Routes.CLEANING) {
                            popUpTo(Routes.QUICK_CLEAN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CLEANING) {
                CleaningScreen(vm) {
                    navController.navigate(Routes.ADVANCED_ISSUES) {
                        popUpTo(Routes.CLEANING) { inclusive = true }
                    }
                }
            }

            composable(Routes.ADVANCED_ISSUES) {
                AdvancedIssuesScreen(
                    vm = vm,
                    onDetailedResults = { navController.navigate(Routes.CLEANING_RESULTS) },
                    onResolveAll = { navController.navigate(Routes.PREMIUM_RESOLVE) },
                    onSkip = {
                        navController.navigate(Routes.SPACE_CLEANED) {
                            popUpTo(Routes.ADVANCED_ISSUES) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.navigate(Routes.SPACE_CLEANED) {
                            popUpTo(Routes.ADVANCED_ISSUES) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CLEANING_RESULTS) {
                CleaningResultsScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.PREMIUM_RESOLVE) {
                PremiumScreen(
                    vm = vm,
                    onClose = {
                        navController.navigate(Routes.SPACE_CLEANED) {
                            popUpTo(Routes.PREMIUM_RESOLVE) { inclusive = true }
                        }
                    },
                    onAlreadyPurchased = {
                        navController.navigate(Routes.REDEEM_SUBSCRIPTION)
                    }
                )
            }

            composable(Routes.SPACE_CLEANED) {
                val onboarded by vm.prefs.onboarded.collectAsStateWithLifecycle(initialValue = false)
                fun leaveSpaceCleaned() {
                    val next = if (onboarded) Routes.HOME else Routes.CONTINUE_ADS
                    navController.navigate(next) { popUpTo(0) }
                }
                SpaceCleanedScreen(
                    vm = vm,
                    onGoToDashboard = { leaveSpaceCleaned() },
                    onClose = { leaveSpaceCleaned() }
                )
            }

            composable(Routes.CONTINUE_ADS) {
                ConsentScreen(
                    onContinue = {
                        vm.setConsented()
                        vm.setOnboarded()
                        navController.navigate(Routes.HOME) { popUpTo(0) }
                    },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            // ---- media ----
            composable(
                route = Routes.STORAGE_GATE,
                arguments = listOf(navArgument("kind") { type = NavType.StringType })
            ) { entry ->
                val kind = entry.arguments?.getString("kind") ?: "photos"
                ContentGateScreen(
                    vm = vm,
                    kind = kind,
                    onDone = {
                        goFeature(Routes.photoGrid(kind)) {
                            popUpTo(Routes.STORAGE) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onRemoveAds = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(Routes.MEDIA_OVERVIEW) {
                MediaOverviewScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onOpen = { goFeature(Routes.photoGrid(it)) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(
                route = Routes.PHOTO_GRID,
                arguments = listOf(navArgument("kind") { type = NavType.StringType })
            ) { entry ->
                MediaGridScreen(
                    vm = vm,
                    kind = entry.arguments?.getString("kind") ?: "photos",
                    onBack = { leaveFeature() }
                )
            }

            // ---- apps ----
            composable(Routes.APPS_GATE) {
                AppsGateScreen(
                    vm = vm,
                    onDone = {
                        goFeature(Routes.appList("all")) {
                            popUpTo(Routes.STORAGE) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onRemoveAds = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(Routes.APPS_OVERVIEW) {
                AppsOverviewScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onOpenList = { goFeature(Routes.appList(it)) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(
                route = Routes.APP_LIST,
                arguments = listOf(navArgument("kind") { type = NavType.StringType })
            ) { entry ->
                AppListScreen(
                    vm = vm,
                    kind = entry.arguments?.getString("kind") ?: "installed",
                    onBack = { leaveFeature() }
                )
            }

            // ---- tips ----
            composable(Routes.TIPS) {
                TipsScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onQuickClean = { goFeature(Routes.QUICK_CLEAN) },
                    onOpenMedia = { goFeature(Routes.photoGrid(it)) },
                    onOpenApps = { goFeature(Routes.appList(it)) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(Routes.SYSTEM_INFO) {
                SystemInfoScreen(vm) { leaveFeature() }
            }

            composable(Routes.CLOUD_TRANSFERS) {
                CloudTransfersScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onCloudSettings = { navController.navigate(Routes.SETTINGS_CLOUD) }
                )
            }

            // ---- settings ----
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onOpen = { navController.navigate(it) },
                    routes = settingsRoutes
                )
            }

            composable(Routes.SETTINGS_QUICK_CLEAN) {
                QuickCleanSettingsScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(Routes.SETTINGS_ANALYSIS) {
                AnalysisPreferencesScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.SETTINGS_NOTIFICATIONS) {
                NotificationsSettingsScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onCategory = { navController.navigate(Routes.notificationCategory(it)) },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            composable(
                route = Routes.SETTINGS_NOTIFICATION_CATEGORY,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                NotificationCategoryScreen(
                    vm = vm,
                    categoryId = entry.arguments?.getString("id") ?: "junk_cleaning",
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS_REALTIME) {
                RealtimeDetectionScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.SETTINGS_CLOUD) {
                CloudServicesScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.SETTINGS_PRIVACY) {
                PersonalPrivacyScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.SETTINGS_LANGUAGE) {
                LanguageScreen(vm) { navController.popBackStack() }
            }

            composable(Routes.AUTO_CLEANING) {
                AutoCleaningScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onCategory = { navController.navigate(Routes.autoCleanCategory(it)) }
                )
            }

            composable(
                route = Routes.AUTO_CLEAN_CATEGORY,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                AutoCleanCategoryScreen(
                    vm = vm,
                    categoryId = entry.arguments?.getString("id") ?: "junk",
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.THEMES) {
                ThemesScreen(
                    vm = vm,
                    onBack = { leaveFeature() },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) }
                )
            }

            // ---- account ----
            composable(Routes.PREMIUM) {
                PremiumScreen(
                    vm = vm,
                    onClose = { navController.popBackStack() },
                    onAlreadyPurchased = {
                        navController.navigate(Routes.REDEEM_SUBSCRIPTION)
                    }
                )
            }

            composable(Routes.PREMIUM_FEATURES) {
                PremiumFeaturesScreen(
                    onClose = { navController.popBackStack() },
                    onUpgrade = { navController.navigate(Routes.PREMIUM) },
                    onLearnMore = { kind ->
                        when (kind) {
                            FeatureTourKind.AutoCleaning ->
                                goFeature(Routes.AUTO_CLEANING)
                            FeatureTourKind.SleepMode ->
                                goFeature(Routes.featureDetail("sleep_mode"))
                            FeatureTourKind.CustomDashboard,
                            FeatureTourKind.PhotoOptimizer,
                            FeatureTourKind.VideoOptimizer ->
                                navController.navigate(Routes.PREMIUM)
                            else ->
                                goFeature(Routes.featureUpsell(kind.toUpsellId()))
                        }
                    }
                )
            }

            composable(
                route = Routes.FEATURE_UPSELL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: "deep_clean"
                FeatureUpsellScreen(
                    vm = vm,
                    featureId = id,
                    onBack = { leaveFeature() },
                    onAlreadyPurchased = {
                        navController.navigate(Routes.ACCOUNT) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.SIGN_IN) {
                SignInScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onSignedIn = { navController.popBackStack() }
                )
            }

            composable(Routes.REDEEM_SUBSCRIPTION) {
                RedeemSubscriptionScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onUseAccount = { navController.navigate(Routes.SIGN_IN) },
                    onActivationCode = { navController.navigate(Routes.REDEEM_ACTIVATION) }
                )
            }

            composable(Routes.REDEEM_ACTIVATION) {
                ActivationCodeScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onActivated = {
                        // Pop activation + redeem; leave caller (Sleep Mode / Account) on stack.
                        navController.popBackStack(Routes.REDEEM_SUBSCRIPTION, inclusive = true)
                    }
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { leaveFeature() },
                    onLicenses = { navController.navigate(Routes.LICENSES) }
                )
            }

            composable(Routes.LICENSES) {
                LicensesScreen { navController.popBackStack() }
            }

            composable(
                route = Routes.FEATURE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                when (id) {
                    "photo_optimizer" -> PhotoOptimizerScreen(
                        vm = vm,
                        onBack = { leaveFeature() },
                        onUpgrade = { navController.navigate(Routes.PREMIUM) }
                    )
                    "video_optimizer" -> VideoOptimizerScreen(
                        vm = vm,
                        onBack = { leaveFeature() },
                        onUpgrade = { navController.navigate(Routes.PREMIUM) }
                    )
                    "sleep_mode" -> SleepModeScreen(
                        vm = vm,
                        onBack = { leaveFeature() },
                        onUpgrade = { navController.navigate(Routes.PREMIUM) },
                        onAlreadyPurchased = {
                            navController.navigate(Routes.ACCOUNT) {
                                launchSingleTop = true
                            }
                        }
                    )
                    "deep_clean" -> DeepCleanScreen(
                        vm = vm,
                        onBack = { leaveFeature() },
                        onUpgrade = { navController.navigate(Routes.PREMIUM) },
                        onQuickClean = { goFeature(Routes.QUICK_CLEAN) },
                        onAlreadyPurchased = {
                            navController.navigate(Routes.ACCOUNT) {
                                launchSingleTop = true
                            }
                        }
                    )
                    "browser_cleaner" -> BrowserCleanerScreen(
                        vm = vm,
                        onBack = { leaveFeature() },
                        onUpgrade = { navController.navigate(Routes.PREMIUM) },
                        onQuickClean = { goFeature(Routes.QUICK_CLEAN) },
                        onAlreadyPurchased = {
                            navController.navigate(Routes.ACCOUNT) {
                                launchSingleTop = true
                            }
                        }
                    )
                    else -> FeatureDetailScreen(
                        featureId = id,
                        onBack = { leaveFeature() }
                    )
                }
            }
        }

        if (showHomeBanner) {
            HomeBannerAd(modifier = Modifier.fillMaxWidth().height(50.dp))
        }
        if (showBottomBar) {
            CleanerBottomBar(
                currentRoute = currentRoute,
                onSelect = { route ->
                    if (route == currentRoute) return@CleanerBottomBar
                    goFeature(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        }

        // Cover AVG / UPGRADE (and the rest of Home) while any transition ad is up so
        // edge-to-edge + AdMob interstitial cannot leave app chrome peeking on top.
        if (pendingAfterAd != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(20f)
                    .background(Color.Black)
            )
            TransitionAdOverlay(
                onDismiss = {
                    InterstitialAdManager.markShown()
                    val next = pendingAfterAd
                    pendingAfterAd = null
                    next?.invoke()
                }
            )
        }
    }

    // Handle system back AFTER NavHost so Navigation cannot steal the event.
    val onSecondaryTab = currentRoute in FeatureTransitionAds.tabsBackToHome
    val canPop = navController.previousBackStackEntry != null
    BackHandler(enabled = pendingAfterAd == null) {
        when {
            !premium && onSecondaryTab -> leaveTabToHome()
            !premium && FeatureTransitionAds.isExitAdRoute(currentRoute) && canPop -> leaveFeature()
            canPop -> navController.popBackStack()
            else -> activity?.moveTaskToBack(true)
        }
    }
}
