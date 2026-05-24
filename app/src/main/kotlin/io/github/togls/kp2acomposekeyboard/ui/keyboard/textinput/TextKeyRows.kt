package io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardRow

@Composable
internal fun CommitTextKeyRow(
    keys: List<String>,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    KeyboardRow(modifier = modifier) {
        keys.forEach { text ->
            CommitTextKey(
                modifier = Modifier.width(metrics.standardKeyWidth),
                text = text,
                onIntent = onIntent,
            )
        }
    }
}
