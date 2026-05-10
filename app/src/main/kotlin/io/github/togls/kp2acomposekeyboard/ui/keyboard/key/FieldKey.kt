package io.github.togls.kp2acomposekeyboard.ui.keyboard.key

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis

@Composable
internal fun FieldKey(
    field: KeyboardFieldUiModel,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = field.safeDisplayLabel(),
        onClick = { onIntent(KeyboardIntent.CommitField(field.id)) },
        emphasis = if (field.sensitive) {
            KeyboardKeyEmphasis.Sensitive
        } else {
            KeyboardKeyEmphasis.Normal
        },
    )
}

@Composable
private fun KeyboardFieldUiModel.safeDisplayLabel(): String {
    val label = label.trim()

    if (label.isBlank()) {
        return stringResource(R.string.entry_name_unnamed)
    }

    return label
}
