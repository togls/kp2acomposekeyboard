package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun ExtraFieldPagedPanel(
    fields: List<KeyboardFieldUiModel>,
    pageIndex: Int,
    pageSize: Int,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safePageSize = pageSize.coerceAtLeast(1)
    val pageFields = fields
        .drop(pageIndex.coerceAtLeast(0) * safePageSize)
        .take(safePageSize)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dpCompat),
        verticalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        Text(
            text = "其余字段：",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (pageFields.isEmpty()) {
            Text(
                text = "没有其他字段",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
        ) {
            pageFields.forEach { field ->
                FieldButton(
                    modifier = Modifier.weight(1f),
                    field = field,
                    onIntent = onIntent,
                )
            }

            repeat(safePageSize - pageFields.size) {
                EmptyFieldSlot(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EmptyFieldSlot(
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier)
}