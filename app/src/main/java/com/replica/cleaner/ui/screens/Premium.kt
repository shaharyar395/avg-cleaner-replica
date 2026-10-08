package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.components.SecondaryButton
import com.replica.cleaner.ui.theme.Amber
import com.replica.cleaner.ui.util.clickOpenPrivacyPolicy
import com.replica.cleaner.ui.theme.DangerRed
import com.replica.cleaner.ui.theme.LocalCleanerColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class PlanFeature(val icon: ImageVector, val title: String, val subtitle: String)

/** Cleaner Premium feature rows — matches AVG paywall recording. */
private val cleanerPremiumFeatures = listOf(
    PlanFeature(Icons.Default.Block, "Remove ads", "No advertisement in this app"),
    PlanFeature(Icons.Default.Visibility, "Deep Clean", "Removes hidden junk files"),
    PlanFeature(Icons.Default.SmartToy, "Auto Cleaning", "Set once, never worry again"),
    PlanFeature(Icons.Default.Language, "Browser Cleaner", "Clean unimportant Google data"),
    PlanFeature(Icons.Default.AcUnit, "Sleep Mode", "Force stops apps you don't use"),
    PlanFeature(Icons.Default.DashboardCustomize, "Custom dashboard", "Get shortcuts to your favorite info"),
    PlanFeature(Icons.Default.GridView, "Photo Optimizer", "Control size and quality of photos"),
    PlanFeature(Icons.Default.Videocam, "Video Optimizer", "Unlock the Video Optimizer and cut your largest files down to size."),
    PlanFeature(Icons.Default.Palette, "Themes", "Make your app match your style"),
    PlanFeature(Icons.Default.SupportAgent, "AVG direct support", "At your service")
)

private val antivirusPremiumFeatures = listOf(
    PlanFeature(Icons.Default.Lock, "App Locking", "Secure your sensitive apps"),
    PlanFeature(Icons.Default.PhotoLibrary, "Photo Vault", "Unlimited protection for personal photos"),
    PlanFeature(Icons.Default.Security, "Scam Protection", "Get warned before tapping risky links")
)

/** Upgrade-plan accordion features — exact 7 Cleanup icons from AVG recording. */
private val cleanupFeatures = listOf(
    PlanFeature(Icons.Default.Block, "Remove ads", "No advertisements in the app"),
    PlanFeature(Icons.Default.Visibility, "Deep Clean", "Removes hidden junk files"),
    PlanFeature(Icons.Default.Language, "Browser Cleaner", "Clean unimportant Google data"),
    PlanFeature(Icons.Default.SmartToy, "Auto Cleaning", "Set once and never worry again"),
    PlanFeature(Icons.Default.AcUnit, "Sleep Mode", "Force stops apps you don't use"),
    PlanFeature(Icons.Default.GridView, "Photo Optimizer", "Control size and quality of photos"),
    PlanFeature(Icons.Default.SupportAgent, "AVG Direct support", "At your service")
)

private val securityFeatures = listOf(
    PlanFeature(Icons.Default.Lock, "App Locking", "Secure your sensitive apps"),
    PlanFeature(Icons.Default.PhotoLibrary, "Photo Vault", "Unlimited protection for personal photos"),
    PlanFeature(Icons.Default.MyLocation, "Scam Protection", "Get warned before tapping risky links")
)

/**
 * @param firstRun Clear-data / intro paywall (YEARLY tabs + white compare cards).
 *                 Upgrade flows use the "Ignore this offer" plans + exclusive 10% on back.
 */
