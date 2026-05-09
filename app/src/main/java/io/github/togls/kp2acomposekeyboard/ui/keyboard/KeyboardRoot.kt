package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings

@Composable
fun KeyboardRoot(
    state: KeyboardUiState,
    settings: KeyboardSettings,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(settings.keyboardHeightMode.toKeyboardHeight())
            .clipToBounds(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        when (state.mainLayout) {
            MainKeyboardLayout.Default -> {
                DefaultKeyboardLayout(
                    state = state,
                    onIntent = onIntent,
                )
            }

            MainKeyboardLayout.Entry -> {
                EntryKeyboardLayout(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}