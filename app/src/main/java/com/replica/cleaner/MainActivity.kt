package com.replica.cleaner

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.replica.cleaner.l10n.AppLanguage
import com.replica.cleaner.l10n.L10n
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.nav.AppNavHost
import com.replica.cleaner.ui.theme.CleanerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hard-lock portrait — do not rotate/tilt with the device.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val vm: CleanerViewModel = viewModel()
            val mode by vm.themeMode.collectAsStateWithLifecycle()
            val accent by vm.accent.collectAsStateWithLifecycle()
            val languageName by vm.prefs.language.collectAsStateWithLifecycle(
                initialValue = AppLanguage.DEFAULT.displayName
            )
            val l10n = remember(languageName) { L10n.forDisplayName(languageName) }

            val lifecycleOwner = LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        vm.refreshPermissions()
                        vm.refreshStorage()
                        vm.reloadMediaIfNeeded()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            CompositionLocalProvider(
                LocalL10n provides l10n,
                LocalLayoutDirection provides l10n.language.layoutDirection
            ) {
                CleanerTheme(mode = mode, accent = accent) {
                    AppNavHost(vm = vm, navController = rememberNavController())
                }
            }
        }
    }
}
