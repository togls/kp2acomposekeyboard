package io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardRow

@Composable
internal fun SymbolTextInputLayout(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        CommitTextKeyRow(
            keys = SymbolKeyboardRows[0],
            onIntent = onIntent,
        )

        CommitTextKeyRow(
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
                CommitTextKey(
                    text = text,
                    onIntent = onIntent,
                    modifier = Modifier.width(metrics.standardKeyWidth),
                )
            }

            DeleteKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )
        }
    }
}

private val SymbolKeyboardRows = listOf(
    listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
    listOf("_", "\\", "|", "~", "<", ">", "€", "£", "$", "·"),
)

private val SymbolKeyboardLastRow = listOf("/", ";", ":", "\"", "'", "`")
