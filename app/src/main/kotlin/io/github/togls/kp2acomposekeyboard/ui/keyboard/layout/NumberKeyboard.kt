package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
    KeyboardWidthLayout(
        modifier = modifier,
        referenceKeyCount = 10,
    ) { widths ->
        val keyWidth = widths.standardKeyWidth

        val sideKeyWidth = widths.flexibleKeyWidth(
            fixedKeyCount = NumberKeyboardLastRow.size,
            flexibleKeyCount = 2,
        )

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
        ) {
            TextKeyRow(
                keys = NumberKeyboardRows[0],
                keyWidth = keyWidth,
                onIntent = onIntent,
            )

            TextKeyRow(
                keys = NumberKeyboardRows[1],
                keyWidth = keyWidth,
                onIntent = onIntent,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
            ) {

                KeyboardKey(
                    text = "=\\<",
                    onClick = {
                        onIntent(KeyboardIntent.SwitchToSymbols)
                    },
                    emphasis = KeyboardKeyEmphasis.Action,
                    modifier = Modifier
                        .width(sideKeyWidth),
                )

                NumberKeyboardLastRow.forEach { text ->
                    CommitTextKey(
                        text = text,
                        onIntent = onIntent,
                        modifier = Modifier
                            .width(keyWidth),
                    )
                }

                DeleteKey(
                    modifier = Modifier
                        .width(sideKeyWidth),
                    onIntent = onIntent,
                )
            }
        }
    }
}

private val NumberKeyboardRows = listOf(
    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
    listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "\\"),
)

private val NumberKeyboardLastRow = listOf("*", "\"", "'", ":", ";", "!", "?")
