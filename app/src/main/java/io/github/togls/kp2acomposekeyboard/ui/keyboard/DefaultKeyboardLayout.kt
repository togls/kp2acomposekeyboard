package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState

@Composable
fun DefaultKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dpCompat)
            .padding(
                horizontal = KeyboardMetrics.OuterPaddingHorizontal,
                vertical = KeyboardMetrics.OuterPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        val entryName = state.currentEntryName?.takeIf { it.isNotBlank() } ?: "未命名条目"

        if (state.hasActiveSession) {
            ExistingEntryHint(
                entryName = entryName,
                onIntent = onIntent,
            )
        }

        DefaultKeyboardUtilityRow(
            onIntent = onIntent,
        )

        when (state.defaultInputMode) {
            DefaultInputMode.Letters -> {
                LetterKeyboard(
                    state = state,
                    onIntent = onIntent,
                )
            }

            DefaultInputMode.Numbers -> {
                NumberKeyboard(
                    onIntent = onIntent,
                )
            }

            DefaultInputMode.Symbols -> {
                SymbolKeyboard(
                    onIntent = onIntent,
                )
            }
        }

        DefaultKeyboardActionRow(
            state = state,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun DefaultKeyboardActionRow(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
    ) {
        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = when (state.defaultInputMode) {
                DefaultInputMode.Letters -> "123"
                DefaultInputMode.Numbers -> "ABC"
                DefaultInputMode.Symbols -> "ABC"
            },
            onClick = {
                when (state.defaultInputMode) {
                    DefaultInputMode.Letters -> onIntent(KeyboardIntent.SwitchToNumbers)
                    DefaultInputMode.Numbers -> onIntent(KeyboardIntent.SwitchToLetters)
                    DefaultInputMode.Symbols -> onIntent(KeyboardIntent.SwitchToLetters)
                }
            },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = when (state.defaultInputMode) {
                DefaultInputMode.Letters -> "符号"
                DefaultInputMode.Numbers -> "符号"
                DefaultInputMode.Symbols -> "123"
            },
            onClick = {
                when (state.defaultInputMode) {
                    DefaultInputMode.Letters -> onIntent(KeyboardIntent.SwitchToSymbols)
                    DefaultInputMode.Numbers -> onIntent(KeyboardIntent.SwitchToSymbols)
                    DefaultInputMode.Symbols -> onIntent(KeyboardIntent.SwitchToNumbers)
                }
            },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(2f),
            text = "空格",
            onClick = { onIntent(KeyboardIntent.CommitText(" ")) },
        )

        KeyboardKey(
            modifier = Modifier.weight(1.6f),
            text = "选择条目",
            onClick = { onIntent(KeyboardIntent.SelectEntry) },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(1.2f),
            text = "换行",
            onClick = { onIntent(KeyboardIntent.Enter) },
            emphasis = KeyboardKeyEmphasis.Action,
        )
    }
}

@Composable
private fun DefaultKeyboardUtilityRow(
    onIntent: (KeyboardIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = "设置",
            onClick = { onIntent(KeyboardIntent.OpenSettings) },
            emphasis = KeyboardKeyEmphasis.Action,
        )
    }
}
