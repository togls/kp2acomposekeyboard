package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.ClearEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.ExpandFieldsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.LanguageSwitchKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.SpaceKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics

@Composable
internal fun NormalEntryActionRow(
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    KeyboardRow(modifier = modifier) {
        SelectEntryKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.sideKeyWidth),
        )

        LanguageSwitchKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.standardKeyWidth),
        )

        SpaceKey(
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        ExpandFieldsKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.standardKeyWidth),
        )

        DeleteKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.sideKeyWidth),
        )
    }
}
