package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags

@Composable
internal fun NormalEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(KeyboardTestTags.EntryNormalContent),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        EntryFieldGrid(
            fields = state.fixedFields,
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.keyboardRowHeight)
                .testTag(KeyboardTestTags.EntryFixedFields),
        )

        EntryFieldGrid(
            fields = state.extraFields,
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.remainingFieldsAreaHeight)
                .verticalScroll(scrollState)
                .testTag(KeyboardTestTags.EntryRemainingFields),
        )

        NormalEntryActionRow(
            onIntent = onIntent,
            modifier = Modifier
                .height(metrics.keyboardRowHeight)
                .testTag(KeyboardTestTags.EntryActions),
        )
    }
}
