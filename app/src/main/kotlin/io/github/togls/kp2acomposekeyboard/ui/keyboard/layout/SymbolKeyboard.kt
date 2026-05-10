package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.row.TextKeyRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
fun SymbolKeyboard(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardWidthLayout(
        modifier = modifier,
        referenceKeyCount = 10,
    ) { widths ->
        val keyWidth = widths.standardKeyWidth

        val sideKeyWidth = widths.flexibleKeyWidth(
            fixedKeyCount = SymbolKeyboardLastRow.size,
            flexibleKeyCount = 2,
        )

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
        ) {
            TextKeyRow(
                keys = SymbolKeyboardRows[0],
                keyWidth = keyWidth,
                onIntent = onIntent,
            )

            TextKeyRow(
                keys = SymbolKeyboardRows[1],
                keyWidth = keyWidth,
                onIntent = onIntent,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
            ) {
                KeyboardKey(
                    text = "?123",
                    onClick = {
                        onIntent(KeyboardIntent.SwitchToNumbers)
                    },
                    emphasis = KeyboardKeyEmphasis.Action,
                    modifier = Modifier
                        .width(sideKeyWidth),
                )

                SymbolKeyboardLastRow.forEach { text ->
                    KeyboardKey(
                        text = text,
                        modifier = Modifier
                            .width(keyWidth),
                        onClick = { onIntent(KeyboardIntent.CommitText(text)) },
                    )
                }

                DeleteKey(
                    onIntent = onIntent,
                    modifier = Modifier
                        .width(sideKeyWidth),
                )
            }
        }
    }
}

private val SymbolKeyboardRows = listOf(
    listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
    listOf("_", "\\", "|", "~", "<", ">", "€", "£", "$", "·"),
)

private val SymbolKeyboardLastRow = listOf("/", ";", ":", "\"", "'", "`")
