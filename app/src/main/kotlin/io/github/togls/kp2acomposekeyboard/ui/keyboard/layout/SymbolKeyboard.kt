package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
fun SymbolKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        TextKeyRow(
            keys = SymbolKeyboardRows[0],
            onIntent = onIntent,
        )

        TextKeyRow(
            keys = SymbolKeyboardRows[1],
            onIntent = onIntent,
        )

        KeyboardRow {
            KeyboardKey(
                text = "?123",
                onClick = {
                    onIntent(KeyboardIntent.SwitchToNumbers)
                },
                emphasis = KeyboardKeyEmphasis.Action,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )

            SymbolKeyboardLastRow.forEach { text ->
                KeyboardKey(
                    text = text,
                    modifier = Modifier.width(metrics.standardKeyWidth),
                    onClick = { onIntent(KeyboardIntent.CommitText(text)) },
                )
            }

            DeleteKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.sideKeyWidth),
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
            KeyboardKey(
                text = text,
                modifier = Modifier.width(metrics.standardKeyWidth),
                onClick = { onIntent(KeyboardIntent.CommitText(text)) },
            )
        }
    }
}

private val SymbolKeyboardRows = listOf(
    listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
    listOf("_", "\\", "|", "~", "<", ">", "€", "£", "$", "·"),
)

private val SymbolKeyboardLastRow = listOf("/", ";", ":", "\"", "'", "`")
