package com.baothanhbin.agridoctorai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.baothanhbin.agridoctorai.navigation.TopLevelDestination
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.feature.camera.navigation.navigateToCamera

@Composable
fun MainBottomNavBar(
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: TopLevelDestination?,
    navController: NavController
) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Bottom bar with rounded top corners and shadow
            NavigationBar(
                modifier = modifier
                    .shadow(
                        elevation = 15.dp
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = Color.White
            ) {
                val splitIndex = (destinations.size + 1) / 2
                val first = destinations.take(splitIndex)
                val second = destinations.drop(splitIndex)

                first.forEach { destination ->
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

                // Spacer to create a notch-like gap under the center FAB
                Spacer(modifier = Modifier.weight(1f))

                second.forEach { destination ->
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

            // Center floating action button (camera-like action)
            FloatingActionButton(
                onClick = { navController.navigateToCamera(0) },
                shape = CircleShape,
                containerColor = Color(0xFF2E7D32),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        // Add a small spacer to ensure content above doesn't collide with the FAB overlap
        Spacer(modifier = Modifier.height(8.dp))
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