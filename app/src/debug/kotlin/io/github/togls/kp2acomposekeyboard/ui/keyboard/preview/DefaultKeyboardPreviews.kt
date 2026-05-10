package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode

@Preview(
    name = "Letters",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun DefaultKeyboardLettersPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = DefaultInputMode.Letters,
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
private fun DefaultKeyboardNumbersPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = DefaultInputMode.Numbers,
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
private fun DefaultKeyboardSymbolsPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = DefaultInputMode.Symbols,
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
private fun DefaultKeyboardLettersDarkPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = DefaultInputMode.Letters,
        ),
        settings = previewDarkSettings(),
    )
}

@Preview(
    name = "Utility Panel",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun DefaultKeyboardUtilityPanelPreview() {
    KeyboardPreviewContent(
        state = previewUtilityPanelState(),
    )
}

@Preview(
    name = "Right Utility Slot",
    group = "Keyboard / Default",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun DefaultKeyboardRightUtilitySlotPreview() {
    KeyboardPreviewContent(
        state = previewRightUtilitySlotState(),
    )
}
