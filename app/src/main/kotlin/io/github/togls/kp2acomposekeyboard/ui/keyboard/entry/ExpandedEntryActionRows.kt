package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.ClearEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.CollapseFieldsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.NextPageKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.PreviousPageKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.SwitchToTextInputKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics

@Composable
internal fun ExpandedEntryActionRows(
    canScrollUp: Boolean,
    canScrollDown: Boolean,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            PreviousPageKey(
                enabled = canScrollUp,
                onClick = onScrollUp,
                modifier = Modifier.weight(1f),
            )

            NextPageKey(
                enabled = canScrollDown,
                onClick = onScrollDown,
                modifier = Modifier.weight(1f),
            )

            CollapseFieldsKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            SwitchToTextInputKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1.8f),
            )

            SelectEntryKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1.5f),
            )

            DeleteKey(
                modifier = Modifier.weight(1f),
                onIntent = onIntent,
            )
        }
    }
}
