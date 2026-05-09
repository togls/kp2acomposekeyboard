package io.github.togls.kp2acomposekeyboard.ui.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings
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
                // 横屏空间更紧张，先裁剪溢出内容，避免异常撑高 IME 窗口。
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
}
