package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardBottomGap
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardNavigationBarSpacer
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics

@Composable
internal fun KeyboardFrame(
    state: KeyboardUiState,
    adaptiveMetrics: KeyboardAdaptiveMetrics,
    isLandscape: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        KeyboardContentArea(
            state = state,
            adaptiveMetrics = adaptiveMetrics,
            isLandscape = isLandscape,
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        KeyboardBottomGap(isLandscape = isLandscape)
        KeyboardNavigationBarSpacer()
    }
}