@Composable
fun PremiumScreen(
    vm: CleanerViewModel,
    onClose: () -> Unit,
    onAlreadyPurchased: () -> Unit = {},
    firstRun: Boolean = false
) {
    val colors = LocalCleanerColors.current
    var loading by remember { mutableStateOf(true) }
    var yearly by remember { mutableStateOf(true) }
    var selectedPlan by remember { mutableStateOf("plus") } // plus | premium
    var purchasePhase by remember { mutableStateOf(0) } // 0 plans, 3 plus pitch, 1 processing, 2 playsheet
    var showExclusiveOffer by remember { mutableStateOf(false) }
    var exclusiveCheckout by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var purchaseJob by remember { mutableStateOf<Job?>(null) }

    fun dismissPurchaseUi() {
        purchaseJob?.cancel()
        purchaseJob = null
        purchasePhase = 0
    }

    fun startPurchase() {
        purchaseJob?.cancel()
        purchasePhase = 1
        purchaseJob = scope.launch {
            delay(1600)
            purchasePhase = 2
        }
    }

    fun requestDismiss() {
        if (firstRun) {
            onClose()
        } else if (!showExclusiveOffer) {
            showExclusiveOffer = true
        } else {
            onClose()
        }
    }

    BackHandler(enabled = true) {
        when {
            purchasePhase == 1 || purchasePhase == 2 -> dismissPurchaseUi()
            purchasePhase == 3 -> purchasePhase = 0
            showExclusiveOffer -> onClose()
            else -> requestDismiss()
        }
    }

    LaunchedEffect(Unit) {
        delay(900)
        loading = false
    }

    if (loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
        }
        return
    }

    if (showExclusiveOffer) {
        ExclusiveOfferScreen(
            onClose = onClose,
            onContinue = {
                selectedPlan = "premium"
                exclusiveCheckout = true
                showExclusiveOffer = false
                startPurchase()
            }
        )
        return
    }

    if (purchasePhase == 3) {
        PremiumPlusPitchScreen(
            yearly = yearly,
            onClose = { purchasePhase = 0 },
            onUpgrade = { startPurchase() },
            onContinueWithPremium = {
                selectedPlan = "premium"
                startPurchase()
            }
        )
        return
    }

    val plusYear = "Rs 1,850.00"
    val premYear = "Rs 1,050.00"
    val plusMonth = "Rs 249.00"
    val premMonth = "Rs 199.00"
    val plusPrice = if (yearly) plusYear else plusMonth
    val premPrice = if (yearly) premYear else premMonth
    val periodLabel = if (yearly) "/ year" else "/ month"

    // Upgrade paywall prices (monthly equivalent when yearly).
    val plusMonthEq = if (yearly) "Rs 148.75 / month" else "$plusMonth / month"
    val plusBilled = if (yearly) "Billed Rs 1785 / year" else tr("Billed monthly")
    val premMonthEq = if (yearly) "Rs 87.50 / month" else "$premMonth / month"
    val premBilled = if (yearly) "Billed Rs 1050 / year" else tr("Billed monthly")

    Box(Modifier.fillMaxSize()) {
        if (firstRun) {
            FirstRunPremiumPlans(
                yearly = yearly,
                onYearlyChange = { yearly = it },
                menuOpen = menuOpen,
                onMenuOpenChange = { menuOpen = it },
                onClose = onClose,
                onAlreadyPurchased = onAlreadyPurchased,
                plusPrice = plusPrice,
                premPrice = premPrice,
                periodLabel = periodLabel,
                onUpgradePremium = {
                    selectedPlan = "premium"
                    startPurchase()
                },
                onUpgradePlus = {
                    selectedPlan = "plus"
                    purchasePhase = 3
                }
            )
        } else {
            UpgradePremiumPlans(
                yearly = yearly,
                onYearlyChange = { yearly = it },
                selectedPlan = selectedPlan,
                onSelectPlan = { selectedPlan = it },
                plusMonthEq = plusMonthEq,
                plusBilled = plusBilled,
                premMonthEq = premMonthEq,
                premBilled = premBilled,
                onBack = { requestDismiss() },
                onContinue = {
                    startPurchase()
                }
            )
        }

        if (purchasePhase >= 1) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { dismissPurchaseUi() }
            )
        }

        if (purchasePhase == 1) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* consume */ }
                    .padding(horizontal = 36.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = tr("Processing purchase..."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        }

        if (purchasePhase == 2) {
            val sheetPrice = when {
                exclusiveCheckout -> "Rs 950.00"
                selectedPlan == "plus" -> plusPrice
                else -> premPrice
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                AvgPlayPurchaseSheet(
                    planName = if (selectedPlan == "plus") tr("Premium Plus") else tr("Premium"),
                    price = sheetPrice,
                    period = if (exclusiveCheckout) "/ year" else periodLabel,
                    onDismiss = {
                        exclusiveCheckout = false
                        dismissPurchaseUi()
                    },
                    onConfirm = {
                        purchasePhase = 1
                        scope.launch {
                            delay(900)
                            vm.setPremium(true)
                            exclusiveCheckout = false
                            dismissPurchaseUi()
                            onClose()
                        }
                    }
                )
            }
        }
    }
}

