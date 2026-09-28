package com.replica.cleaner.ui.screens

import com.replica.cleaner.l10n.tr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.replica.cleaner.ui.CleanerViewModel
import com.replica.cleaner.ui.components.CleanerTopBar
import com.replica.cleaner.ui.components.PrimaryButton
import com.replica.cleaner.ui.theme.Amber
import com.replica.cleaner.ui.theme.LocalCleanerColors
import com.replica.cleaner.ui.util.clickOpenPrivacyPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Feature-specific upsell (Sleep Mode / Deep Clean Learn More, etc.) —
 * matches AVG: Premium feature label, plans, includes, reviews, FAQs, CONTINUE.
 */
@Composable
fun FeatureUpsellScreen(
    vm: CleanerViewModel,
    featureId: String,
    onBack: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val copy = featureUpsellCopy(featureId) // composable lookup
    FeatureUpsellPaywall(
        title = copy.title,
        body = copy.body,
        faqs = copy.faqs,
        onBack = onBack,
        onPurchased = { vm.setPremium(true) },
        onAlreadyPurchased = onAlreadyPurchased
    )
}

private data class FeatureUpsellCopy(
    val title: String,
    val body: String,
    val faqs: List<Pair<String, String>>
)

@Composable
private fun featureUpsellCopy(id: String): FeatureUpsellCopy = when (id) {
    "deep_clean" -> FeatureUpsellCopy(
        title = tr("Deep Clean"),
        body = tr("Free up space fast — clear junk, tidy storage, and organize your device."),
        faqs = listOf(
            tr("What's the difference between Hidden and Visible caches?") to
                tr("Apps create temporarily needed files called 'caches', but afterward, when you don't need them anymore, caches just take up space. Hidden caches (sometimes called private caches) take up much more space and are more tricky to delete, while visible caches take up less space and can be removed easily. Usually, you need to open your settings to remove hidden app caches one-by-one, but Deep Clean lets you clean hidden caches for many apps simultaneously."),
            tr("How does Deep Clean work?") to
                tr("You can clean individual hidden caches by going to App detail > Clean cache in your system settings. Deep Clean does the same thing, but instead of one-by-one, it will clean all the apps you select at once."),
            tr("Should I be concerned about deleting cache files?") to
                tr("Nope! There's nothing in your cache files that your apps can't recreate if needed.\n\nBut be careful to clean up these files regularly, otherwise your device can collect a lot of junk files over time.")
        )
    )
    "auto_cleaning" -> FeatureUpsellCopy(
        title = tr("Auto Cleaning"),
        body = tr("Switch it on and never worry again — cleanup runs on your schedule."),
        faqs = listOf(
            tr("What does Auto Cleaning do?") to
                tr("Auto Cleaning schedules Quick Clean and related cleanup so junk is removed without opening the app each time."),
            tr("Can I change the schedule?") to
                tr("Yes. You can set how often Auto Cleaning runs and which categories it includes."),
            tr("Is Auto Cleaning safe?") to
                tr("Yes. It only removes junk categories you allow — personal files stay untouched.")
        )
    )
    "browser_cleaner" -> FeatureUpsellCopy(
        title = tr("Browser Cleaner"),
        body = tr("Improve your browsing experience by clearing out unnecessary data and hidden junk."),
        faqs = listOf(
            tr("How does it work?") to
                tr("When you're ready to clean, Browser Cleaner navigates your device settings and taps on all the right buttons on your behalf. This automated process allows us to find and delete your browser data fast and safely."),
            tr("Is it safe to delete browsing records?") to
                tr("For sure! Browsing records are not important to your device or how it works, so you probably won't notice that they're gone."),
            tr("Is it safe to delete search records?") to
                tr("Yes! However, cleaning Google Search data may have some unintended consequences. Our testing showed you might need to grant some permissions again. Nothing destructive, just a bit inconvenient — that's why this item is never pre-selected for cleaning.")
        )
    )
    "custom_dashboard" -> FeatureUpsellCopy(
        title = tr("Custom dashboard"),
        body = tr("Add custom shortcuts to your dashboard for quick access to your favorite info."),
        faqs = listOf(
            tr("What is Custom dashboard?") to
                tr("It lets you pin shortcuts to the tools and info you use most on Home."),
            tr("Can I rearrange shortcuts?") to
                tr("Yes. Customize which tiles appear and their order."),
            tr("Is this a Premium feature?") to
                tr("Yes. Custom dashboard is included with Premium.")
        )
    )
    else -> FeatureUpsellCopy(
        title = tr("Sleep Mode"),
        body = tr("Cut distractions by pausing multiple apps — no activity or notifications until you reopen them."),
        faqs = listOf(
            tr("What is the purpose of Sleep Mode?") to
                tr("Sleep Mode uses Force stop action and puts apps to sleep until you need them again. Force stopped apps can't access system resources and thus have no effect on battery, data, memory, and storage space. You can wake up stopped apps by reopening them any time."),
            tr("How does Force stop work?") to
                tr("Force stop stops all app processes on the spot. Usually you need to open your settings to Force stop each app one-by-one, but Sleep Mode lets you stop multiple apps at once."),
            tr("Should I be concerned about force stopping apps?") to
                tr("Nope! When you force stop apps, there's no risk of losing data, preferences, or account info.\n\nBut don't forget — force stopped apps can't send you notifications or work in the background, so you shouldn't force stop apps that send important info (like security or messaging apps)."),
            tr("Can force stopped apps restart by themselves?") to
                tr("There are a few exception apps that can wake themselves up without being opened (like Facebook Messenger and some system apps), but most apps will stay force stopped until you need them.")
        )
    )
}

