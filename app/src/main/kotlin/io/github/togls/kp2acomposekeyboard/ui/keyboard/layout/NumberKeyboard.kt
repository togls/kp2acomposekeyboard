package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.row.TextKeyRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics

@Composable
internal fun NumberKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {

            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "=\\<",
                onClick = {
                    onIntent(KeyboardIntent.SwitchToLetters)
                },
                emphasis = KeyboardKeyEmphasis.Action,
            )

            NumberKeyboardLastRow.forEach { text ->
                CommitTextKey(
                    modifier = Modifier.weight(1f),
                    text = text,
                    onIntent = onIntent,
                )
            }

            DeleteKey(
                modifier = Modifier.weight(1.5f),
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
