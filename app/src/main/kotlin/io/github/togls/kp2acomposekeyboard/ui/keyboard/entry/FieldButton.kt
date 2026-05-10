package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.style.KeyboardKeyEmphasis

@Composable
fun FieldButton(
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

private fun KeyboardFieldUiModel.safeDisplayLabel(): String {
    val label = label.trim()

    if (label.isBlank()) {
        return "未命名字段"
    }

    return label
}