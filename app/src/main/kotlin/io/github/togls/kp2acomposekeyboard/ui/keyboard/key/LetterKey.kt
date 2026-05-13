package io.github.togls.kp2acomposekeyboard.ui.keyboard.key

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags

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
        modifier = modifier.testTag(KeyboardTestTags.letterKey(letter)),
    )
}

private fun Char.toInputText(isUppercase: Boolean): String {
    return if (isUppercase) {
        uppercaseChar().toString()
    } else {
        toString()
    }
}
