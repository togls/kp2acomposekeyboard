package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.togls.kp2acomposekeyboard.domain.keyboard.TextInputMode

@Preview(
    name = "Letters",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun TextInputKeyboardLettersPreview() {
    KeyboardPreviewContent(
        state = previewTextInputKeyboardState(
            inputMode = TextInputMode.Letters,
        ),
    )
}

@Preview(
    name = "Numbers",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun TextInputKeyboardNumbersPreview() {
    KeyboardPreviewContent(
        state = previewTextInputKeyboardState(
            inputMode = TextInputMode.Numbers,
        ),
    )
}

@Preview(
    name = "Symbols",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun TextInputKeyboardSymbolsPreview() {
    KeyboardPreviewContent(
        state = previewTextInputKeyboardState(
            inputMode = TextInputMode.Symbols,
        ),
    )
}

@Preview(
    name = "Letters - Dark",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TextInputKeyboardLettersDarkPreview() {
    KeyboardPreviewContent(
        state = previewTextInputKeyboardState(
            inputMode = TextInputMode.Letters,
        ),
        settings = previewDarkSettings(),
    )
}

@Preview(
    name = "Quick Action Panel",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun TextInputKeyboardQuickActionPanelPreview() {
    KeyboardPreviewContent(
        state = previewQuickActionPanelState(),
    )
}

@Preview(
    name = "Right Quick Action Slot",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun TextInputKeyboardRightQuickActionSlotPreview() {
    KeyboardPreviewContent(
        state = previewRightQuickActionSlotState(),
    )
}
