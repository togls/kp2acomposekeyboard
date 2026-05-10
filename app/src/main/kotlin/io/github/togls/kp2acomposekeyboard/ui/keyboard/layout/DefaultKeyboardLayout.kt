package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.ExistingEntryHint
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.EnterKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SettingsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SpaceKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.token.isKeyboardLandscape

@Composable
fun DefaultKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = KeyboardMetrics.OuterPaddingHorizontal,
                vertical = KeyboardMetrics.OuterPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        val isLandscape = isKeyboardLandscape()

        if (state.hasActiveSession) {
            ExistingEntryHint(
                entryName = state.currentEntryName?.takeIf { it.isNotBlank() } ?: "未命名条目",
                onIntent = onIntent,
                modifier = Modifier.height(adaptiveMetrics.keyHeight),
            )
        }

        if (!isLandscape) {
            DefaultKeyboardUtilityRow(
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
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
    ) {
        KeyboardKey(
            modifier = Modifier.weight(1f),
            text = when (state.defaultInputMode) {
                DefaultInputMode.Letters -> "?123"
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

        SpaceKey(
            onIntent = onIntent,
            modifier = Modifier.weight(3f),
        )

        SelectEntryKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.5f),
        )

        EnterKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1.2f),
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
        SettingsKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )
    }
}
