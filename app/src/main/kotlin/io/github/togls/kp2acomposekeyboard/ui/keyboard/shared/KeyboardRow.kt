package io.github.togls.kp2acomposekeyboard.ui.keyboard.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics

@Composable
internal fun KeyboardRow(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = KeyboardMetrics.KeySpacing,
            alignment = horizontalAlignment,
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
