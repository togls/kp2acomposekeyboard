package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun SymbolKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        SymbolKeyRow(
            keys = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
            onIntent = onIntent,
        )

        SymbolKeyRow(
            keys = listOf("_", "\\", "|", "~", "<", ">", "€", "£", "$", "·"),
            onIntent = onIntent,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
        ) {
            listOf("/", ";", ":", "\"", "'", "`").forEach { text ->
                KeyboardKey(
                    modifier = Modifier.weight(1f),
                    text = text,
                    onClick = { onIntent(KeyboardIntent.CommitText(text)) },
                )
            }

            KeyboardKey(
                modifier = Modifier.weight(1.5f),
                text = "⌫",
                onClick = { onIntent(KeyboardIntent.DeleteBackward) },
                emphasis = KeyboardKeyEmphasis.Action,
            )
        }
    }
}

@Composable
private fun SymbolKeyRow(
    keys: List<String>,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        keys.forEach { text ->
            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = text,
                onClick = { onIntent(KeyboardIntent.CommitText(text)) },
            )
        }
    }
}