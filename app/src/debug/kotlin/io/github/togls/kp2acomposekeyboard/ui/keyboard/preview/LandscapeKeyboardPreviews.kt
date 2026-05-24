package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.togls.kp2acomposekeyboard.domain.keyboard.TextInputMode
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode

@Preview(
    name = "Default - Letters",
    group = "Keyboard / Landscape",
    showBackground = true,
    widthDp = 800,
    heightDp = 240,
)
@Composable
private fun LandscapeDefaultLettersPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = TextInputMode.Letters,
        ),
        settings = previewCompactLightSettings(),
    )
}

@Preview(
    name = "Default - Numbers",
    group = "Keyboard / Landscape",
    showBackground = true,
    widthDp = 800,
    heightDp = 240,
)
@Composable
private fun LandscapeDefaultNumbersPreview() {
    KeyboardPreviewContent(
        state = previewDefaultKeyboardState(
            inputMode = TextInputMode.Numbers,
        ),
        settings = previewCompactLightSettings(),
    )
}

@Preview(
    name = "Entry - Paged",
    group = "Keyboard / Landscape",
    showBackground = true,
    widthDp = 800,
    heightDp = 240,
)
@Composable
private fun LandscapeEntryPagedPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Paged,
        ),
        settings = previewCompactLightSettings(),
    )
}

@Preview(
    name = "Entry - Expanded",
    group = "Keyboard / Landscape",
    showBackground = true,
    widthDp = 800,
    heightDp = 240,
)
@Composable
private fun LandscapeEntryExpandedPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Expanded,
        ),
        settings = previewCompactLightSettings(),
    )
}