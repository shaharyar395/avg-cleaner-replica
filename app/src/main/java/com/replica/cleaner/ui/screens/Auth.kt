package com.replica.cleaner.ui.screens

import android.accounts.AccountManager
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.l10n.tr
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.util.clickOpenPrivacyPolicy
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.Divider
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AuthStep {
    SignIn,
    ForgotEmail,
    ForgotCreatePassword,
    SignUpEmail,
    SignUpCreatePassword
}

/**
 * Sign In + Forgot password + Sign up + Google account picker.
 * Local account storage (no remote AVG SSO) — UI/flow matches the real app recording.
 */
@Composable
fun SignInScreen(
    vm: CleanerViewModel,
    onBack: () -> Unit,
    onSignedIn: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val l10n = LocalL10n.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedEmail by vm.prefs.email.collectAsStateWithLifecycle(initialValue = null)

    var step by remember { mutableStateOf(AuthStep.SignIn) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showGooglePicker by remember { mutableStateOf(false) }

    val addAccountLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        showGooglePicker = true
    }

    fun validEmail(value: String) = value.contains("@") && value.contains(".")

    fun openGooglePicker() {
        showGooglePicker = true
    }

    fun completeGoogleSignIn(accountEmail: String) {
        busy = true
        scope.launch {
            delay(500)
            vm.signIn(accountEmail, provider = "google")
            busy = false
            showGooglePicker = false
            onSignedIn()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (step) {
            AuthStep.SignIn -> {
                if (storedEmail != null) {
                    SignedInPanel(
                        email = storedEmail!!,
                        onSignOut = {
                            vm.signOut()
                            onBack()
                        },
                        onBack = onBack
                    )
                } else {
                    SignInForm(
                        email = email,
                        password = password,
                        showPassword = showPassword,
                        error = error,
                        busy = busy,
                        onEmail = { email = it; error = null },
                        onPassword = { password = it; error = null },
                        onTogglePassword = { showPassword = !showPassword },
                        onContinue = {
                            when {
                                !validEmail(email) ->
                                    error = l10n.tr("Enter a valid email address.")
                                password.length < 6 ->
                                    error = l10n.tr("Password must be at least 6 characters.")
                                else -> {
                                    busy = true
                                    scope.launch {
                                        val ok = vm.verifyPassword(email.trim(), password)
                                        busy = false
                                        if (ok) onSignedIn()
                                        else error = l10n.tr("Incorrect email or password.")
                                    }
                                }
                            }
                        },
                        onForgot = {
                            error = null
                            password = ""
                            step = AuthStep.ForgotEmail
                        },
                        onSignUp = {
                            error = null
                            password = ""
                            passwordConfirm = ""
                            step = AuthStep.SignUpEmail
                        },
                        onGoogle = { openGooglePicker() },
                        onBack = onBack
                    )
                }
            }

            AuthStep.ForgotEmail -> {
                SsoCardScreen(
                    title = tr("Need a new password?"),
                    body = tr("We'll email you to confirm you're really you. Then you'll create a new password."),
                    email = email,
                    onEmail = { email = it; error = null },
                    error = error,
                    busy = busy,
                    primaryLabel = tr("Continue"),
                    onPrimary = {
                        if (!validEmail(email)) {
                            error = l10n.tr("Enter a valid email address.")
                        } else {
                            busy = true
                            scope.launch {
                                delay(700)
                                busy = false
                                Toast.makeText(
                                    context,
                                    l10n.tr("Check your email to continue, then set a new password."),
                                    Toast.LENGTH_LONG
                                ).show()
                                password = ""
                                passwordConfirm = ""
                                step = AuthStep.ForgotCreatePassword
                            }
                        }
                    },
                    secondaryLabel = tr("Go to sign-in"),
                    onSecondary = {
                        error = null
                        step = AuthStep.SignIn
                    },
                    onBack = { step = AuthStep.SignIn }
                )
            }

            AuthStep.ForgotCreatePassword -> {
                CreatePasswordScreen(
                    title = tr("Create a new password"),
                    body = tr("Choose a new password for %s").replace("%s", email.trim()),
                    password = password,
                    passwordConfirm = passwordConfirm,
                    showPassword = showPassword,
                    error = error,
                    busy = busy,
                    onPassword = { password = it; error = null },
                    onPasswordConfirm = { passwordConfirm = it; error = null },
                    onTogglePassword = { showPassword = !showPassword },
                    onContinue = {
                        when {
                            password.length < 6 ->
                                error = l10n.tr("Password must be at least 6 characters.")
                            password != passwordConfirm ->
                                error = l10n.tr("Passwords do not match.")
                            else -> {
                                busy = true
                                scope.launch {
                                    delay(600)
                                    vm.resetPassword(email.trim(), password)
                                    busy = false
                                    Toast.makeText(
                                        context,
                                        l10n.tr("Password updated. You can sign in now."),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    password = ""
                                    passwordConfirm = ""
                                    step = AuthStep.SignIn
                                }
                            }
                        }
                    },
                    onBack = { step = AuthStep.ForgotEmail }
                )
            }

            AuthStep.SignUpEmail -> {
                SignUpEmailScreen(
                    email = email,
                    error = error,
                    busy = busy,
                    onEmail = { email = it; error = null },
                    onContinue = {
                        if (!validEmail(email)) {
                            error = l10n.tr("Enter a valid email address.")
                        } else {
                            busy = true
                            scope.launch {
                                delay(500)
                                busy = false
                                password = ""
                                passwordConfirm = ""
                                step = AuthStep.SignUpCreatePassword
                            }
                        }
                    },
                    onGoSignIn = {
                        error = null
                        step = AuthStep.SignIn
                    },
                    onGoogle = { openGooglePicker() },
                    onApple = {
                        Toast.makeText(
                            context,
                            l10n.tr("Sign in with Apple isn’t available on Android."),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onBack = { step = AuthStep.SignIn }
                )
            }

            AuthStep.SignUpCreatePassword -> {
                CreatePasswordScreen(
                    title = tr("Create your password"),
                    body = tr("Finish activating %s").replace("%s", email.trim()),
                    password = password,
                    passwordConfirm = passwordConfirm,
                    showPassword = showPassword,
                    error = error,
                    busy = busy,
                    onPassword = { password = it; error = null },
                    onPasswordConfirm = { passwordConfirm = it; error = null },
                    onTogglePassword = { showPassword = !showPassword },
                    onContinue = {
                        when {
                            password.length < 6 ->
                                error = l10n.tr("Password must be at least 6 characters.")
                            password != passwordConfirm ->
                                error = l10n.tr("Passwords do not match.")
                            else -> {
                                busy = true
                                scope.launch {
                                    delay(600)
                                    vm.registerAccount(email.trim(), password)
                                    busy = false
                                    onSignedIn()
                                }
                            }
                        }
                    },
                    onBack = { step = AuthStep.SignUpEmail }
                )
            }
        }

        if (showGooglePicker) {
            GoogleAccountPickerDialog(
                onDismiss = { showGooglePicker = false },
                onPick = { completeGoogleSignIn(it) },
                onAddAccount = {
                    showGooglePicker = false
                    addAccountLauncher.launch(
                        Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                        }
                    )
                }
            )
        }

        if (busy && step == AuthStep.SignIn && storedEmail == null) {
            // Light overlay only for sign-in submit; other steps show inline progress.
        }
    }
}

@Composable
private fun SignedInPanel(email: String, onSignOut: () -> Unit, onBack: () -> Unit) {
    val colors = LocalCleanerColors.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(tr("Account"), onBack = onBack, centered = false)
        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(tr("Signed in as"), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(email, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(32.dp))
            SecondaryButton(tr("SIGN OUT"), onClick = onSignOut)
        }
    }
}

@Composable
private fun SignInForm(
    email: String,
    password: String,
    showPassword: Boolean,
    error: String?,
    busy: Boolean,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onContinue: () -> Unit,
    onForgot: () -> Unit,
    onSignUp: () -> Unit,
    onGoogle: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        CleanerTopBar(tr("Sign In"), onBack = onBack, centered = false)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            AuthField(
                value = email,
                onValueChange = onEmail,
                label = tr("Email"),
                keyboardType = KeyboardType.Email,
                isError = error != null
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = password,
                onValueChange = onPassword,
                label = tr("Password"),
                keyboardType = KeyboardType.Password,
                isError = error != null,
                password = true,
                showPassword = showPassword,
                onTogglePassword = onTogglePassword
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error, style = MaterialTheme.typography.bodySmall, color = colors.danger)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton(tr("CONTINUE"), onClick = onContinue, enabled = !busy)
            Spacer(Modifier.height(16.dp))
            Text(
                text = tr("Forgot password?"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgot)
                    .padding(8.dp)
            )
            Text(
                text = tr("Sign up"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSignUp)
                    .padding(8.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(Modifier.weight(1f))
                Text("  OR  ", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Divider(Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            GoogleSignInButton(onClick = onGoogle, enabled = !busy)
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Light AVG SSO-style card (forgot password / activate). */
@Composable
private fun SsoCardScreen(
    title: String,
    body: String,
    email: String,
    onEmail: (String) -> Unit,
    error: String?,
    busy: Boolean,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "←  ${tr("Back")}",
            color = Color(0xFF1565C0),
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        AvgBrandRow()
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmail,
                label = { Text(tr("Email")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFF9E9E9E),
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E),
                    focusedLabelColor = Color(0xFF4285F4),
                    unfocusedLabelColor = Color(0xFF757575),
                    cursorColor = Color(0xFF4285F4)
                )
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF7CB342))
                    .clickable(enabled = !busy, onClick = onPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(primaryLabel, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                secondaryLabel,
                color = Color(0xFF1565C0),
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSecondary)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun SignUpEmailScreen(
    email: String,
    error: String?,
    busy: Boolean,
    onEmail: (String) -> Unit,
    onContinue: () -> Unit,
    onGoSignIn: () -> Unit,
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "←  ${tr("Back")}",
            color = Color(0xFF1565C0),
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp)
        )
        AvgBrandRow()
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Text(
                tr("Activate an account"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text(
                tr("If you've already shopped with us, please use the email address you used at checkout."),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmail,
                label = { Text(tr("Email")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFF9E9E9E),
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E),
                    focusedLabelColor = Color(0xFF4285F4),
                    unfocusedLabelColor = Color(0xFF757575),
                    cursorColor = Color(0xFF4285F4)
                )
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("By clicking 'Continue', you agree with our "))
                    withStyle(SpanStyle(color = Color(0xFF1565C0), textDecoration = TextDecoration.Underline)) {
                        append(tr("License Agreement"))
                    }
                    append(tr(" and "))
                    withStyle(SpanStyle(color = Color(0xFF1565C0), textDecoration = TextDecoration.Underline)) {
                        append(tr("Privacy Policy"))
                    }
                    append(".")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                modifier = Modifier.clickOpenPrivacyPolicy()
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF7CB342))
                    .clickable(enabled = !busy, onClick = onContinue),
                contentAlignment = Alignment.Center
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(tr("Continue"), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                tr("Go to sign-in"),
                color = Color(0xFF1565C0),
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGoSignIn)
                    .padding(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = Color(0xFFDDDDDD))
                Text("  or  ", color = Color(0xFF888888), fontSize = 13.sp)
                HorizontalDivider(Modifier.weight(1f), color = Color(0xFFDDDDDD))
            }
            Spacer(Modifier.height(14.dp))
            SocialOutlineButton(
                label = tr("Continue with Google"),
                leading = { GoogleGMark(18.dp) },
                onClick = onGoogle
            )
            Spacer(Modifier.height(10.dp))
            SocialOutlineButton(
                label = tr("Continue with Apple"),
                leading = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onApple
            )
        }
    }
}

