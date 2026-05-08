package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState

@Composable
fun LetterKeyboard(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dpCompat),
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
            horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
        ) {
            KeyboardKey(
                modifier = Modifier.weight(1.2f),
                text = "⇧",
                onClick = { onIntent(KeyboardIntent.ToggleUppercase) },
                emphasis = KeyboardKeyEmphasis.Action,
            )

            "zxcvbnm".forEach { letter ->
                LetterKey(
                    modifier = Modifier.weight(1f),
                    letter = letter,
                    isUppercase = state.isUppercase,
                    onIntent = onIntent,
                )
            }

            KeyboardKey(
                modifier = Modifier.weight(1.2f),
                text = "⌫",
                onClick = { onIntent(KeyboardIntent.DeleteBackward) },
                emphasis = KeyboardKeyEmphasis.Action,
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

    KeyboardKey(
        modifier = modifier,
        text = text,
        onClick = { onIntent(KeyboardIntent.CommitText(text)) },
    )
}

private fun Char.toInputText(isUppercase: Boolean): String {
    return if (isUppercase) {
        uppercaseChar().toString()
    } else {
        toString()
    }
}