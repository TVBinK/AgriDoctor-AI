package com.baothanhbin.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

private const val HALF_SCREEN_DIVISOR = 2
private const val NAV_DURATION_MS = 180
private val navSlideSpec = tween<IntOffset>(
    durationMillis = NAV_DURATION_MS,
    easing = FastOutSlowInEasing
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromRightHalf(): EnterTransition {
    return slideInHorizontally(
        animationSpec = navSlideSpec,
        initialOffsetX = { fullWidth -> fullWidth / HALF_SCREEN_DIVISOR }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToLeftHalf(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = navSlideSpec,
        targetOffsetX = { fullWidth -> -fullWidth }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromLeftHalf(): EnterTransition {
    return slideInHorizontally(
        animationSpec = navSlideSpec,
        initialOffsetX = { fullWidth -> -fullWidth / HALF_SCREEN_DIVISOR }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToRightHalf(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = navSlideSpec,
        targetOffsetX = { fullWidth -> fullWidth }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromBottomHalf(): EnterTransition {
    return slideInVertically(
        animationSpec = navSlideSpec,
        initialOffsetY = { fullHeight -> fullHeight / HALF_SCREEN_DIVISOR }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToTopHalf(): ExitTransition {
    return slideOutVertically(
        animationSpec = navSlideSpec,
        targetOffsetY = { fullHeight -> -fullHeight }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideInFromTopHalf(): EnterTransition {
    return slideInVertically(
        animationSpec = navSlideSpec,
        initialOffsetY = { fullHeight -> -fullHeight / HALF_SCREEN_DIVISOR }
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutToBottomHalf(): ExitTransition {
    return slideOutVertically(
        animationSpec = navSlideSpec,
        targetOffsetY = { fullHeight -> fullHeight }
    )
}
