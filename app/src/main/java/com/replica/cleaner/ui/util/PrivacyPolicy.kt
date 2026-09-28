package com.replica.cleaner.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.replica.cleaner.R

/** Opens the app privacy policy URL when the receiver is clicked. */
@Composable
fun Modifier.clickOpenPrivacyPolicy(): Modifier {
    val uriHandler = LocalUriHandler.current
    val url = LocalContext.current.getString(R.string.privacy_policy_url)
    return this.clickable { uriHandler.openUri(url) }
}
