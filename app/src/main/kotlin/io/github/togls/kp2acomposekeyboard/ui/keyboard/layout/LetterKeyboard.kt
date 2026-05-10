package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.ShiftKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.KeyboardMetrics

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

@Composable
private fun LetterRow(
    letters: String,
    keyWidth: Dp,
    isUppercase: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.KeySpacing,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        letters.forEach { letter ->
            LetterKey(
                modifier = Modifier.width(keyWidth),
                letter = letter,
                isUppercase = isUppercase,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun LetterKey(
    letter: Char,
    isUppercase: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = letter.toInputText(isUppercase)

    CommitTextKey(
        text = text,
        onIntent = onIntent,
        modifier = modifier,
    )
}

private fun Char.toInputText(isUppercase: Boolean): String {
    return if (isUppercase) {
        uppercaseChar().toString()
    } else {
        toString()
    }
}