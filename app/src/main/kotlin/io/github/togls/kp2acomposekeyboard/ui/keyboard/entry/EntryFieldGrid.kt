package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardFieldSummary
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.FieldKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardRow

@Composable
internal fun EntryFieldGrid(
    fields: List<KeyboardFieldSummary>,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = EntryFieldColumnCount,
) {
    require(columns >= 1) { "columns must be >= 1." }

    val metrics = LocalKeyboardLayoutMetrics.current
    val fieldWidth = metrics.fieldKeyWidth(columns)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        fields.chunked(columns).forEach { rowFields ->
            KeyboardRow {
                rowFields.forEach { field ->
                    FieldKey(
                        modifier = Modifier.width(fieldWidth),
                        field = field,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }
}

internal const val EntryFieldColumnCount = 3
