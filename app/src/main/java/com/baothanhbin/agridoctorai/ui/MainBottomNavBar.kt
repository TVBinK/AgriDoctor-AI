package com.baothanhbin.agridoctorai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baothanhbin.agridoctorai.navigation.TopLevelDestination

@Composable
fun MainBottomNavBar(
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: TopLevelDestination?,
) {
    Column(Modifier.fillMaxWidth()) {
        NavigationBar(
            modifier = modifier
                .shadow(
                    elevation = 15.dp
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            containerColor = Color.White
        ) {
            destinations.forEach { destination ->
                val selected = currentDestination == destination
                MainBottomBarItem(
                    label = stringResource(id = destination.label),
                    selected = selected,
                    onclick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}
@Composable
fun RowScope.MainBottomBarItem(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onclick: () -> Unit,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
) {
    NavigationBarItem(
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        selected = selected,
        onClick = onclick,
        icon = if (selected) selectedIcon else icon,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = Color.Unspecified,
            unselectedIconColor = Color.Unspecified,
            selectedTextColor = Color.Gray,
            unselectedTextColor = Color.Gray
        )
    )
}