package io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.LetterKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.ShiftKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardRow

@Composable
internal fun LetterTextInputLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        LetterKeyRow(
            letters = "qwertyuiop",
            isUppercase = state.isUppercase,
            onIntent = onIntent,
            modifier = Modifier.testTag(KeyboardTestTags.LetterReferenceRow),
        )

        LetterKeyRow(
            letters = "asdfghjkl",
            isUppercase = state.isUppercase,
            onIntent = onIntent,
        )

        KeyboardRow {
            ShiftKey(
                onIntent = onIntent,
                modifier = Modifier.width(metrics.sideKeyWidth),
            )

            "zxcvbnm".forEach { letter ->
                LetterKey(
                    modifier = Modifier.width(metrics.standardKeyWidth),
                    letter = letter,
                    isUppercase = state.isUppercase,
                    onIntent = onIntent,
                )
            }

            DeleteKey(
                modifier = Modifier.width(metrics.sideKeyWidth),
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun LetterKeyRow(
    letters: String,
    isUppercase: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    KeyboardRow(modifier = modifier) {
        letters.forEach { letter ->
            LetterKey(
                modifier = Modifier.width(metrics.standardKeyWidth),
                letter = letter,
                isUppercase = isUppercase,
                onIntent = onIntent,
            )
        }
    }
}
