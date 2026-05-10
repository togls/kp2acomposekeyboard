package io.github.togls.kp2acomposekeyboard.ui.keyboard.key

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

class LetterKey {
}

@Composable
internal fun LetterKey(
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