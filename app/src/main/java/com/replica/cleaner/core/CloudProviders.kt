package com.replica.cleaner.core

import android.content.Context
import android.content.Intent
import android.net.Uri

/** IDs stored in Prefs.connectedCloudProviders. */
object CloudProviders {
    const val DROPBOX = "dropbox"
    const val GOOGLE_DRIVE = "google_drive"

    data class Provider(
        val id: String,
        val displayName: String,
        val packageName: String,
        val webUrl: String,
        val loginUrl: String
    )

    val Dropbox = Provider(
        id = DROPBOX,
        displayName = "Dropbox",
        packageName = "com.dropbox.android",
        webUrl = "https://www.dropbox.com",
        loginUrl = "https://www.dropbox.com/login"
    )

    val GoogleDrive = Provider(
        id = GOOGLE_DRIVE,
        displayName = "Google Drive",
        packageName = "com.google.android.apps.docs",
        webUrl = "https://drive.google.com",
        loginUrl = "https://accounts.google.com/ServiceLogin?service=wise&continue=https%3A%2F%2Fdrive.google.com"
    )

    fun all(): List<Provider> = listOf(Dropbox, GoogleDrive)

    fun byId(id: String): Provider? = all().find { it.id == id }

    /**
     * Opens the provider app if installed, otherwise the login/web page.
     * Returns true if an activity was started.
     */
    fun openConnect(context: Context, provider: Provider): Boolean {
        val pm = context.packageManager
        val appIntent = pm.getLaunchIntentForPackage(provider.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val login = Intent(Intent.ACTION_VIEW, Uri.parse(provider.loginUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val web = Intent(Intent.ACTION_VIEW, Uri.parse(provider.webUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${provider.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        return when {
            appIntent != null -> Permissions.safeStart(context, appIntent, login)
            else -> Permissions.safeStart(context, login, web) ||
                Permissions.safeStart(context, market, web)
        }
    }
}
