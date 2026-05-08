package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun ExistingEntryHint(
    entryName: String,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 0.dp),
                text = "当前已有条目：$entryName",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            TextButton(
                onClick = { onIntent(KeyboardIntent.SwitchToEntryLayout) },
            ) {
                Text(text = "切回条目布局")
            }
        }
    }
}