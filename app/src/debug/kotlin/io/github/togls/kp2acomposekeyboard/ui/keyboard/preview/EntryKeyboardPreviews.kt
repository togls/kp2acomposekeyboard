package io.github.togls.kp2acomposekeyboard.ui.keyboard.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode

@Preview(
    name = "Paged",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun EntryKeyboardPagedPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Paged,
        ),
    )
}

@Preview(
    name = "Expanded",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 360,
)
@Composable
private fun EntryKeyboardExpandedPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Expanded,
        ),
        settings = previewTallLightSettings(),
    )
}

@Preview(
    name = "Paged - Long Labels",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun EntryKeyboardLongLabelsPreview() {
    KeyboardPreviewContent(
        state = previewLongLabelEntryKeyboardState(),
    )
}

@Preview(
    name = "Paged - Empty Fields",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
)
@Composable
private fun EntryKeyboardEmptyFieldsPreview() {
    KeyboardPreviewContent(
        state = previewEmptyEntryKeyboardState(),
    )
}

@Preview(
    name = "Expanded - Empty Fields",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 360,
)
@Composable
private fun EntryKeyboardExpandedEmptyFieldsPreview() {
    KeyboardPreviewContent(
        state = previewEmptyEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Expanded,
        ),
        settings = previewTallLightSettings(),
    )
}

@Preview(
    name = "Paged - Dark",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 320,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EntryKeyboardPagedDarkPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Paged,
        ),
        settings = previewDarkSettings(),
    )
}

@Preview(
    name = "Expanded - Dark",
    group = "Keyboard / Entry",
    showBackground = true,
    widthDp = 411,
    heightDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EntryKeyboardExpandedDarkPreview() {
    KeyboardPreviewContent(
        state = previewEntryKeyboardState(
            displayMode = EntryFieldDisplayMode.Expanded,
        ),
        settings = previewDarkSettings(),
    )
}
