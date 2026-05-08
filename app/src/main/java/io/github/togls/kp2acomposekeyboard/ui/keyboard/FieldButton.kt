package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun FieldButton(
    field: KeyboardFieldUiModel,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier,
        text = field.label.ifBlank { "未命名字段" },
        onClick = { onIntent(KeyboardIntent.CommitField(field.id)) },
        emphasis = if (field.sensitive) {
            KeyboardKeyEmphasis.Action
        } else {
            KeyboardKeyEmphasis.Normal
        },
    )
}