package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.dpCompat

@Composable
fun EntryHeader(
    entryName: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dpCompat, vertical = 8.dpCompat),
            text = "当前条目：${entryName?.takeIf { it.isNotBlank() } ?: "未命名条目"}",
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}