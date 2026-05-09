package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.settings.KeyboardHeightMode

fun KeyboardHeightMode.toKeyboardHeight(): Dp {
    return when (this) {
        KeyboardHeightMode.Compact -> 260.dp
        KeyboardHeightMode.Normal -> 300.dp
        KeyboardHeightMode.Tall -> 340.dp
    }
}

// KeyboardHeightMode.Compact -> 280.dp
// KeyboardHeightMode.Normal -> 320.dp
// KeyboardHeightMode.Tall -> 360.dp