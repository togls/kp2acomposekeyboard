package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags
import kotlinx.coroutines.launch

@Composable
internal fun ExpandedEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val expandedFields = state.fixedFields + state.extraFields
    val expandedFieldIds = expandedFields.map { field -> field.id }
    var visibleFieldListAreaHeightPx by remember { mutableFloatStateOf(0f) }
    var isResettingScroll by remember { mutableStateOf(false) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    val pageState = EntryFieldPageState(
        currentOffsetPx = scrollState.value.toFloat(),
        maxScrollOffsetPx = scrollState.maxValue.toFloat(),
        visibleFieldListAreaHeightPx = visibleFieldListAreaHeightPx,
        contentHeightPx = scrollState.maxValue.toFloat() + visibleFieldListAreaHeightPx,
    )
    val latestPageState by rememberUpdatedState(pageState)

    LaunchedEffect(
        state.mainLayout,
        state.entryFieldDisplayMode,
        state.hasActiveSession,
        state.currentEntryName,
        expandedFieldIds,
    ) {
        isResettingScroll = true
        try {
            scrollState.scrollTo(0)
        } finally {
            isResettingScroll = false
        }
    }

    LaunchedEffect(
        scrollState.maxValue,
        visibleFieldListAreaHeightPx,
        expandedFieldIds,
        state.entryFieldDisplayMode,
    ) {
        if (scrollState.value > scrollState.maxValue) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(scrollState) {
        var wasScrolling = scrollState.isScrollInProgress
        snapshotFlow { scrollState.isScrollInProgress }
            .collect { isScrolling ->
                val endedUserScroll = wasScrolling &&
                        !isScrolling &&
                        !isResettingScroll &&
                        !isProgrammaticScroll
                wasScrolling = isScrolling

                if (endedUserScroll) {
                    val target = latestPageState.snapTargetPx().toInt()
                    if (target != scrollState.value) {
                        isProgrammaticScroll = true
                        try {
                            scrollState.animateScrollTo(target)
                        } finally {
                            isProgrammaticScroll = false
                        }
                    }
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(KeyboardTestTags.EntryExpandedContent),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            EntryFieldGrid(
                fields = expandedFields,
                onIntent = onIntent,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        visibleFieldListAreaHeightPx = size.height.toFloat()
                    }
                    .verticalScroll(scrollState)
                    .testTag(KeyboardTestTags.EntryExpandedFields),
            )
        }

        ExpandedEntryActionRows(
            canScrollUp = pageState.previousEnabled,
            canScrollDown = pageState.nextEnabled,
            onScrollUp = {
                coroutineScope.launch {
                    isProgrammaticScroll = true
                    try {
                        scrollState.animateScrollTo(pageState.previousTargetPx().toInt())
                    } finally {
                        isProgrammaticScroll = false
                    }
                }
            },
            onScrollDown = {
                coroutineScope.launch {
                    isProgrammaticScroll = true
                    try {
                        scrollState.animateScrollTo(pageState.nextTargetPx().toInt())
                    } finally {
                        isProgrammaticScroll = false
                    }
                }
            },
            onIntent = onIntent,
        )
    }
}
