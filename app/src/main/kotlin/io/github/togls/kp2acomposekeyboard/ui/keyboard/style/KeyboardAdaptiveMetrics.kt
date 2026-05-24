package io.github.togls.kp2acomposekeyboard.ui.keyboard.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode

@Immutable
internal data class KeyboardAdaptiveMetrics(
    val keyHeight: Dp,
    val keyHorizontalPadding: Dp,
    val keyCornerRadius: Dp,
    val bottomSafePadding: Dp,
    val maxNavigationAwareBottomPadding: Dp,
) {
    companion object {
        private val Portrait = KeyboardAdaptiveMetrics(
            keyHeight = KeyboardMetrics.KeyBaseHeight,
            keyHorizontalPadding = KeyboardMetrics.KeyHorizontalPadding,
            keyCornerRadius = KeyboardMetrics.KeyCornerRadius,
            bottomSafePadding = 30.dp,
            maxNavigationAwareBottomPadding = 30.dp,
        )

        private val Landscape = KeyboardAdaptiveMetrics(
            keyHeight = 38.dp,
            keyHorizontalPadding = 6.dp,
            keyCornerRadius = KeyboardMetrics.KeyCornerRadius,
            bottomSafePadding = 6.dp,
            maxNavigationAwareBottomPadding = 12.dp,
        )

        fun resolve(
            heightMode: KeyboardHeightMode,
            isLandscape: Boolean,
        ): KeyboardAdaptiveMetrics {
            val base = if (isLandscape) {
                Landscape
            } else {
                Portrait
            }

            val scale = heightMode.keyHeightScale()

            return base.copy(
                keyHeight = base.keyHeight * scale,
            )
        }
    }
}

private fun KeyboardHeightMode.keyHeightScale(): Float {
    return when (this) {
        KeyboardHeightMode.Compact -> 0.9f
        KeyboardHeightMode.Normal -> 1.0f
        KeyboardHeightMode.Tall -> 1.1f
    }
}

internal val LocalKeyboardAdaptiveMetrics = compositionLocalOf {
    KeyboardAdaptiveMetrics.resolve(
        heightMode = KeyboardHeightMode.Normal,
        isLandscape = false,
    )
}