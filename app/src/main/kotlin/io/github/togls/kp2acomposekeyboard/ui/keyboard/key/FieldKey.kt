package io.github.togls.kp2acomposekeyboard.ui.keyboard.key

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis

@Composable
internal fun FieldKey(
    field: KeyboardFieldSummary,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeyboardKey(
        modifier = modifier.testTag(KeyboardTestTags.field(field.id)),
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
private fun KeyboardFieldSummary.safeDisplayLabel(): String {
    val trimmedLabel = label.trim()

    if (trimmedLabel.isBlank()) {
        return stringResource(R.string.entry_name_unnamed)
    }

    return trimmedLabel
}
