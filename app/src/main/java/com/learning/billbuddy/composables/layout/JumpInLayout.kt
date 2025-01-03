package com.learning.billbuddy.composables.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun JumpInLayout(flag: Boolean, childView: @Composable () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = flag,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth } // Start off-screen to the right
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth } // Exit off-screen to the left
        ) + fadeOut()
    ) {
        childView()
    }
}