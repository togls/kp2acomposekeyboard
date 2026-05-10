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
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.CommitTextKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics

@Composable
internal fun TextKeyRow(
    keys: List<String>,
    keyWidth: Dp,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.KeySpacing,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        keys.forEach { text ->
            CommitTextKey(
                modifier = Modifier
                    .width(keyWidth),
                text = text,
                onIntent = onIntent,
            )
        }
    }
}
