package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.domain.keyboard.TextInputMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.EnterKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.KeyboardKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.LanguageSwitchKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SpaceKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun DefaultKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutMetrics = LocalKeyboardLayoutMetrics.current
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current

    CompositionLocalProvider(
        LocalKeyboardAdaptiveMetrics provides adaptiveMetrics.copy(
            keyHeight = layoutMetrics.keyboardRowHeight,
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = KeyboardMetrics.OuterPaddingHorizontal,
                    vertical = KeyboardMetrics.OuterPaddingVertical,
                )
                .testTag(KeyboardTestTags.DefaultContent),
            verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
        ) {

            DefaultKeyboardContent(
                state = state,
                onIntent = onIntent,
            )

            DefaultKeyboardActionRow(
                state = state,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun DefaultKeyboardContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    when (state.textInputMode) {
        TextInputMode.Letters -> {
            LetterKeyboard(
                state = state,
                onIntent = onIntent,
            )
        }

        TextInputMode.Numbers -> {
            NumberKeyboard(
                onIntent = onIntent,
            )
        }

        TextInputMode.Symbols -> {
            SymbolKeyboard(
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun DefaultKeyboardActionRow(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    KeyboardRow {
        KeyboardKey(
            modifier = Modifier
                .width(metrics.sideKeyWidth)
                .testTag(KeyboardTestTags.DefaultSwitchKey),
            text = when (state.textInputMode) {
                TextInputMode.Letters -> "?123"
                TextInputMode.Numbers -> "ABC"
                TextInputMode.Symbols -> "ABC"
            },
            onClick = {
                when (state.textInputMode) {
                    TextInputMode.Letters -> onIntent(KeyboardIntent.SwitchToNumbers)
                    TextInputMode.Numbers -> onIntent(KeyboardIntent.SwitchToLetters)
                    TextInputMode.Symbols -> onIntent(KeyboardIntent.SwitchToLetters)
                }
            },
            emphasis = KeyboardKeyEmphasis.Action,
        )

        if (state.textInputMode == TextInputMode.Letters) {
            LanguageSwitchKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.standardKeyWidth),
            )
        }

        SpaceKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        SelectEntryKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.standardKeyWidth),
        )

        EnterKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.sideKeyWidth),
        )
    }
}
