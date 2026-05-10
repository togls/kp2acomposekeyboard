package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Immutable
internal data class KeyboardKeyWidths(
    val availableWidth: Dp,
    val standardKeyWidth: Dp,
    val keySpacing: Dp,
) {
    /**
     * Calculates the width for flexible keys after fixed-width keys and gaps are removed.
     *
     * Example:
     * Shift + 7 fixed letter keys + Delete = 9 keys, therefore 8 gaps.
     * Shift and Delete share the remaining width equally.
     */
    fun flexibleKeyWidth(
        fixedKeyCount: Int,
        flexibleKeyCount: Int,
    ): Dp {
        require(fixedKeyCount >= 0) { "fixedKeyCount must be >= 0." }
        require(flexibleKeyCount > 0) { "flexibleKeyCount must be > 0." }

        val gapCount = fixedKeyCount + flexibleKeyCount - 1

        val fixedKeysWidth = standardKeyWidth * fixedKeyCount.toFloat()
        val gapsWidth = keySpacing * gapCount.toFloat()

        return (availableWidth - fixedKeysWidth - gapsWidth) / flexibleKeyCount.toFloat()
    }
}

@Composable
internal fun KeyboardWidthLayout(
    modifier: Modifier = Modifier,
    referenceKeyCount: Int = 10,
    content: @Composable (KeyboardKeyWidths) -> Unit,
) {
    require(referenceKeyCount > 0) { "referenceKeyCount must be > 0." }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val keySpacing = KeyboardMetrics.KeySpacing
        val gapCount = referenceKeyCount - 1

        // The reference row defines the standard key width.
        // For qwertyuiop / 1234567890, this is 10 keys and 9 gaps.
        val standardKeyWidth =
            (maxWidth - keySpacing * gapCount.toFloat()) / referenceKeyCount.toFloat()

        content(
            KeyboardKeyWidths(
                availableWidth = maxWidth,
                standardKeyWidth = standardKeyWidth,
                keySpacing = keySpacing,
            ),
        )
    }
}