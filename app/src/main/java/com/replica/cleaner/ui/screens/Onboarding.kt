package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.replica.cleaner.core.Permissions
import com.replica.cleaner.core.formatBytes
import com.replica.cleaner.core.formatPercent
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.util.clickOpenPrivacyPolicy
import com.replica.cleaner.ui.components.CleanerCard
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.RingProgress
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.theme.LocalCleanerColors
import com.replica.cleaner.ui.theme.Magenta
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    vm: CleanerViewModel,
    onDone: (consented: Boolean, onboarded: Boolean) -> Unit
) {
    // Brief brand flash, then route by first-run flags (cleared with app data).
    var phase by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val consented = vm.prefs.consented.first()
        val onboarded = vm.prefs.onboarded.first()
        delay(if (consented && onboarded) 700 else 450)
        phase = 1
        delay(if (consented && onboarded) 1400 else 700)
        onDone(consented, onboarded)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(
                    com.replica.cleaner.R.drawable.ic_app_brand
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(if (phase == 0) 96.dp else 72.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            if (phase >= 1) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "AVG Cleaner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(48.dp))
                SplashCleaningIllustration(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 40.dp)
                )
            }
        }
    }
}

/** Real AVG clear-data first screen: legal + GET STARTED. */
@Composable
fun GetStartedScreen(
    onGetStarted: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.35f))
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                com.replica.cleaner.R.drawable.ic_app_brand
            ),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "AVG Cleaner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = tr("Using this app and its features requires access to data about your installed apps which we collect and store locally to provide you with tips on space optimization and phone functionality."),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                append(tr("By proceeding, you confirm you accept AVG's "))
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) { append(tr("Agreement")) }
                append(tr(" and "))
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) { append(tr("Privacy Policy")) }
                append(".")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickOpenPrivacyPolicy()
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(tr("GET STARTED"), onGetStarted)
        Spacer(Modifier.height(14.dp))
        Text(
            text = tr("ALREADY PURCHASED?"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .clickable(onClick = onAlreadyPurchased)
                .padding(12.dp)
        )
        Spacer(Modifier.height(20.dp))
    }
}

/** "Updating cleaning database..." — shown once after GET STARTED. */
@Composable
fun UpdatingDatabaseScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onDone()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tr("Updating cleaning database..."),
                style = MaterialTheme.typography.titleMedium,
                color = LocalCleanerColors.current.textSecondary
            )
            Spacer(Modifier.height(20.dp))
            androidx.compose.material3.CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

private data class IntroSlide(val title: String, val kind: Int)

private const val INTRO_SLIDE_MS = 3_000L

/**
 * 4-step intro matching real AVG after clear data:
 * auto-advances every ~3s with story-style dash fill, tap also skips ahead,
 * then navigates to the premium plans gate.
 */
