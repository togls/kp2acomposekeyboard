package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun AllFieldsExpandedPanel(
    fields: List<KeyboardFieldUiModel>,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        Text(
            text = "全部字段：",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 展开模式必须限制高度，否则字段过多时会把整个 IME 窗口撑高。
                .heightIn(max = 150.dpCompat)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dpCompat),
        ) {
            fields.chunked(FIELDS_PER_ROW).forEach { rowFields ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
                ) {
                    rowFields.forEach { field ->
                        FieldButton(
                            modifier = Modifier.weight(1f),
                            field = field,
                            onIntent = onIntent,
                        )
                    }

                    repeat(FIELDS_PER_ROW - rowFields.size) {
                        EmptyExpandedFieldSlot(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyExpandedFieldSlot(
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier)
}

private const val FIELDS_PER_ROW = 3