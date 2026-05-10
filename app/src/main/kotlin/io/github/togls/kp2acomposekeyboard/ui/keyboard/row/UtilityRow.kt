package io.github.togls.kp2acomposekeyboard.ui.keyboard.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ExistingEntryHint
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SettingsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun UtilityRow(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val actionKeySize = adaptiveMetrics.keyHeight

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsKey(
            onIntent = onIntent,
            modifier = Modifier
                .width(actionKeySize)
                .height(actionKeySize),
        )

        if (state.hasActiveSession) {
            val entryName = state.currentEntryName
                ?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.entry_name_unnamed)

            ExistingEntryHint(
                entryName = entryName,
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1f)
                    .height(actionKeySize),
            )
        } else {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(actionKeySize),
            )
        }

        Spacer(
            modifier = Modifier
                .width(actionKeySize)
                .height(actionKeySize),
        )
    }
}