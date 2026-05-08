package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout

@Composable
fun KeyboardRoot(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