/** Clear-data first-run paywall — YEARLY/MONTHLY underline tabs + white compare cards. */
@Composable
private fun FirstRunPremiumPlans(
    yearly: Boolean,
    onYearlyChange: (Boolean) -> Unit,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onAlreadyPurchased: () -> Unit,
    plusPrice: String,
    premPrice: String,
    periodLabel: String,
    onUpgradePremium: () -> Unit,
    onUpgradePlus: () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = tr("Close"),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.weight(1f))
            Box {
                Box(
                    Modifier
                        .size(44.dp)
                        .clickable { onMenuOpenChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = tr("More"),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { onMenuOpenChange(false) },
                    offset = DpOffset(x = (-8).dp, y = 0.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = tr("Already purchased?"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onMenuOpenChange(false)
                            onAlreadyPurchased()
                        }
                    )
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onYearlyChange(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tr("YEARLY"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (yearly) Color.White else colors.textSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = tr("Best deal"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Amber)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .width(72.dp)
                            .height(3.dp)
                            .background(if (yearly) Color.White else Color.Transparent)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onYearlyChange(false) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tr("MONTHLY"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!yearly) Color.White else colors.textSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .width(72.dp)
                            .height(3.dp)
                            .background(if (!yearly) Color.White else Color.Transparent)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvgPlanCompareCard(
                    modifier = Modifier.weight(1f),
                    title = tr("Premium"),
                    includes = listOf(tr("Cleaner Premium")),
                    price = premPrice,
                    period = periodLabel,
                    primaryCta = false,
                    onUpgrade = onUpgradePremium
                )
                AvgPlanCompareCard(
                    modifier = Modifier.weight(1f),
                    title = tr("Premium Plus"),
                    includes = listOf(tr("Cleaner Premium"), tr("AntiVirus Premium")),
                    price = plusPrice,
                    period = periodLabel,
                    primaryCta = true,
                    onUpgrade = onUpgradePlus
                )
            }

            Spacer(Modifier.height(18.dp))

            AvgFeatureSectionCard(
                title = tr("Cleaner Premium"),
                features = cleanerPremiumFeatures,
                footer = null
            )

            Spacer(Modifier.height(12.dp))

            AvgFeatureSectionCard(
                title = tr("AntiVirus Premium"),
                features = antivirusPremiumFeatures,
                footer = tr("Available with AVG AntiVirus")
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("By proceeding, you accept AVG's "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Agreement"))
                    }
                    append(tr(" and "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Privacy Policy"))
                    }
                    append(tr(". Your subscription renews unless canceled."))
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickOpenPrivacyPolicy()
            )
        }
    }
}

/** Upgrade paywall — "Ignore this offer" + YEARLY/MONTHLY pill + radio plan cards. */
@Composable
private fun UpgradePremiumPlans(
    yearly: Boolean,
    onYearlyChange: (Boolean) -> Unit,
    selectedPlan: String,
    onSelectPlan: (String) -> Unit,
    plusMonthEq: String,
    plusBilled: String,
    premMonthEq: String,
    premBilled: String,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = LocalCleanerColors.current
    var plusCleanupExpanded by remember { mutableStateOf(false) }
    var plusSecurityExpanded by remember { mutableStateOf(false) }
    var premCleanupExpanded by remember { mutableStateOf(false) }
    var premSecurityExpanded by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = tr("Back"),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(12.dp)
                    .size(24.dp)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = Amber,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(tr("Ignore this offer"))
                    }
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(tr(" if you're happy with a cluttered phone…"))
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    tr("4.7/5 ratings"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    tr("100M+ downloads"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(18.dp))

            // YEARLY / MONTHLY segmented control
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(colors.cardHigh)
                    .padding(4.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (yearly) accent else Color.Transparent)
                        .clickable { onYearlyChange(true) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (yearly) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color(0xFF07281A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            tr("YEARLY"),
                            fontWeight = FontWeight.Bold,
                            color = if (yearly) Color(0xFF07281A) else Color.White
                        )
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (!yearly) accent else Color.Transparent)
                        .clickable { onYearlyChange(false) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tr("MONTHLY"),
                        fontWeight = FontWeight.Bold,
                        color = if (!yearly) Color(0xFF07281A) else Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            PlanCard(
                name = tr("Premium Plus"),
                price = plusMonthEq,
                billed = plusBilled,
                selected = selectedPlan == "plus",
                onSelect = { onSelectPlan("plus") }
            ) {
                FeatureIconGroup(
                    title = tr("Cleanup Premium"),
                    subtitle = tr("Get rid of clutter and unused files"),
                    features = cleanupFeatures,
                    included = true,
                    expanded = plusCleanupExpanded,
                    onToggle = { plusCleanupExpanded = !plusCleanupExpanded }
                )
                FeatureIconGroup(
                    title = tr("Security Premium"),
                    subtitle = tr("Protect your phone from hackers"),
                    features = securityFeatures,
                    included = true,
                    expanded = plusSecurityExpanded,
                    onToggle = { plusSecurityExpanded = !plusSecurityExpanded }
                )
            }

            Spacer(Modifier.height(12.dp))

            PlanCard(
                name = tr("Premium"),
                price = premMonthEq,
                billed = premBilled,
                selected = selectedPlan == "premium",
                onSelect = { onSelectPlan("premium") }
            ) {
                FeatureIconGroup(
                    title = tr("Cleanup Premium"),
                    subtitle = tr("Get rid of clutter and unused files"),
                    features = cleanupFeatures,
                    included = true,
                    expanded = premCleanupExpanded,
                    onToggle = { premCleanupExpanded = !premCleanupExpanded }
                )
                FeatureIconGroup(
                    title = tr("Security Premium"),
                    subtitle = tr("Protect your phone from hackers"),
                    features = securityFeatures,
                    included = false,
                    expanded = premSecurityExpanded,
                    onToggle = { premSecurityExpanded = !premSecurityExpanded }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("By proceeding, you accept AVG's "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Agreement"))
                    }
                    append(tr(" and "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Privacy Policy"))
                    }
                    append(tr(". Your subscription renews unless canceled."))
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickOpenPrivacyPolicy()
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            PrimaryButton(tr("CONTINUE"), onClick = onContinue)
        }
    }
}

/** Retention downsell when backing out of the upgrade paywall. */
@Composable
private fun ExclusiveOfferScreen(
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = tr("Close"),
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(12.dp)
                    .size(24.dp)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                tr("Don't miss out - it's time!"),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                tr("Here is your"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("exclusive"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "10%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Amber)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    tr("offer"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.weight(1f))
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Text(
                tr("Premium plan"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Rs 79.17 / month",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF333333)
            )
            Text(
                "Billed Rs 950 / year",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("Premium helps "))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1B7A4A))) {
                        append(tr("users free up at least 10x more space"))
                    }
                    append(tr(" on their device"))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1B7A4A),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE6F6EE))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF2ECC71))
                    .clickable(onClick = onContinue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tr("CONTINUE"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = buildAnnotatedString {
                    append(tr("By proceeding, you accept AVG's "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Agreement"))
                    }
                    append(tr(" and "))
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(tr("Privacy Policy"))
                    }
                    append(tr(". Your subscription renews unless canceled."))
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickOpenPrivacyPolicy()
            )
        }
    }
}

