package io.github.togls.kp2acomposekeyboard.ui.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.DefaultKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.EntryKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.KeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.toKeyboardHeight

@Composable
fun KeyboardRoot(
    state: KeyboardUiState,
    settings: KeyboardSettings,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val adaptiveMetrics = if (isLandscape) {
        KeyboardAdaptiveMetrics.Landscape
    } else {
        KeyboardAdaptiveMetrics.Portrait
    }

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
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds(),
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

                KeyboardBottomGap(isLandscape = isLandscape)

                KeyboardNavigationBarSpacer()
            }
        }
    }
}

/**
 * Adds a small visual gap between the key area and the system navigation bar.
 *
 * This is intentionally separate from [KeyboardNavigationBarSpacer], because
 * the navigation bar spacer should only represent the real system inset.
 */
@Composable
private fun KeyboardBottomGap(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val height = if (isLandscape) {
        0.dp
    } else {
        16.dp
    }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    )
}

/**
 * Reserves the bottom navigation bar area for the IME.
 *
 * The keyboard background still comes from the parent Surface, while the
 * interactive key area stays above the system navigation bar.
 */
@Composable
private fun KeyboardNavigationBarSpacer(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars),
    )
}