@Composable
fun FeatureUpsellPaywall(
    title: String,
    body: String,
    faqs: List<Pair<String, String>>,
    onBack: () -> Unit,
    onPurchased: () -> Unit,
    onAlreadyPurchased: () -> Unit
) {
    val colors = LocalCleanerColors.current
    var yearly by remember { mutableStateOf(true) }
    var purchasePhase by remember { mutableStateOf(0) }
    var openFaq by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val featureBlue = Color(0xFF4DB8FF)

    val includes = listOf(
        Triple(tr("Zero ads"), tr("No advertisement in this app"), Icons.Default.Block),
        Triple(tr("Auto Cleaning"), tr("Set once, never worry again"), Icons.Default.Autorenew),
        Triple(tr("Browser Cleaner"), tr("Clean unimportant Google data"), Icons.Default.Language),
        Triple(tr("Sleep Mode"), tr("Force stops apps you don't use"), Icons.Default.Bedtime),
        Triple(tr("Custom Dashboard"), tr("Get shortcuts to your favorite info"), Icons.Default.DashboardCustomize),
        Triple(tr("Photo Optimizer"), tr("Control size and quality of photos"), Icons.Default.GridView),
        Triple(tr("Themes"), tr("Make your app match your style"), Icons.Default.Palette),
        Triple(tr("Avast Direct support"), tr("At your service"), Icons.Default.HeadsetMic)
    )
    val reviews = listOf(
        "Znort B." to "Decided recently to purchase the full package, and I've got to say, I'm impressed. Easy to set up and use.",
        "Lisa A." to "Wow. I'm impressed. The deep clean feature is amazing. It's like having a new phone again!"
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            CleanerTopBar("", onBack = onBack)

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = tr("Premium feature"),
                    style = MaterialTheme.typography.labelLarge,
                    color = Amber,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(20.dp))

                Box(Modifier.fillMaxWidth()) {
                    UpsellPlanCard(
                        title = tr("Yearly"),
                        subtitle = tr("%s / year").replace("%s", "$23.99"),
                        price = tr("%s / month").replace("%s", "$1.99"),
                        selected = yearly,
                        onSelect = { yearly = true },
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = tr("SAVE 72%"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF07281A),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                UpsellPlanCard(
                    title = tr("Monthly"),
                    subtitle = tr("Billed monthly"),
                    price = tr("%s / month").replace("%s", "$1.99"),
                    selected = !yearly,
                    onSelect = { yearly = false }
                )

                Spacer(Modifier.height(24.dp))
                Text(
                    tr("Premium includes:"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                includes.forEach { (itemTitle, itemBody, icon) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                itemTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(itemBody, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(featureBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = Color(0xFF0A1A28), modifier = Modifier.size(22.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    tr("From our customers"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    reviews.forEach { (name, review) ->
                        Column(
                            Modifier
                                .width(280.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.card)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    repeat(5) {
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            tint = Amber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(review, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    tr("FAQs"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                faqs.forEachIndexed { index, (q, a) ->
                    val open = openFaq == index
                    Column {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { openFaq = if (open) null else index }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                q,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = colors.textSecondary
                            )
                        }
                        if (open) {
                            Text(
                                a,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    tr("Already purchased?"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = purchasePhase == 0, onClick = onAlreadyPurchased)
                )
                Spacer(Modifier.height(12.dp))
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
                        .clickOpenPrivacyPolicy()
                )
                Spacer(Modifier.height(16.dp))
            }

            Column(Modifier.padding(16.dp)) {
                PrimaryButton(
                    text = tr("CONTINUE"),
                    enabled = purchasePhase == 0,
                    onClick = {
                        purchasePhase = 1
                        scope.launch {
                            delay(1400)
                            purchasePhase = 2
                        }
                    }
                )
            }
        }

        if (purchasePhase >= 1) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
        }
        if (purchasePhase == 1) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
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
                    Text(tr("Processing purchase..."), color = colors.textSecondary)
                }
            }
        }
        if (purchasePhase == 2) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF2A2A2E))
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Text("Google Play", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                Text(tr("Start by adding a payment method"), color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    tr("Add a payment method to your Google Account to complete your purchase."),
                    color = Color(0xFFB0B0B5),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF5A5A60), RoundedCornerShape(10.dp))
                        .clickable {
                            purchasePhase = 1
                            scope.launch {
                                delay(800)
                                onPurchased()
                            }
                        }
                        .padding(14.dp)
                ) {
                    Text(tr("Add credit or debit card"), color = Color.White)
                }
                Text(
                    tr("Simulate successful purchase"),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            purchasePhase = 1
                            scope.launch {
                                delay(800)
                                onPurchased()
                            }
                        }
                        .padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun UpsellPlanCard(
    title: String,
    subtitle: String,
    price: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCleanerColors.current
    val slash = price.indexOf(" / ")
    val priceMain = if (slash >= 0) price.substring(0, slash) else price
    val priceUnit = if (slash >= 0) price.substring(slash) else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else colors.divider,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                ) { append(priceMain) }
                if (priceUnit.isNotEmpty()) {
                    withStyle(SpanStyle(color = colors.textSecondary)) { append(priceUnit) }
                }
            },
            style = MaterialTheme.typography.titleSmall
        )
    }
}
