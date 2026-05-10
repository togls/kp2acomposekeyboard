package io.github.togls.kp2acomposekeyboard.ui.keyboard.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.LetterKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics

@Composable
internal fun LetterRow(
    letters: String,
    keyWidth: Dp,
    isUppercase: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.KeySpacing,
            alignment = Alignment.Companion.CenterHorizontally,
        ),
    ) {
        letters.forEach { letter ->
            LetterKey(
                modifier = Modifier.Companion.width(keyWidth),
                letter = letter,
                isUppercase = isUppercase,
                onIntent = onIntent,
            )
        }
    }
}