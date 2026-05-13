package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.row.ExpandedEntryActionRows
import io.github.togls.kp2acomposekeyboard.ui.keyboard.row.NormalEntryActionRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics
import kotlinx.coroutines.launch

@Composable
fun EntryKeyboardLayout(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalKeyboardLayoutMetrics.current
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val normalScrollState = rememberScrollState()
    val expandedScrollState = rememberScrollState()
    val fixedFieldIds = state.fixedFields.map { field -> field.id }
    val extraFieldIds = state.extraFields.map { field -> field.id }

    LaunchedEffect(
        state.mainLayout,
        state.entryFieldDisplayMode,
        state.hasActiveSession,
        state.currentEntryName,
        fixedFieldIds,
        extraFieldIds,
    ) {
        normalScrollState.scrollTo(0)
        expandedScrollState.scrollTo(0)
    }

    CompositionLocalProvider(
        LocalKeyboardAdaptiveMetrics provides adaptiveMetrics.copy(
            keyHeight = metrics.keyboardRowHeight,
        ),
    ) {
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
            when (state.entryFieldDisplayMode) {
                EntryFieldDisplayMode.Paged -> {
                    NormalEntryContent(
                        state = state,
                        scrollState = normalScrollState,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                }

                EntryFieldDisplayMode.Expanded -> {
                    ExpandedEntryContent(
                        state = state,
                        scrollState = expandedScrollState,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NormalEntryContent(
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

@Composable
private fun ExpandedEntryContent(
    state: KeyboardUiState,
    scrollState: ScrollState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val expandedFields = state.fixedFields + state.extraFields
    var visibleFieldListAreaHeightPx by remember { mutableFloatStateOf(0f) }
    val pageState = EntryFieldPageState(
        currentOffsetPx = scrollState.value.toFloat(),
        maxScrollOffsetPx = scrollState.maxValue.toFloat(),
        visibleFieldListAreaHeightPx = visibleFieldListAreaHeightPx,
        contentHeightPx = scrollState.maxValue.toFloat() + visibleFieldListAreaHeightPx,
    )

    LaunchedEffect(
        scrollState.maxValue,
        visibleFieldListAreaHeightPx,
        expandedFields.map { field -> field.id },
        state.entryFieldDisplayMode,
    ) {
        if (scrollState.value > scrollState.maxValue) {
            scrollState.scrollTo(scrollState.maxValue)
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
                    scrollState.animateScrollTo(pageState.previousTargetPx().toInt())
                }
            },
            onScrollDown = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(pageState.nextTargetPx().toInt())
                }
            },
            onIntent = onIntent,
        )
    }
}
