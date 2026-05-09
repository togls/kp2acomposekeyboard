package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.DeleteKey

@Composable
fun NumberKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        NumberKeyRow(
            keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            onIntent = onIntent,
        )

        NumberKeyRow(
            keys = listOf("-", "/", ":", ";", "(", ")", "¥", "&", "@", "\""),
            onIntent = onIntent,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
        ) {
            listOf(".", ",", "?", "!", "'").forEach { text ->
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

@Composable
private fun NumberKeyRow(
    keys: List<String>,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
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