package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class KeyboardAdaptiveMetrics(
    val keyMinHeight: Dp,
    val keyHorizontalPadding: Dp,
    val keyCornerRadius: Dp,
    val bottomSafePadding: Dp,
    val maxNavigationAwareBottomPadding: Dp,
) {
    companion object {
        val Portrait = KeyboardAdaptiveMetrics(
            keyMinHeight = 46.dp,
            keyHorizontalPadding = 8.dp,
            keyCornerRadius = 18.dp,
            bottomSafePadding = 10.dp,
            maxNavigationAwareBottomPadding = 18.dp,
        )

        val Landscape = KeyboardAdaptiveMetrics(
            keyMinHeight = 38.dp,
            keyHorizontalPadding = 6.dp,
            keyCornerRadius = 16.dp,
            bottomSafePadding = 6.dp,
            maxNavigationAwareBottomPadding = 12.dp,
        )
    }
}

internal val LocalKeyboardAdaptiveMetrics = compositionLocalOf {
    KeyboardAdaptiveMetrics.Portrait
}