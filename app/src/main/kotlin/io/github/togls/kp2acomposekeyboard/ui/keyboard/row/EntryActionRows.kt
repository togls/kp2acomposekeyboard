package io.github.togls.kp2acomposekeyboard.ui.keyboard.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ClearEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ExpandFieldsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.NextPageKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.PreviousPageKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SwitchToDefaultLayoutKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
fun PagedEntryActionRow(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
    ) {
        SwitchToDefaultLayoutKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.4f),
        )

        SelectEntryKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.5f),
        )

        PreviousPageKey(
            enabled = canGoPrevious,
            onClick = { onIntent(KeyboardIntent.PrevExtraFieldPage) },
            modifier = Modifier.weight(1f),
        )

        NextPageKey(
            enabled = canGoNext,
            onClick = { onIntent(KeyboardIntent.NextExtraFieldPage) },
            modifier = Modifier.weight(1f),
        )

        ExpandFieldsKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        ClearEntryKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        DeleteKey(
            modifier = Modifier.weight(1f),
            onIntent = onIntent,
        )
    }
}
