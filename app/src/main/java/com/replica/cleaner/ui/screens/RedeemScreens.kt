package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.l10n.tr

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Redeem subscription" hub — opened from Sleep Mode tr("Already purchased?")
 * and Account tr("Already have a subscription?").
 */
@Composable
fun RedeemSubscriptionScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onUseAccount: () -> Unit,
    onActivationCode: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val context = LocalContext.current
    val l10n = LocalL10n.current
    val scope = rememberCoroutineScope()
    var restoring by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            CleanerTopBar(tr("Redeem subscription"), onBack = onBack, centered = false)

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                RedeemOptionCard(
                    icon = Icons.Default.Person,
                    title = tr("Use your AVG Account"),
                    body = tr("If your subscription is linked to this account, it will activate automatically."),
                    onClick = onUseAccount
                )
                Spacer(Modifier.height(12.dp))
                RedeemOptionCard(
                    icon = Icons.Default.Key,
                    title = tr("Use an activation code"),
                    body = tr("Enter the code you received after purchase from another device or AVG app."),
                    onClick = onActivationCode
                )
                Spacer(Modifier.height(12.dp))
                RedeemOptionCard(
                    icon = null,
                    title = tr("Restore from Google Play"),
                    body = tr("If you've purchased a subscription from Google Play, this can restore it."),
                    leading = {
                        Text(
                            "G",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        restoring = true
                        scope.launch {
                            delay(1600)
                            restoring = false
                            android.widget.Toast
                                .makeText(
                                    context,
                                    l10n.tr("No Google Play purchases found"),
                                    android.widget.Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                )

                Spacer(Modifier.height(28.dp))
                Text(
                    text = buildAnnotatedString {
                        append(tr("Get more info about AVG subscriptions and how to redeem them "))
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(tr("in our FAQ"))
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://support.avg.com/")
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        if (restoring) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.card)
                        .padding(horizontal = 36.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(tr("Restoring purchases..."), color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun ActivationCodeScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onActivated: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val l10n = LocalL10n.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var processing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            CleanerTopBar(tr("Activation code"), onBack = onBack, centered = false)

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    tr("Enter the activation code from your purchase receipt or another AVG product."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        error = null
                    },
                    label = { Text(tr("Activation code")) },
                    singleLine = true,
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = colors.divider,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = colors.textSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = colors.danger, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = tr("ACTIVATE"),
                    enabled = !processing,
                    onClick = {
                        val trimmed = code.trim()
                        when {
                            trimmed.length < 6 ->
                                error = l10n.tr("Enter a valid activation code.")
                            else -> {
                                processing = true
                                scope.launch {
                                    delay(1200)
                                    // Demo: codes containing PREMIUM unlock; others fail.
                                    if (trimmed.contains("PREMIUM") || trimmed == "AVG-DEMO") {
                                        vm.setPremium(true)
                                        processing = false
                                        android.widget.Toast
                                            .makeText(
                                                context,
                                                l10n.tr("Subscription activated"),
                                                android.widget.Toast.LENGTH_SHORT
                                            )
                                            .show()
                                        onActivated()
                                    } else {
                                        processing = false
                                        error = l10n.tr("This code isn't valid or has already been used.")
                                    }
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    tr("Demo tip: use code AVG-DEMO to unlock Premium."),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        if (processing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.card)
                        .padding(horizontal = 36.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(tr("Activating..."), color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun RedeemOptionCard(
    icon: ImageVector?,
    title: String,
    body: String,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    val colors = LocalCleanerColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.divider.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (leading != null) leading()
            else if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textSecondary
        )
    }
}