@Composable
fun IntroCarouselScreen(onFinished: () -> Unit) {
    val slides = listOf(
        IntroSlide(
            tr("Your phone can quickly fill up with unnecessary data"),
            0
        ),
        IntroSlide(
            tr("We can help you free up space by cleaning your phone..."),
            1
        ),
        IntroSlide(
            tr("...and free up resources by putting unused apps to sleep"),
            2
        ),
        IntroSlide(
            tr("Get access to all our premium features and say goodbye to ads!"),
            3
        )
    )
    var page by remember { mutableIntStateOf(0) }
    val colors = LocalCleanerColors.current
    val accent = MaterialTheme.colorScheme.primary
    val segmentProgress = remember { Animatable(0f) }

    fun goNext() {
        if (page < slides.lastIndex) {
            page++
        } else {
            onFinished()
        }
    }

    // Auto-advance like AVG (tap still works via clickable below).
    LaunchedEffect(page) {
        segmentProgress.snapTo(0f)
        segmentProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = INTRO_SLIDE_MS.toInt(), easing = LinearEasing)
        )
        goNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { goNext() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.width(132.dp)
            ) {
                slides.indices.forEach { i ->
                    val fill = when {
                        i < page -> 1f
                        i == page -> segmentProgress.value
                        else -> 0f
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.divider)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(fill.coerceIn(0f, 1f))
                                .height(4.dp)
                                .background(accent)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.28f))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(tween(380)) + slideInHorizontally { it / 10 }) togetherWith
                        (fadeOut(tween(280)) + slideOutHorizontally { -it / 10 })
                },
                label = "introSlide"
            ) { p ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IntroIllustration(
                        kind = slides[p].kind,
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(Modifier.height(36.dp))
                    Text(
                        text = slides[p].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(0.55f))

            // AVG only shows this hint on the first tip; later slides auto-advance.
            if (page == 0) {
                Text(
                    text = tr("Tap to continue"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(28.dp))
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun IntroIllustration(kind: Int, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height
        when (kind) {
            0 -> {
                // Phone + gauge
                val phoneW = w * 0.42f
                val phoneH = h * 0.72f
                val left = (w - phoneW) / 2f
                val top = h * 0.12f
                drawRoundRect(
                    color = line,
                    topLeft = Offset(left, top),
                    size = Size(phoneW, phoneH),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = stroke
                )
                drawArc(
                    color = line,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(left + phoneW * 0.12f, top + phoneH * 0.22f),
                    size = Size(phoneW * 0.76f, phoneH * 0.5f),
                    style = stroke
                )
                drawLine(
                    color = line,
                    start = Offset(w / 2f, top + phoneH * 0.48f),
                    end = Offset(left + phoneW * 0.78f, top + phoneH * 0.28f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
            1 -> {
                // Robot + spray + phone
                drawCircle(
                    color = line,
                    radius = w * 0.16f,
                    center = Offset(w * 0.38f, h * 0.42f),
                    style = stroke
                )
                drawCircle(color = line, radius = 4.dp.toPx(), center = Offset(w * 0.33f, h * 0.4f))
                drawCircle(color = line, radius = 4.dp.toPx(), center = Offset(w * 0.43f, h * 0.4f))
                drawRoundRect(
                    color = line,
                    topLeft = Offset(w * 0.58f, h * 0.22f),
                    size = Size(w * 0.28f, h * 0.55f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = stroke
                )
                drawRoundRect(
                    color = line,
                    topLeft = Offset(w * 0.18f, h * 0.58f),
                    size = Size(w * 0.14f, h * 0.22f),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = stroke
                )
            }
            2 -> {
                // Battery balloon / sleep resources
                drawRoundRect(
                    color = line,
                    topLeft = Offset(w * 0.28f, h * 0.38f),
                    size = Size(w * 0.34f, h * 0.28f),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = stroke
                )
                drawLine(
                    color = line,
                    start = Offset(w * 0.45f, h * 0.38f),
                    end = Offset(w * 0.45f, h * 0.22f),
                    strokeWidth = stroke.width
                )
                drawOval(
                    color = line,
                    topLeft = Offset(w * 0.22f, h * 0.08f),
                    size = Size(w * 0.46f, h * 0.18f),
                    style = stroke
                )
                // Bolt
                val bolt = Path().apply {
                    moveTo(w * 0.48f, h * 0.42f)
                    lineTo(w * 0.4f, h * 0.55f)
                    lineTo(w * 0.47f, h * 0.55f)
                    lineTo(w * 0.42f, h * 0.62f)
                    lineTo(w * 0.55f, h * 0.48f)
                    lineTo(w * 0.48f, h * 0.48f)
                    close()
                }
                drawPath(bolt, color = line, style = stroke)
            }
            else -> {
                // Shield + flex premium
                val shield = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.78f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.55f)
                    quadraticBezierTo(w * 0.5f, h * 0.88f, w * 0.22f, h * 0.55f)
                    lineTo(w * 0.22f, h * 0.22f)
                    close()
                }
                drawPath(shield, color = line, style = stroke)
                drawCircle(
                    color = line,
                    radius = w * 0.08f,
                    center = Offset(w * 0.5f, h * 0.4f),
                    style = stroke
                )
                drawLine(
                    color = line,
                    start = Offset(w * 0.42f, h * 0.55f),
                    end = Offset(w * 0.35f, h * 0.42f),
                    strokeWidth = stroke.width
                )
                drawLine(
                    color = line,
                    start = Offset(w * 0.58f, h * 0.55f),
                    end = Offset(w * 0.68f, h * 0.38f),
                    strokeWidth = stroke.width
                )
            }
        }
    }
}

/** Green line-art phone + trash + spray bottle, matching AVG splash. */
@Composable
private fun SplashCleaningIllustration(modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val phoneW = w * 0.28f
        val phoneH = h * 0.78f
        val phoneLeft = cx - phoneW / 2f
        val phoneTop = h * 0.08f
        val radius = 14.dp.toPx()

        // Phone outline
        drawRoundRect(
            color = line,
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(radius, radius),
            style = stroke
        )
        // Screen inset
        val inset = 8.dp.toPx()
        drawRoundRect(
            color = line,
            topLeft = Offset(phoneLeft + inset, phoneTop + inset * 1.6f),
            size = Size(phoneW - inset * 2, phoneH - inset * 3.2f),
            cornerRadius = CornerRadius(radius * 0.55f, radius * 0.55f),
            style = stroke
        )
        // Home bar
        drawLine(
            color = line,
            start = Offset(cx - phoneW * 0.18f, phoneTop + phoneH - inset * 1.2f),
            end = Offset(cx + phoneW * 0.18f, phoneTop + phoneH - inset * 1.2f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )

        // Trash bin (left)
        val binLeft = phoneLeft - w * 0.22f
        val binTop = phoneTop + phoneH * 0.42f
        val binW = w * 0.14f
        val binH = h * 0.28f
        drawRoundRect(
            color = line,
            topLeft = Offset(binLeft, binTop + binH * 0.18f),
            size = Size(binW, binH * 0.72f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = stroke
        )
        drawLine(
            color = line,
            start = Offset(binLeft - 4.dp.toPx(), binTop + binH * 0.18f),
            end = Offset(binLeft + binW + 4.dp.toPx(), binTop + binH * 0.18f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        // Bubbles in trash
        drawCircle(
            color = line,
            radius = 5.dp.toPx(),
            center = Offset(binLeft + binW * 0.35f, binTop + binH * 0.05f),
            style = stroke
        )
        drawCircle(
            color = line,
            radius = 3.5.dp.toPx(),
            center = Offset(binLeft + binW * 0.7f, binTop + binH * 0.12f),
            style = stroke
        )

        // Spray bottle (right)
        val sprayLeft = phoneLeft + phoneW + w * 0.08f
        val sprayTop = phoneTop + phoneH * 0.28f
        val sprayW = w * 0.12f
        val sprayH = h * 0.38f
        drawRoundRect(
            color = line,
            topLeft = Offset(sprayLeft, sprayTop + sprayH * 0.22f),
            size = Size(sprayW, sprayH * 0.7f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = stroke
        )
        // Neck
        drawRoundRect(
            color = line,
            topLeft = Offset(sprayLeft + sprayW * 0.28f, sprayTop + sprayH * 0.05f),
            size = Size(sprayW * 0.44f, sprayH * 0.2f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = stroke
        )
        // Nozzle pointing toward phone
        drawLine(
            color = line,
            start = Offset(sprayLeft + sprayW * 0.1f, sprayTop + sprayH * 0.12f),
            end = Offset(sprayLeft - w * 0.04f, sprayTop + sprayH * 0.02f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        // Spray mist
        drawCircle(
            color = line,
            radius = 2.5.dp.toPx(),
            center = Offset(sprayLeft - w * 0.05f, sprayTop + sprayH * 0.0f),
            style = stroke
        )
        drawCircle(
            color = line,
            radius = 2.dp.toPx(),
            center = Offset(sprayLeft - w * 0.07f, sprayTop + sprayH * 0.08f),
            style = stroke
        )
    }
}

@Composable
private fun SplashStat(value: String, label: String, accent: Boolean) {
    val colors = LocalCleanerColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (accent) MaterialTheme.colorScheme.primary else colors.textSecondary
        )
    }
}

/** tr("Your trusted phone cleaner") — the ads-vs-upgrade consent gate. */
@Composable
fun ConsentScreen(
    onContinue: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.25f))
        IllustrationTile(size = 180)
        Spacer(Modifier.height(32.dp))
        Text(
            text = tr("Your trusted phone cleaner"),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = tr("Personalized advertising allows us to offer some features for free. If you upgrade, you won't see ads."),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalCleanerColors.current.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = tr("Read more in our Consent Policy."),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalCleanerColors.current.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(tr("CONTINUE WITH ADS"), onContinue)
        Spacer(Modifier.height(12.dp))
        SecondaryButton(tr("UPGRADE"), onUpgrade)
        Spacer(Modifier.height(24.dp))
    }
}

/** First-run tr("START HERE") screen — matches AVG before permissions. */
@Composable
fun OnboardingScreen(
    vm: CleanerViewModel,
    onSeeResults: () -> Unit,
    onClose: () -> Unit
) {
    val storage by vm.storage.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { vm.refreshStorage() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshStorage()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                com.replica.cleaner.R.drawable.ic_app_brand
            ),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "AVG Cleaner",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(36.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${formatPercent(storage.usedFraction)} %",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = tr("Used space"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatBytes(storage.freeBytes),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = tr("Free space"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onSeeResults),
            contentAlignment = Alignment.Center
        ) {
            RingProgress(
                progress = storage.usedFraction.coerceIn(0.08f, 1f),
                modifier = Modifier.size(200.dp),
                strokeWidth = 4.dp
            )
            Box(
                Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.cardHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tr("START HERE"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = tr("We'll guide you through your first cleanup."),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = tr("Skip"),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
            modifier = Modifier
                .clickable(onClick = onClose)
                .padding(16.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}

/**
 * First-run checklist: Give us access → Scan for junk → See results.
 * Access opens an in-app Permission needed screen (system Allow dialog).
 * Scan returns here with both steps ticked, then See results opens Quick Clean.
 */
@Composable
fun PermissionsScreen(
    vm: CleanerViewModel,
    scanDone: Boolean = false,
    onGiveAccess: () -> Unit,
    onScanJunk: () -> Unit,
    onSeeResults: () -> Unit = onScanJunk,
    onSkip: () -> Unit,
    onClose: () -> Unit
) {
    val status by vm.permissions.collectAsStateWithLifecycle()
    val storage by vm.storage.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshPermissions()
                vm.refreshStorage()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val accessDone = status.canScan || status.notifications
    val title = when {
        scanDone && accessDone -> tr("You're all set!")
        accessDone -> tr("One down, one to go!")
        else -> tr("One down, one to go!")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(
            title = "",
            onBack = onClose,
            closeIcon = true,
            centered = false
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SplashCleaningIllustration(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                )
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        formatBytes(storage.freeBytes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(tr("Free space"), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${formatPercent(storage.usedFraction)} %",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(tr("Used space"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(28.dp))

            SetupStepRow(
                number = 1,
                done = accessDone,
                title = tr("Give us access"),
                body = tr("We need permission to clean your photos, media, and files."),
                active = !accessDone,
                onClick = { if (!accessDone) onGiveAccess() }
            )
            Spacer(Modifier.height(18.dp))
            SetupStepRow(
                number = 2,
                done = scanDone,
                title = tr("Scan for junk"),
                body = tr("We'll show you what can be safely removed to free up space."),
                active = accessDone && !scanDone,
                onClick = { if (accessDone && !scanDone) onScanJunk() }
            )

            Spacer(Modifier.height(28.dp))
            when {
                scanDone -> {
                    PrimaryButton(tr("SEE RESULTS"), onSeeResults)
                    Spacer(Modifier.height(12.dp))
                }
                accessDone -> {
                    PrimaryButton(tr("SCAN FOR JUNK"), onScanJunk)
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (!scanDone) {
                SecondaryButton(tr("NOT NOW"), onSkip, Modifier.padding(horizontal = 8.dp))
                Spacer(Modifier.height(24.dp))
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SetupStepRow(
    number: Int,
    done: Boolean,
    title: String,
    body: String,
    active: Boolean,
    onClick: () -> Unit = {}
) {
    val colors = LocalCleanerColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = active || !done, onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when {
                        done || active -> MaterialTheme.colorScheme.primary
                        else -> colors.divider
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF07281A),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    "$number",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color(0xFF07281A) else colors.textSecondary
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (done) colors.textSecondary else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    granted: Boolean,
    buttonText: String,
    hint: String? = null,
    steps: List<String>? = null,
    onClick: () -> Unit
) {
    val colors = LocalCleanerColors.current
    CleanerCard(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else Magenta,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Magenta,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = Magenta, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        if (hint != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        steps?.forEachIndexed { index, step ->
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", fontSize = 11.sp, color = Color(0xFF07281A), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        if (granted) {
            Text(
                text = tr("Granted"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            PrimaryButton(buttonText, onClick)
        }
    }
}

/** The tr("Finding junk…") ring with tips engagement panel. */
@Composable
fun ScanningScreen(vm: CleanerViewModel, onDone: () -> Unit) {
    val scan by vm.scan.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) {
        vm.refreshStorage()
        vm.startScan(force = true)
    }
    LaunchedEffect(scan.running, scan.result, scan.error) {
        if (!scan.running && (scan.result != null || scan.error != null)) {
            delay(400)
            onDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.35f))
        RingProgress(
            progress = scan.progress.coerceAtLeast(0.02f),
            modifier = Modifier.size(170.dp),
            strokeWidth = 5.dp
        ) {
            Text(
                text = "${(scan.progress * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = tr("Finding junk…"),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = scan.label.ifBlank { tr("Scanning device storage") },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = 1
        )
        Spacer(Modifier.weight(0.25f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(colors.card)
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Text(
                tr("Share your thoughts while you wait"),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tr("What cleaning tips are important to you? This won't interrupt your scan."),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(16.dp))
            SecondaryButton(tr("CUSTOMIZE MY TIPS"), onClick = {})
        }
    }
}

/** tr("You're all set and ready to clean!") */
@Composable
fun ReadyScreen(vm: CleanerViewModel, onSeeResults: () -> Unit, onClose: () -> Unit) {
    val storage by vm.storage.collectAsStateWithLifecycle()
    val colors = LocalCleanerColors.current

    LaunchedEffect(Unit) { vm.refreshStorage() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title = "", onBack = onClose, closeIcon = true)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tr("You're all set and ready to clean!"),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SplashCleaningIllustration(
                    modifier = Modifier
                        .width(140.dp)
                        .height(120.dp)
                )
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatBytes(storage.freeBytes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(tr("Free space"), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "${formatPercent(storage.usedFraction)} %",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(tr("Used space"), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            Spacer(Modifier.height(40.dp))
            CheckedRow(tr("Give us access"))
            Spacer(Modifier.height(16.dp))
            CheckedRow(tr("Scan for junk"))
        }
        Column(Modifier.padding(24.dp)) {
            PrimaryButton(tr("SEE RESULTS"), onSeeResults)
        }
    }
}

@Composable
private fun CheckedRow(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Stand-in for the line-art illustrations in the reference app. Kept as a
 * composable so real vector assets can be dropped in one place later.
 */
@Composable
fun IllustrationTile(size: Int, modifier: Modifier = Modifier) {
    val colors = LocalCleanerColors.current
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size((size * 0.45f).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
        )
    }
}
