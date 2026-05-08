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
            .padding(horizontal = 8.dpCompat, vertical = 8.dpCompat),
        verticalArrangement = Arrangement.spacedBy(8.dpCompat),
    ) {
        if (state.hasActiveSession && state.currentEntryName != null) {
            ExistingEntryHint(
                entryName = state.currentEntryName,
                onIntent = onIntent,
            )
        }

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
        horizontalArrangement = Arrangement.spacedBy(6.dpCompat),
    ) {
        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = if (state.defaultInputMode == DefaultInputMode.Numbers) "ABC" else "123",
            onClick = {
                if (state.defaultInputMode == DefaultInputMode.Numbers) {
                    onIntent(KeyboardIntent.SwitchToLetters)
                } else {
                    onIntent(KeyboardIntent.SwitchToNumbers)
                }
            },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = if (state.defaultInputMode == DefaultInputMode.Symbols) "ABC" else "符号",
            onClick = {
                if (state.defaultInputMode == DefaultInputMode.Symbols) {
                    onIntent(KeyboardIntent.SwitchToLetters)
                } else {
                    onIntent(KeyboardIntent.SwitchToSymbols)
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