@Composable
private fun CreatePasswordScreen(
    title: String,
    body: String,
    password: String,
    passwordConfirm: String,
    showPassword: Boolean,
    error: String?,
    busy: Boolean,
    onPassword: (String) -> Unit,
    onPasswordConfirm: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "←  ${tr("Back")}",
            color = Color(0xFF1565C0),
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp)
        )
        AvgBrandRow()
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPassword,
                label = { Text(tr("Password")) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = onTogglePassword)
                    )
                },
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFF9E9E9E),
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E)
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = onPasswordConfirm,
                label = { Text(tr("Confirm password")) },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4285F4),
                    unfocusedBorderColor = Color(0xFF9E9E9E),
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E)
                )
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF7CB342))
                    .clickable(enabled = !busy, onClick = onContinue),
                contentAlignment = Alignment.Center
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(tr("Continue"), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GoogleAccountPickerDialog(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
    onAddAccount: () -> Unit
) {
    val context = LocalContext.current
    val accounts = remember {
        runCatching {
            AccountManager.get(context).getAccountsByType("com.google").map { it.name }
        }.getOrDefault(emptyList())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 20.dp)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.replica.cleaner.R.drawable.ic_app_brand),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                tr("Choose an account"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                tr("to continue to AVG Cleaner"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            if (accounts.isEmpty()) {
                Text(
                    tr("No Google accounts on this device."),
                    color = Color(0xFF5F6368),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            } else {
                accounts.forEach { accountEmail ->
                    val initial = accountEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(accountEmail) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7E57C2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                accountEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                                color = Color(0xFF202124),
                                fontWeight = FontWeight.Medium
                            )
                            Text(accountEmail, color = Color(0xFF5F6368), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddAccount)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF5F6368))
                }
                Spacer(Modifier.width(14.dp))
                Text(tr("Add another account"), color = Color(0xFF202124))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("To continue, Google will share your name, email address, and profile picture with AVG Cleaner. Before using this app, review its "))
                    withStyle(SpanStyle(color = Color(0xFF1A73E8))) { append(tr("privacy policy")) }
                    append(tr(" and "))
                    withStyle(SpanStyle(color = Color(0xFF1A73E8))) { append(tr("terms of service")) }
                    append(".")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6368),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickOpenPrivacyPolicy()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(tr("CANCEL"), color = Color(0xFF1A73E8))
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isError: Boolean,
    password: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    val colors = LocalCleanerColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password && !showPassword) PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = if (password) {
            {
                Icon(
                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.clickable(onClick = onTogglePassword)
                )
            }
        } else null,
        isError = isError,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = colors.divider,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = colors.textSecondary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        GoogleGMark(20.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            tr("SIGN IN WITH GOOGLE"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SocialOutlineButton(
    label: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        leading()
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color(0xFF202124), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AvgBrandRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.replica.cleaner.R.drawable.ic_app_brand),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "AVG Cleaner",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            fontSize = 20.sp
        )
    }
}

@Composable
private fun GoogleGMark(size: androidx.compose.ui.unit.Dp) {
    // Compact multicolor "G" mark for the Google button.
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "G",
            color = Color(0xFF4285F4),
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.7f).sp
        )
    }
}
