package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

internal val LocalKeyboardLayoutMetrics = compositionLocalOf {
    calculateKeyboardLayoutMetrics(
        KeyboardLayoutInput(
            totalWidth = 0.dp,
            totalHeight = 0.dp,
            candidateRowHeight = 0.dp,
            horizontalPadding = 0.dp,
            verticalOuterPadding = 0.dp,
            keySpacing = 0.dp,
            rowSpacing = 0.dp,
            bottomSpacerHeight = 0.dp,
            navigationSpacerHeight = 0.dp,
            sideKeyStandardKeyCount = 7,
        ),
    )
}
