package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import kotlinx.coroutines.launch

@Composable
fun EntryKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.entryFieldDisplayMode, state.currentEntryName) {
        if (state.entryFieldDisplayMode == EntryFieldDisplayMode.Expanded) {
            expandedScrollState.scrollTo(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                horizontal = KeyboardMetrics.OuterPaddingHorizontal,
                vertical = KeyboardMetrics.OuterPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        EntryHeader(entryName = state.currentEntryName)

        when (state.entryFieldDisplayMode) {
            EntryFieldDisplayMode.Paged -> {
                PagedEntryContent(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )
            }

            EntryFieldDisplayMode.Expanded -> {
                ExpandedEntryContent(
                    state = state,
                    scrollState = expandedScrollState,
                    onScrollUp = {
                        coroutineScope.launch {
                            expandedScrollState.animateScrollTo(
                                (expandedScrollState.value - EXPANDED_SCROLL_PAGE_SIZE_PX)
                                    .coerceAtLeast(0),
                            )
                        }
                    },
                    onScrollDown = {
                        coroutineScope.launch {
                            expandedScrollState.animateScrollTo(
                                (expandedScrollState.value + EXPANDED_SCROLL_PAGE_SIZE_PX)
                                    .coerceAtMost(expandedScrollState.maxValue),
                            )
                        }
                    },
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PagedEntryContent(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
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
            modifier = Modifier.weight(1f),
        )

        PagedEntryActionRow(
            canGoPrevious = state.extraFieldPageIndex > 0,
            canGoNext = hasNextPage(state),
            onIntent = onIntent,
        )

        KeyboardBottomSafeSpacer()
    }
}

@Composable
private fun ExpandedEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        AllFieldsExpandedPanel(
            fields = state.allFields,
            scrollState = scrollState,
            onIntent = onIntent,
            modifier = Modifier.weight(1f),
        )

        ExpandedEntryActionRows(
            canScrollUp = scrollState.value > 0,
            canScrollDown = scrollState.value < scrollState.maxValue,
            onScrollUp = onScrollUp,
            onScrollDown = onScrollDown,
            onIntent = onIntent,
        )

        KeyboardBottomSafeSpacer()
    }
}

private fun hasNextPage(state: KeyboardUiState): Boolean {
    val pageSize = state.extraFieldPageSize.coerceAtLeast(1)
    val nextPageStart = (state.extraFieldPageIndex + 1) * pageSize
    return nextPageStart < state.extraFields.size
}

// 固定滚动步长用于 P0，避免为了 prev/next 引入复杂布局测量。
private const val EXPANDED_SCROLL_PAGE_SIZE_PX = 220