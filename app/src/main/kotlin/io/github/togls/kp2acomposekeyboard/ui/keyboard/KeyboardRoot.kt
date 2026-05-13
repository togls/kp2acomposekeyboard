package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardFrame
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.isKeyboardLandscape
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.toKeyboardHeight


/**
 * Root container for the IME keyboard UI.
 *
 * It resolves orientation-aware keyboard metrics, owns the keyboard background,
 * and keeps the interactive key area above the system navigation bar.
 */
@Composable
fun KeyboardRoot(
    state: KeyboardUiState,
    settings: KeyboardSettings,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLandscape = isKeyboardLandscape()

    val adaptiveMetrics = KeyboardAdaptiveMetrics.resolve(
        heightMode = settings.keyboardHeightMode,
        isLandscape = isLandscape,
    )

    CompositionLocalProvider(
        LocalKeyboardAdaptiveMetrics provides adaptiveMetrics,
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(settings.keyboardHeightMode.toKeyboardHeight(isLandscape))
                // The IME window is controlled by the system. Clip overflow to avoid
                // accidentally expanding the input view on compact landscape screens.
                .clipToBounds(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            KeyboardFrame(
                state = state,
                adaptiveMetrics = adaptiveMetrics,
                isLandscape = isLandscape,
                onIntent = onIntent,
            )
        }
    }
}