@Composable
private fun AvgPlanCompareCard(
    modifier: Modifier = Modifier,
    title: String,
    includes: List<String>,
    price: String,
    period: String,
    primaryCta: Boolean,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        includes.forEach { label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF333333),
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = price,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center
        )
        Text(
            text = period,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(50))
                .then(
                    if (primaryCta) {
                        Modifier.background(Amber)
                    } else {
                        Modifier
                            .background(Color.White)
                            .border(1.dp, Color.Black, RoundedCornerShape(50))
                    }
                )
                .clickable(onClick = onUpgrade),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tr("UPGRADE"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun AvgFeatureSectionCard(
    title: String,
    features: List<PlanFeature>,
    footer: String?
) {
    val border = Color(0xFF4A5560)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = border.copy(alpha = 0.7f))
        features.forEach { feature ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = tr(feature.title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = tr(feature.subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8B0B8),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (footer != null) {
            HorizontalDivider(color = border.copy(alpha = 0.7f))
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA8B0B8),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun AvgPlayPurchaseSheet(
    planName: String,
    price: String,
    period: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // planName/price/period kept for call-site compatibility; Google Play sheet focuses on payment method.
    @Suppress("UNUSED_PARAMETER")
    val _unused = "$planName $price $period"
    val context = LocalContext.current
    val email = remember {
        runCatching {
            android.accounts.AccountManager.get(context)
                .getAccountsByType("com.google")
                .firstOrNull()
                ?.name
        }.getOrNull().orEmpty()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF2A2A2E))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* consume */ }
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = tr("Google Play"),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = tr("Start by adding a payment method"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (email.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0B0B5)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = tr(
                "Add a payment method to your Google Account to complete your purchase. Your payment information is only visible to Google."
            ),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0B0B5)
        )
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF5A5A60), RoundedCornerShape(10.dp))
                .clickable(onClick = onConfirm)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = tr("Add credit or debit card"),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = tr("CANCEL"),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFB0B0B5),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(10.dp)
        )
    }
}

/* ---- legacy plan cards kept for pitch / tour screens ---- */

