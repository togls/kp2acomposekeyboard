package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.TextKeyRow

@Composable
fun SymbolKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            SymbolKeyboardLastRow.forEach { text ->
                KeyboardKey(
                    modifier = Modifier.weight(1f),
                    text = text,
                    onClick = { onIntent(KeyboardIntent.CommitText(text)) },
                )
            }

            DeleteKey(
                modifier = Modifier.weight(1.5f),
                onIntent = onIntent,
            )
        }
    }
}

private val SymbolKeyboardRows = listOf(
    listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
    listOf("_", "\\", "|", "~", "<", ">", "€", "£", "$", "·"),
)

private val SymbolKeyboardLastRow = listOf("/", ";", ":", "\"", "'", "`")

@Composable
private fun SymbolKeyRow(
    keys: List<String>,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
    ) {
        keys.forEach { text ->
            CommitTextKey(
                text = text,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}