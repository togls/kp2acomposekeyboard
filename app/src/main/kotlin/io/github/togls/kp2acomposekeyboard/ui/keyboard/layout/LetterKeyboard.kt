package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.ShiftKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.dpCompat

@Composable
fun LetterKeyboard(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.RowSpacing,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        LetterRow(
            letters = "qwertyuiop",
            isUppercase = state.isUppercase,
            onIntent = onIntent,
        )

        LetterRow(
            letters = "asdfghjkl",
            isUppercase = state.isUppercase,
            onIntent = onIntent,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            ShiftKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1.2f),
            )

            "zxcvbnm".forEach { letter ->
                LetterKey(
                    modifier = Modifier.weight(1f),
                    letter = letter,
                    isUppercase = state.isUppercase,
                    onIntent = onIntent,
                )
            }

            DeleteKey(
                modifier = Modifier.weight(1.2f),
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun LetterRow(
    letters: String,
    isUppercase: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        letters.forEach { letter ->
            LetterKey(
                modifier = Modifier.weight(1f),
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