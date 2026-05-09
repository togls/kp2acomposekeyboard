package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.ClearEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.SettingsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.SwitchToDefaultLayoutKey

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

        SettingsKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "prev",
            enabled = canGoPrevious,
            onClick = { onIntent(KeyboardIntent.PrevExtraFieldPage) },
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "next",
            enabled = canGoNext,
            onClick = { onIntent(KeyboardIntent.NextExtraFieldPage) },
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "全部",
            onClick = { onIntent(KeyboardIntent.ExpandFields) },
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

@Composable
fun ExpandedEntryActionRows(
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
            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "prev",
                enabled = canScrollUp,
                onClick = onScrollUp,
            )

            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "next",
                enabled = canScrollDown,
                onClick = onScrollDown,
            )

            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "收起",
                onClick = { onIntent(KeyboardIntent.CollapseFields) },
                emphasis = KeyboardKeyEmphasis.Action,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            SwitchToDefaultLayoutKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1.8f),
            )

            SelectEntryKey(
                onIntent = onIntent,
                modifier = Modifier.weight(1.5f),
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
}