package io.github.togls.kp2acomposekeyboard.ui.keyboard.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode

internal object KeyboardMetrics {
    val OuterPaddingHorizontal = 8.dp
    val OuterPaddingVertical = 8.dp

    val RowSpacing = 7.dp
    val KeySpacing = 6.dp

    val KeyBaseHeight = 46.dp
    val KeyHorizontalPadding = 8.dp
    val KeyVerticalPadding = 0.dp
    val KeyCornerRadius = 18.dp

    val PressedScale = 0.97f
    val NormalScale = 1f
}

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
            keyHeight = 46.dp,
            keyHorizontalPadding = 8.dp,
            keyCornerRadius = 18.dp,
            bottomSafePadding = 30.dp,
            maxNavigationAwareBottomPadding = 30.dp,
        )

        private val Landscape = KeyboardAdaptiveMetrics(
            keyHeight = 38.dp,
            keyHorizontalPadding = 6.dp,
            keyCornerRadius = 16.dp,
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