package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Immutable
internal data class KeyboardLayoutInput(
    val totalWidth: Dp,
    val totalHeight: Dp,
    val candidateRowHeight: Dp,
    val horizontalPadding: Dp,
    val verticalOuterPadding: Dp,
    val keySpacing: Dp,
    val rowSpacing: Dp,
    val bottomSpacerHeight: Dp,
    val navigationSpacerHeight: Dp,
    val sideKeyStandardKeyCount: Int,
    val pixelSnapDensity: Float? = null,
)

@Immutable
internal data class KeyboardLayoutMetrics(
    val standardKeyWidth: Dp,
    val sideKeyWidth: Dp,
    val keyboardRowHeight: Dp,
    val remainingFieldsAreaHeight: Dp,
    private val availableWidth: Dp,
    private val keySpacing: Dp,
    private val pixelSnapDensity: Float?,
) {
    fun fieldKeyWidth(columns: Int): Dp {
        require(columns >= 1) { "columns must be >= 1." }

        return ((availableWidth - keySpacing * (columns - 1).toFloat()) / columns.toFloat())
            .coerceAtLeast(0.dp)
            .snapDownToPixel(pixelSnapDensity)
    }
}

internal fun calculateKeyboardLayoutMetrics(
    input: KeyboardLayoutInput,
): KeyboardLayoutMetrics {
    val availableWidth = (input.totalWidth - input.horizontalPadding * 2f)
        .coerceAtLeast(0.dp)
    val standardKeyWidth = ((availableWidth - input.keySpacing * STANDARD_GAP_COUNT) /
            STANDARD_KEY_COUNT.toFloat())
        .coerceAtLeast(0.dp)
        .snapDownToPixel(input.pixelSnapDensity)
    val sideKeyWidth = sideKeyWidth(
        availableWidth = availableWidth,
        standardKeyWidth = standardKeyWidth,
        keySpacing = input.keySpacing,
        standardKeyCount = input.sideKeyStandardKeyCount,
        pixelSnapDensity = input.pixelSnapDensity,
    )

    // Candidate, bottom, and navigation areas live outside the four keyboard rows.
    val keyboardAreaHeight = input.totalHeight -
            input.candidateRowHeight -
            input.verticalOuterPadding * 2f -
            input.bottomSpacerHeight -
            input.navigationSpacerHeight
    val keyboardRowHeight = ((keyboardAreaHeight - input.rowSpacing * KEYBOARD_ROW_GAP_COUNT) /
            KEYBOARD_ROW_COUNT.toFloat())
        .coerceAtLeast(0.dp)
        .snapDownToPixel(input.pixelSnapDensity)

    return KeyboardLayoutMetrics(
        standardKeyWidth = standardKeyWidth,
        sideKeyWidth = sideKeyWidth,
        keyboardRowHeight = keyboardRowHeight,
        remainingFieldsAreaHeight = keyboardRowHeight * REMAINING_FIELD_ROW_COUNT.toFloat() +
                input.rowSpacing,
        availableWidth = availableWidth,
        keySpacing = input.keySpacing,
        pixelSnapDensity = input.pixelSnapDensity,
    )
}

private fun sideKeyWidth(
    availableWidth: Dp,
    standardKeyWidth: Dp,
    keySpacing: Dp,
    standardKeyCount: Int,
    pixelSnapDensity: Float?,
): Dp {
    require(standardKeyCount >= 0) { "standardKeyCount must be >= 0." }

    val totalKeyCount = standardKeyCount + SIDE_KEY_COUNT
    val gapCount = (totalKeyCount - 1).coerceAtLeast(0)
    return ((availableWidth -
            standardKeyWidth * standardKeyCount.toFloat() -
            keySpacing * gapCount.toFloat()) / SIDE_KEY_COUNT.toFloat())
        .coerceAtLeast(0.dp)
        .snapDownToPixel(pixelSnapDensity)
}

private fun Dp.snapDownToPixel(density: Float?): Dp {
    if (density == null || density <= 0f) {
        return this
    }

    // Repeated fixed-width keys are measured independently; snapping down avoids
    // cumulative rounding overflow that would clip the trailing key.
    return (floor(value * density) / density).dp
}

private const val STANDARD_KEY_COUNT = 10
private const val STANDARD_GAP_COUNT = STANDARD_KEY_COUNT - 1
private const val KEYBOARD_ROW_COUNT = 4
private const val KEYBOARD_ROW_GAP_COUNT = KEYBOARD_ROW_COUNT - 1
private const val REMAINING_FIELD_ROW_COUNT = 2
private const val SIDE_KEY_COUNT = 2
