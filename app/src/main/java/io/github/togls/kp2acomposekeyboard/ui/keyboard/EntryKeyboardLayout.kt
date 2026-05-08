package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState

@Composable
fun EntryKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dpCompat)
            .padding(horizontal = 8.dpCompat, vertical = 8.dpCompat),
        verticalArrangement = Arrangement.spacedBy(8.dpCompat),
    ) {
        EntryHeader(entryName = state.currentEntryName)

        when (state.entryFieldDisplayMode) {
            EntryFieldDisplayMode.Paged -> {
                PagedEntryContent(
                    state = state,
                    onIntent = onIntent,
                )
            }

            EntryFieldDisplayMode.Expanded -> {
                ExpandedEntryContent(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun PagedEntryContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    FixedFieldRow(
        fields = state.fixedFields,
        onIntent = onIntent,
    )

    ExtraFieldPagedPanel(
        fields = state.extraFields,
        pageIndex = state.extraFieldPageIndex,
        pageSize = state.extraFieldPageSize,
        onIntent = onIntent,
    )

    PagedEntryActionRow(
        canGoPrevious = state.extraFieldPageIndex > 0,
        canGoNext = hasNextPage(state),
        onIntent = onIntent,
    )
}

@Composable
private fun ExpandedEntryContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
) {
    AllFieldsExpandedPanel(
        fields = state.allFields,
        onIntent = onIntent,
    )

    ExpandedEntryActionRows(
        canScrollUp = false,
        canScrollDown = state.allFields.size > EXPANDED_SCROLL_THRESHOLD,
        onIntent = onIntent,
    )
}

private fun hasNextPage(state: KeyboardUiState): Boolean {
    val pageSize = state.extraFieldPageSize.coerceAtLeast(1)
    val nextPageStart = (state.extraFieldPageIndex + 1) * pageSize
    return nextPageStart < state.extraFields.size
}

private const val EXPANDED_SCROLL_THRESHOLD = 6