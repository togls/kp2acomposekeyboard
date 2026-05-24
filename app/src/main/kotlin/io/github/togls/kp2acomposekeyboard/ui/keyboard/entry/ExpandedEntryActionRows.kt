package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics

@Composable
internal fun ExpandedEntryActionRows(
    canScrollUp: Boolean,
    canScrollDown: Boolean,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            SelectEntryKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )

            SwitchToTextInputKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.standardKeyWidth),
            )

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
                modifier = Modifier.width(metrics.standardKeyWidth),
            )

            DeleteKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )
        }
    }
}
