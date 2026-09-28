package com.replica.cleaner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.replica.cleaner.l10n.LocalL10n
import com.replica.cleaner.ui.nav.Routes
import com.replica.cleaner.ui.theme.LocalCleanerColors

/**
 * The app's top bar: a back (or close) affordance on the left, a centred title,
 * and an optional action. Matches the reference layout, which centres titles on
 * detail screens and left-aligns them on list screens.
 */
@Composable
fun CleanerTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
    closeIcon: Boolean = false,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (closeIcon) Icons.Default.Close
                    else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (closeIcon) "Close" else "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (centered) 0.dp else 8.dp),
            textAlign = if (centered) androidx.compose.ui.text.style.TextAlign.Center
            else androidx.compose.ui.text.style.TextAlign.Start
        )

        if (actions != null) actions() else Spacer(Modifier.width(44.dp))
    }
}

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

/**
 * The selected tab gets a filled pill behind its icon, which is what the
 * reference app does instead of tinting the icon alone.
 */
@Composable
fun CleanerBottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCleanerColors.current
    val t = LocalL10n.current
    val tabs = remember(t.language.tag) {
        listOf(
            BottomTab(Routes.HOME, t.home, Icons.Outlined.Home),
            BottomTab(Routes.TOOLS, t.tools, Icons.Outlined.Apps),
            BottomTab(Routes.STORAGE, t.storage, Icons.Outlined.Storage),
            BottomTab(Routes.ACCOUNT, t.account, Icons.Outlined.Person)
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(tab.route) }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(54.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else androidx.compose.ui.graphics.Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) androidx.compose.ui.graphics.Color(0xFF07281A)
                            else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.onBackground
                        else colors.textSecondary
                    )
                }
            }
        }
    }
}
