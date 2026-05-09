package io.github.togls.kp2acomposekeyboard.ui.keyboard.keys

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
internal fun CommitTextKey(
    text: String,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = text,
        contentDescription = "输入 $text",
        onClick = { onIntent(KeyboardIntent.CommitText(text)) },
    )
}

@Composable
internal fun ShiftKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = "⇧",
        contentDescription = "切换大小写",
        onClick = { onIntent(KeyboardIntent.ToggleUppercase) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}