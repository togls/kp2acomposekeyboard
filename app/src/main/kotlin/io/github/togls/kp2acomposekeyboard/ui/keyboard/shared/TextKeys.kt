package io.github.togls.kp2acomposekeyboard.ui.keyboard.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardKeyEmphasis

@Composable
internal fun CommitTextKey(
    text: String,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = text,
        contentDescription = stringResource(R.string.cd_key_commit_text, text),
        onClick = { onIntent(KeyboardIntent.CommitText(text)) },
    )
}

@Composable
internal fun ShiftKey(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardIconKey(
        modifier = modifier,
        iconRes = R.drawable.ic_shift_24,
        contentDescription = stringResource(R.string.cd_key_shift),
        onClick = { onIntent(KeyboardIntent.ToggleUppercase) },
        emphasis = KeyboardKeyEmphasis.Action,
    )
}
