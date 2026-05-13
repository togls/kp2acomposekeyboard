package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
internal fun NumberKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        TextKeyRow(
            keys = NumberKeyboardRows[0],
            onIntent = onIntent,
        )

        TextKeyRow(
            keys = NumberKeyboardRows[1],
            onIntent = onIntent,
        )

        KeyboardRow {
            KeyboardKey(
                text = "=\\<",
                onClick = {
                    onIntent(KeyboardIntent.SwitchToSymbols)
                },
                emphasis = KeyboardKeyEmphasis.Action,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )

            NumberKeyboardLastRow.forEach { text ->
                CommitTextKey(
                    text = text,
                    onIntent = onIntent,
                    modifier = Modifier.width(metrics.standardKeyWidth),
                )
            }

            DeleteKey(
                modifier = Modifier.width(metrics.sideKeyWidth),
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun TextKeyRow(
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

private val NumberKeyboardRows = listOf(
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
    listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "\\"),
)

private val NumberKeyboardLastRow = listOf("*", "\"", "'", ":", ";", "!", "?")
