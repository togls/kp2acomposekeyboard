package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent

@Composable
fun PagedEntryActionRow(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        KeyboardKey(
            modifier = Modifier.weight(1.8f),
            text = "切换默认布局",
            onClick = { onIntent(KeyboardIntent.SwitchToDefaultLayout) },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(1.5f),
            text = "选择条目",
            onClick = { onIntent(KeyboardIntent.SelectEntry) },
            emphasis = KeyboardKeyEmphasis.Action,
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

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "清除",
            onClick = { onIntent(KeyboardIntent.ClearEntry) },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "⌫",
            onClick = { onIntent(KeyboardIntent.DeleteBackward) },
            emphasis = KeyboardKeyEmphasis.Action,
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
        verticalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
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
            horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
        ) {
            KeyboardKey(
                modifier = Modifier.weight(1.8f),
                text = "切换默认布局",
                onClick = { onIntent(KeyboardIntent.SwitchToDefaultLayout) },
                emphasis = KeyboardKeyEmphasis.Action,
            )

            KeyboardKey(
                modifier = Modifier.weight(1.5f),
                text = "选择条目",
                onClick = { onIntent(KeyboardIntent.SelectEntry) },
                emphasis = KeyboardKeyEmphasis.Action,
            )

            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "清除",
                onClick = { onIntent(KeyboardIntent.ClearEntry) },
                emphasis = KeyboardKeyEmphasis.Action,
            )

            KeyboardKey(
                modifier = Modifier.weight(1f),
                text = "⌫",
                onClick = { onIntent(KeyboardIntent.DeleteBackward) },
                emphasis = KeyboardKeyEmphasis.Action,
            )
        }
    }
}