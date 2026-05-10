package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.domain.KeyboardFieldUiModel
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics

@Composable
fun AllFieldsExpandedPanel(
    fields: List<KeyboardFieldUiModel>,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        Text(
            text = stringResource(R.string.entry_fields_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            fields.chunked(FIELDS_PER_ROW).forEach { rowFields ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
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
    Box(modifier = modifier)
}

private const val FIELDS_PER_ROW = 3