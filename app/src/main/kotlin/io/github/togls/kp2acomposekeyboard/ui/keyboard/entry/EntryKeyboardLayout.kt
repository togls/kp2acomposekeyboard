package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardLayoutMetrics

@Composable
internal fun EntryKeyboardLayout(
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