@Composable
private fun PremiumPlusPitchScreen(
    yearly: Boolean,
    onClose: () -> Unit,
    onUpgrade: () -> Unit,
    onContinueWithPremium: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val accent = MaterialTheme.colorScheme.primary
    var featuresOpen by remember { mutableStateOf(true) }
    val priceMonth = if (yearly) tr("%s / month").replace("%s", "$1.99") else tr("%s / month").replace("%s", "$2.49")
    val billed = if (yearly) {
        tr("%s billed 1st year, then %s / year")
            .replaceFirst("%s", "$19.99")
            .replaceFirst("%s", "$29.99")
    } else {
        tr("Billed monthly")
    }
    val plusFeatures = listOf(
        tr("Get warned when a link is dangerous"),
        tr("Schedule automatic scans to stay safer"),
        tr("Secure your sensitive apps"),
        tr("Protect personal photos with unlimited coverage"),
        tr("Receive quick replies from support")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = tr("Close"),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(12.dp)
                    .size(24.dp)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            PremiumPlusHeroIllustration(
                accent = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = tr("…Or feel in control of your digital life"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Box(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .border(1.5.dp, accent, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                tr("Premium Plus"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                billed,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            priceMonth,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = tr("Save 10%"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF07281A),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                tr("Access these features:"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { featuresOpen = !featuresOpen },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Icons.Default.Security,
                    Icons.Default.PhoneAndroid,
                    Icons.Default.Lock,
                    Icons.Default.PhotoLibrary,
                    Icons.Default.SupportAgent
                ).forEach { icon ->
                    Icon(
                        icon,
                        null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    if (featuresOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = colors.textSecondary
                )
            }
            AnimatedVisibility(visible = featuresOpen) {
                Column(Modifier.padding(top = 12.dp)) {
                    plusFeatures.forEach { line ->
                        Row(
                            Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Amber),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color(0xFF1A1208),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                line,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(text = tr("UPGRADE"), onClick = onUpgrade)
            SecondaryButton(text = tr("CONTINUE WITH PREMIUM"), onClick = onContinueWithPremium)
        }
    }
}

@Composable
private fun PremiumPlusHeroIllustration(
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val cx = w * 0.5f
        val cy = h * 0.55f
        drawCircle(accent, radius = h * 0.38f, center = Offset(cx, cy), style = stroke)
        drawCircle(accent, radius = 14.dp.toPx(), center = Offset(cx, cy - 36.dp.toPx()), style = stroke)
        drawLine(accent, Offset(cx, cy - 20.dp.toPx()), Offset(cx, cy + 8.dp.toPx()), strokeWidth = 3.dp.toPx())
        drawLine(accent, Offset(cx, cy + 8.dp.toPx()), Offset(cx - 28.dp.toPx(), cy + 28.dp.toPx()), strokeWidth = 3.dp.toPx())
        drawLine(accent, Offset(cx, cy + 8.dp.toPx()), Offset(cx + 28.dp.toPx(), cy + 28.dp.toPx()), strokeWidth = 3.dp.toPx())
        drawRoundRect(
            accent,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(36.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = stroke
        )
        drawRoundRect(
            accent,
            topLeft = Offset(w * 0.72f, h * 0.22f),
            size = Size(40.dp.toPx(), 32.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = stroke
        )
    }
}

@Composable
private fun PlanCard(
    name: String,
    price: String,
    billed: String,
    selected: Boolean,
    onSelect: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalCleanerColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else colors.divider,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .border(
                        2.dp,
                        if (selected) MaterialTheme.colorScheme.primary else colors.textSecondary,
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            price,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(billed, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun FeatureIconGroup(
    title: String,
    subtitle: String,
    features: List<PlanFeature>,
    included: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalCleanerColors.current
    val titleColor = if (included) MaterialTheme.colorScheme.onBackground else colors.textSecondary
    val badgeGreen = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        // Collapsed: compact icon preview row. Expanded: detailed feature rows (AVG behavior).
        if (!expanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                features.forEach { feature ->
                    PlanFeatureBadgeIcon(
                        icon = feature.icon,
                        included = included,
                        size = 26.dp,
                        badgeGreen = badgeGreen
                    )
                }
            }
        } else {
            Column(Modifier.padding(bottom = 6.dp)) {
                features.forEachIndexed { index, feature ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = colors.divider.copy(alpha = 0.55f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlanFeatureBadgeIcon(
                            icon = feature.icon,
                            included = included,
                            size = 28.dp,
                            badgeGreen = badgeGreen
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                tr(feature.title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            )
                            Text(
                                tr(feature.subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanFeatureBadgeIcon(
    icon: ImageVector,
    included: Boolean,
    size: androidx.compose.ui.unit.Dp,
    badgeGreen: Color
) {
    val colors = LocalCleanerColors.current
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (included) MaterialTheme.colorScheme.onBackground else colors.textSecondary,
            modifier = Modifier.size(size * 0.78f)
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(size * 0.42f)
                .clip(RoundedCornerShape(50))
                .background(if (included) badgeGreen else DangerRed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (included) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.28f)
            )
        }
    }
}

/** The swipeable "Get to know the Premium features" tour (Account → Explore features). */
private data class FeatureTourPage(
    val title: String,
    val body: String,
    val cta: String, // LEARN MORE | UPGRADE
    val kind: FeatureTourKind
)

enum class FeatureTourKind {
    DeepClean, AutoCleaning, BrowserCleaner, SleepMode, CustomDashboard, PhotoOptimizer, VideoOptimizer
}

fun FeatureTourKind.toUpsellId(): String = when (this) {
    FeatureTourKind.DeepClean -> "deep_clean"
    FeatureTourKind.AutoCleaning -> "auto_cleaning"
    FeatureTourKind.BrowserCleaner -> "browser_cleaner"
    FeatureTourKind.SleepMode -> "sleep_mode"
    FeatureTourKind.CustomDashboard -> "custom_dashboard"
    FeatureTourKind.PhotoOptimizer -> "photo_optimizer"
    FeatureTourKind.VideoOptimizer -> "video_optimizer"
}

@Composable
fun PremiumFeaturesScreen(
    onClose: () -> Unit,
    onUpgrade: () -> Unit,
    onLearnMore: (FeatureTourKind) -> Unit
) {
    val colors = LocalCleanerColors.current
    val accent = MaterialTheme.colorScheme.primary
    val pages = listOf(
        FeatureTourPage(
            "Deep Clean",
            "A powerful cleaner that removes hidden junk.",
            tr("LEARN MORE"),
            FeatureTourKind.DeepClean
        ),
        FeatureTourPage(
            "Auto Cleaning",
            tr("Switch it on and never worry again."),
            tr("LEARN MORE"),
            FeatureTourKind.AutoCleaning
        ),
        FeatureTourPage(
            "Browser Cleaner",
            "Clear all your browsing records for more space and privacy.",
            tr("LEARN MORE"),
            FeatureTourKind.BrowserCleaner
        ),
        FeatureTourPage(
            "Sleep Mode",
            "Force stops unused apps to optimize your device.",
            tr("LEARN MORE"),
            FeatureTourKind.SleepMode
        ),
        FeatureTourPage(
            tr("Custom dashboard"),
            "Add custom shortcuts to your dashboard for quick access to your favorite info.",
            tr("LEARN MORE"),
            FeatureTourKind.CustomDashboard
        ),
        FeatureTourPage(
            "Photo Optimizer",
            "Go Premium to shrink your photos and free up storage space.",
            tr("UPGRADE"),
            FeatureTourKind.PhotoOptimizer
        ),
        FeatureTourPage(
            "Video Optimizer",
            "Unlock the Video Optimizer and cut your largest files down to size.",
            tr("UPGRADE"),
            FeatureTourKind.VideoOptimizer
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Space from top (below status bar), then header bar — matches real AVG 2nd SS
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.card)
        ) {
            // Row 1: close (X) only
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = tr("Close"),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onClose)
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
            // Row 2: title centered
            Text(
                text = tr("Get to know the Premium\nfeatures"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp)
            )
            HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            beyondViewportPageCount = 1
        ) { page ->
                val item = pages[page]
                val ctaFill = if (colors.isDark) Color.Transparent else accent
                val ctaBorder = if (colors.isDark) Color.White.copy(alpha = 0.85f) else accent
                val ctaText = if (colors.isDark) Color.White else Color.White
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (colors.isDark) Color.Transparent else colors.cardHigh),
                    contentAlignment = Alignment.Center
                ) {
                FeatureTourIllustration(
                    kind = item.kind,
                    accent = accent,
                    modifier = Modifier.size(240.dp)
                )
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ctaFill)
                        .border(1.5.dp, ctaBorder, RoundedCornerShape(50))
                        .clickable {
                            when (item.kind) {
                                FeatureTourKind.PhotoOptimizer,
                                FeatureTourKind.VideoOptimizer -> onUpgrade()
                                else -> onLearnMore(item.kind)
                            }
                        }
                        .padding(horizontal = 36.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.cta,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ctaText
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) accent else colors.divider)
                )
            }
        }
    }
}

@Composable
internal fun FeatureTourIllustration(
    kind: FeatureTourKind,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val muted = accent.copy(alpha = 0.55f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val thin = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun plus(x: Float, y: Float, s: Float = 8.dp.toPx()) {
            drawLine(accent, Offset(x - s, y), Offset(x + s, y), strokeWidth = 2.dp.toPx())
            drawLine(accent, Offset(x, y - s), Offset(x, y + s), strokeWidth = 2.dp.toPx())
        }

        when (kind) {
            FeatureTourKind.DeepClean -> {
                // Microscope figure + floating bubbles
                val cx = w * 0.48f
                val cy = h * 0.58f
                // body
                drawCircle(accent, radius = 14.dp.toPx(), center = Offset(cx, cy - 70.dp.toPx()), style = stroke)
                drawLine(accent, Offset(cx, cy - 56.dp.toPx()), Offset(cx, cy - 10.dp.toPx()), strokeWidth = 3.5.dp.toPx())
                // arms to microscope
                drawLine(accent, Offset(cx, cy - 40.dp.toPx()), Offset(cx + 36.dp.toPx(), cy - 20.dp.toPx()), strokeWidth = 3.dp.toPx())
                // microscope
                drawLine(accent, Offset(cx + 40.dp.toPx(), cy + 20.dp.toPx()), Offset(cx + 40.dp.toPx(), cy - 50.dp.toPx()), strokeWidth = 3.5.dp.toPx())
                drawLine(accent, Offset(cx + 20.dp.toPx(), cy + 20.dp.toPx()), Offset(cx + 60.dp.toPx(), cy + 20.dp.toPx()), strokeWidth = 3.5.dp.toPx())
                drawCircle(accent, radius = 16.dp.toPx(), center = Offset(cx + 40.dp.toPx(), cy - 58.dp.toPx()), style = stroke)
                // legs
                drawLine(accent, Offset(cx, cy - 10.dp.toPx()), Offset(cx - 22.dp.toPx(), cy + 40.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawLine(accent, Offset(cx, cy - 10.dp.toPx()), Offset(cx + 18.dp.toPx(), cy + 40.dp.toPx()), strokeWidth = 3.dp.toPx())
                // bubbles
                val bubbles = listOf(
                    Offset(w * 0.18f, h * 0.28f) to IconsHint.Eye,
                    Offset(w * 0.22f, h * 0.52f) to IconsHint.Doc,
                    Offset(w * 0.78f, h * 0.26f) to IconsHint.Folder,
                    Offset(w * 0.82f, h * 0.50f) to IconsHint.Gear
                )
                bubbles.forEach { (c, hint) ->
                    drawCircle(accent, radius = 22.dp.toPx(), center = c, style = stroke)
                    drawHint(c, hint, accent, thin)
                }
                plus(w * 0.12f, h * 0.18f)
                plus(w * 0.88f, h * 0.72f, 6.dp.toPx())
            }

            FeatureTourKind.AutoCleaning -> {
                // Robot + phone
                val phoneL = w * 0.52f
                val phoneT = h * 0.18f
                drawRoundRect(
                    accent,
                    topLeft = Offset(phoneL, phoneT),
                    size = Size(w * 0.32f, h * 0.62f),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = stroke
                )
                drawLine(accent, Offset(phoneL + 20.dp.toPx(), phoneT + 14.dp.toPx()), Offset(phoneL + w * 0.22f, phoneT + 14.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                // robot
                val rx = w * 0.28f
                val ry = h * 0.42f
                drawCircle(accent, 16.dp.toPx(), Offset(rx, ry - 36.dp.toPx()), style = stroke)
                drawRoundRect(accent, Offset(rx - 22.dp.toPx(), ry - 16.dp.toPx()), Size(44.dp.toPx(), 50.dp.toPx()), CornerRadius(10.dp.toPx()), style = stroke)
                drawCircle(accent, 10.dp.toPx(), Offset(rx, ry + 4.dp.toPx()), style = thin)
                // spray bottle in hand
                drawLine(accent, Offset(rx + 22.dp.toPx(), ry - 4.dp.toPx()), Offset(rx + 48.dp.toPx(), ry - 28.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawRoundRect(accent, Offset(rx + 42.dp.toPx(), ry - 52.dp.toPx()), Size(18.dp.toPx(), 28.dp.toPx()), CornerRadius(4.dp.toPx()), style = thin)
                plus(w * 0.14f, h * 0.22f)
                plus(w * 0.9f, h * 0.3f, 6.dp.toPx())
                plus(w * 0.18f, h * 0.78f, 5.dp.toPx())
            }

            FeatureTourKind.BrowserCleaner -> {
                val c = Offset(w * 0.5f, h * 0.48f)
                drawCircle(accent, radius = 58.dp.toPx(), center = c, style = stroke)
                drawCircle(accent, radius = 42.dp.toPx(), center = c, style = thin)
                // globe meridians
                drawArc(accent, 0f, 360f, false, Offset(c.x - 42.dp.toPx(), c.y - 58.dp.toPx()), Size(84.dp.toPx(), 116.dp.toPx()), style = thin)
                drawLine(accent, Offset(c.x - 58.dp.toPx(), c.y), Offset(c.x + 58.dp.toPx(), c.y), strokeWidth = 2.5.dp.toPx())
                drawLine(accent, Offset(c.x, c.y - 58.dp.toPx()), Offset(c.x, c.y + 58.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                plus(w * 0.18f, h * 0.2f)
                plus(w * 0.82f, h * 0.28f, 6.dp.toPx())
                plus(w * 0.78f, h * 0.72f)
            }

            FeatureTourKind.SleepMode -> {
                val c = Offset(w * 0.5f, h * 0.42f)
                drawCircle(accent, 62.dp.toPx(), c, style = stroke)
                // clock hands
                drawLine(accent, c, Offset(c.x, c.y - 34.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawLine(accent, c, Offset(c.x + 24.dp.toPx(), c.y + 8.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawCircle(accent, 4.dp.toPx(), c)
                // ice crystals base
                val baseY = h * 0.78f
                for (i in 0..4) {
                    val x = w * 0.22f + i * 28.dp.toPx()
                    drawLine(accent, Offset(x, baseY), Offset(x + 10.dp.toPx(), baseY - 28.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                    drawLine(accent, Offset(x + 10.dp.toPx(), baseY - 28.dp.toPx()), Offset(x + 20.dp.toPx(), baseY), strokeWidth = 2.5.dp.toPx())
                }
                // snowflakes
                listOf(Offset(w * 0.22f, h * 0.18f), Offset(w * 0.72f, h * 0.16f), Offset(w * 0.82f, h * 0.32f)).forEach {
                    drawLine(muted, Offset(it.x - 8.dp.toPx(), it.y), Offset(it.x + 8.dp.toPx(), it.y), strokeWidth = 2.dp.toPx())
                    drawLine(muted, Offset(it.x, it.y - 8.dp.toPx()), Offset(it.x, it.y + 8.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
                plus(w * 0.12f, h * 0.55f, 6.dp.toPx())
            }

            FeatureTourKind.CustomDashboard -> {
                // person pointing at phone
                val px = w * 0.28f
                val py = h * 0.55f
                drawCircle(accent, 14.dp.toPx(), Offset(px, py - 70.dp.toPx()), style = stroke)
                drawLine(accent, Offset(px, py - 56.dp.toPx()), Offset(px, py - 8.dp.toPx()), strokeWidth = 3.5.dp.toPx())
                drawLine(accent, Offset(px, py - 40.dp.toPx()), Offset(px + 50.dp.toPx(), py - 28.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawLine(accent, Offset(px, py - 8.dp.toPx()), Offset(px - 18.dp.toPx(), py + 42.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawLine(accent, Offset(px, py - 8.dp.toPx()), Offset(px + 16.dp.toPx(), py + 42.dp.toPx()), strokeWidth = 3.dp.toPx())
                // large phone
                val pl = w * 0.48f
                val pt = h * 0.12f
                drawRoundRect(accent, Offset(pl, pt), Size(w * 0.38f, h * 0.7f), CornerRadius(16.dp.toPx()), style = stroke)
                drawRoundRect(accent, Offset(pl + 18.dp.toPx(), pt + 28.dp.toPx()), Size(52.dp.toPx(), 52.dp.toPx()), CornerRadius(10.dp.toPx()), style = thin)
                // lightbulb
                drawCircle(accent, 12.dp.toPx(), Offset(pl + 44.dp.toPx(), pt + 48.dp.toPx()), style = thin)
                drawLine(accent, Offset(pl + 28.dp.toPx(), pt + 100.dp.toPx()), Offset(pl + 70.dp.toPx(), pt + 100.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                drawLine(accent, Offset(pl + 28.dp.toPx(), pt + 118.dp.toPx()), Offset(pl + 62.dp.toPx(), pt + 118.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                plus(w * 0.12f, h * 0.2f)
                plus(w * 0.9f, h * 0.55f, 6.dp.toPx())
            }

            FeatureTourKind.PhotoOptimizer -> {
                // scale with one large vs two small photos
                val pivot = Offset(w * 0.5f, h * 0.28f)
                drawLine(accent, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.82f, h * 0.42f), strokeWidth = 3.dp.toPx())
                drawLine(accent, pivot, Offset(w * 0.5f, h * 0.42f), strokeWidth = 3.dp.toPx())
                // left pan (up) - large photo
                drawLine(accent, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.18f, h * 0.52f), strokeWidth = 2.5.dp.toPx())
                drawRoundRect(accent, Offset(w * 0.06f, h * 0.52f), Size(70.dp.toPx(), 70.dp.toPx()), CornerRadius(8.dp.toPx()), style = stroke)
                drawCircle(accent, 6.dp.toPx(), Offset(w * 0.14f, h * 0.62f), style = thin)
                // right pan (down) - two small
                drawLine(accent, Offset(w * 0.82f, h * 0.42f), Offset(w * 0.82f, h * 0.58f), strokeWidth = 2.5.dp.toPx())
                drawRoundRect(accent, Offset(w * 0.68f, h * 0.58f), Size(36.dp.toPx(), 36.dp.toPx()), CornerRadius(6.dp.toPx()), style = stroke)
                drawRoundRect(accent, Offset(w * 0.78f, h * 0.72f), Size(36.dp.toPx(), 36.dp.toPx()), CornerRadius(6.dp.toPx()), style = stroke)
                plus(w * 0.88f, h * 0.22f)
                plus(w * 0.92f, h * 0.55f, 5.dp.toPx())
            }

            FeatureTourKind.VideoOptimizer -> {
                val pivot = Offset(w * 0.5f, h * 0.28f)
                drawLine(accent, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.82f, h * 0.42f), strokeWidth = 3.dp.toPx())
                drawLine(accent, pivot, Offset(w * 0.5f, h * 0.42f), strokeWidth = 3.dp.toPx())
                drawLine(accent, Offset(w * 0.18f, h * 0.42f), Offset(w * 0.18f, h * 0.52f), strokeWidth = 2.5.dp.toPx())
                drawRoundRect(accent, Offset(w * 0.05f, h * 0.52f), Size(78.dp.toPx(), 54.dp.toPx()), CornerRadius(8.dp.toPx()), style = stroke)
                // play triangle
                val path = Path().apply {
                    moveTo(w * 0.14f, h * 0.62f)
                    lineTo(w * 0.14f, h * 0.78f)
                    lineTo(w * 0.26f, h * 0.70f)
                    close()
                }
                drawPath(path, accent, style = thin)
                drawLine(accent, Offset(w * 0.82f, h * 0.42f), Offset(w * 0.82f, h * 0.58f), strokeWidth = 2.5.dp.toPx())
                drawRoundRect(accent, Offset(w * 0.70f, h * 0.58f), Size(40.dp.toPx(), 28.dp.toPx()), CornerRadius(6.dp.toPx()), style = stroke)
                drawRoundRect(accent, Offset(w * 0.78f, h * 0.72f), Size(40.dp.toPx(), 28.dp.toPx()), CornerRadius(6.dp.toPx()), style = stroke)
                plus(w * 0.9f, h * 0.25f)
                plus(w * 0.14f, h * 0.2f, 6.dp.toPx())
            }
        }
    }
}

private enum class IconsHint { Eye, Doc, Folder, Gear }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHint(
    c: Offset,
    hint: IconsHint,
    color: Color,
    stroke: Stroke
) {
    when (hint) {
        IconsHint.Eye -> {
            drawOval(color, Offset(c.x - 10.dp.toPx(), c.y - 6.dp.toPx()), Size(20.dp.toPx(), 12.dp.toPx()), style = stroke)
            drawCircle(color, 3.dp.toPx(), c)
        }
        IconsHint.Doc -> {
            drawRoundRect(color, Offset(c.x - 8.dp.toPx(), c.y - 10.dp.toPx()), Size(16.dp.toPx(), 20.dp.toPx()), CornerRadius(2.dp.toPx()), style = stroke)
            drawLine(color, Offset(c.x - 4.dp.toPx(), c.y - 2.dp.toPx()), Offset(c.x + 4.dp.toPx(), c.y - 2.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(color, Offset(c.x - 4.dp.toPx(), c.y + 3.dp.toPx()), Offset(c.x + 4.dp.toPx(), c.y + 3.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
        IconsHint.Folder -> {
            drawRoundRect(color, Offset(c.x - 10.dp.toPx(), c.y - 4.dp.toPx()), Size(20.dp.toPx(), 14.dp.toPx()), CornerRadius(2.dp.toPx()), style = stroke)
            drawLine(color, Offset(c.x - 10.dp.toPx(), c.y - 4.dp.toPx()), Offset(c.x - 2.dp.toPx(), c.y - 10.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(color, Offset(c.x - 2.dp.toPx(), c.y - 10.dp.toPx()), Offset(c.x + 4.dp.toPx(), c.y - 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
        IconsHint.Gear -> {
            drawCircle(color, 6.dp.toPx(), c, style = stroke)
            for (i in 0 until 6) {
                val a = Math.toRadians((i * 60).toDouble())
                val x1 = c.x + kotlin.math.cos(a).toFloat() * 8.dp.toPx()
                val y1 = c.y + kotlin.math.sin(a).toFloat() * 8.dp.toPx()
                val x2 = c.x + kotlin.math.cos(a).toFloat() * 12.dp.toPx()
                val y2 = c.y + kotlin.math.sin(a).toFloat() * 12.dp.toPx()
                drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.5.dp.toPx())
            }
        }
    }
}

/** Landing page for one premium feature, reached from Tools. */
@Composable
fun FeatureDetailScreen(featureId: String, onBack: () -> Unit) {
    val colors = LocalCleanerColors.current
    val title = when (featureId) {
        "photo_optimizer" -> "Photo Optimizer"
        "video_optimizer" -> "Video Optimizer"
        "sleep_mode" -> "Sleep Mode"
        else -> tr("Feature")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        CleanerTopBar(title, onBack = onBack, centered = false)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(tr("Open this feature from Tools."), color = colors.textSecondary)
        }
    }
}
