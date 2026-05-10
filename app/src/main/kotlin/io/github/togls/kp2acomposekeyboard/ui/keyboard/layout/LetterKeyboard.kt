package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.LetterKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ShiftKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.row.LetterRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics

@Composable
fun LetterKeyboard(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardWidthLayout(
        modifier = modifier,
        referenceKeyCount = 10,
    ) { widths ->
        val letterKeyWidth = widths.standardKeyWidth

        // Shift + 7 letters + Delete = 9 keys, therefore 8 gaps.
        val sideKeyWidth = widths.flexibleKeyWidth(
            fixedKeyCount = 7,
            flexibleKeyCount = 2,
            gapCount = 8,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
        ) {
            LetterRow(
                letters = "qwertyuiop",
                keyWidth = letterKeyWidth,
                isUppercase = state.isUppercase,
                onIntent = onIntent,
            )

            LetterRow(
                letters = "asdfghjkl",
                keyWidth = letterKeyWidth,
                isUppercase = state.isUppercase,
                onIntent = onIntent,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = KeyboardMetrics.KeySpacing,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                ShiftKey(
                    onIntent = onIntent,
                    modifier = Modifier.width(sideKeyWidth),
                )

                "zxcvbnm".forEach { letter ->
                    LetterKey(
                        modifier = Modifier.width(letterKeyWidth),
                        letter = letter,
                        isUppercase = state.isUppercase,
                        onIntent = onIntent,
                    )
                }

                DeleteKey(
                    modifier = Modifier.width(sideKeyWidth),
                    onIntent = onIntent,
                )
            }
        }
    }
}

