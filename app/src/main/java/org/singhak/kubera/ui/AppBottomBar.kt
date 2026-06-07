package org.singhak.kubera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Suppress("LongMethod")
@Composable
fun AppBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(height = 1.dp, width = 0.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(AppTab.HOME, Icons.Outlined.Home, "Home", currentTab, onTabSelected)
            NavItem(AppTab.ANALYTICS, Icons.Outlined.BarChart, "Analytics", currentTab, onTabSelected)

            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAddTransaction() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 26.sp,
                    lineHeight = 26.sp,
                )
            }

            NavItem(AppTab.CIRCLES, Icons.Outlined.Group, "Circles", currentTab, onTabSelected)
            NavItem(AppTab.AUTOPAY, Icons.Outlined.Autorenew, "Autopay", currentTab, onTabSelected)
        }
    }
}

@Composable
private fun NavItem(
    tab: AppTab,
    icon: ImageVector,
    label: String,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    val active = tab == currentTab
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onTabSelected(tab) }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
