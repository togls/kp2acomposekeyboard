package io.github.togls.kp2acomposekeyboard.ui.keyboard.style

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardHeightMode

internal fun KeyboardHeightMode.toKeyboardHeight(
    isLandscape: Boolean,
): Dp {
    if (isLandscape) {
        return when (this) {
            KeyboardHeightMode.Compact -> 220.dp
            KeyboardHeightMode.Normal -> 250.dp
            KeyboardHeightMode.Tall -> 280.dp
        }
    }

    return when (this) {
        KeyboardHeightMode.Compact -> 260.dp
        KeyboardHeightMode.Normal -> 300.dp
        KeyboardHeightMode.Tall -> 340.dp
    }
}
