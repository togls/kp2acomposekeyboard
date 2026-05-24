package io.github.togls.kp2acomposekeyboard.ui.keyboard.row

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ClearEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.DeleteKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ExpandFieldsKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.LanguageSwitchKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SelectEntryKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.SpaceKey
import io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.KeyboardRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.layout.LocalKeyboardLayoutMetrics

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

        ClearEntryKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.standardKeyWidth),
        )

        DeleteKey(
            onIntent = onIntent,
            modifier = Modifier.width(metrics.sideKeyWidth),
        )
    }
}
