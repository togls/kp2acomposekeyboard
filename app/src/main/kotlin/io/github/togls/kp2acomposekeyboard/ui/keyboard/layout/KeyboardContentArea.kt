package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.domain.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityDragPreview
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityPanel
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.rememberUtilityDragState

/**
 * Hosts the main keyboard content. Drag-preview bounds are measured here, but
 * layout metrics are provided by KeyboardFrame to avoid measurement-state loops.
 */
@Composable
internal fun KeyboardContentArea(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var contentBoundsInRoot by remember { mutableStateOf<Rect?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                contentBoundsInRoot = coordinates.boundsInRoot()
            },
    ) {
        val utilityDragState = rememberUtilityDragState()

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            UtilityRow(
                state = state,
                dragState = utilityDragState,
                onIntent = onIntent,
            )

            if (state.isUtilityPanelExpanded) {
                UtilityPanel(
                    state = state,
                    dragState = utilityDragState,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )
            } else {
                when (state.mainLayout) {
                    MainKeyboardLayout.TextInput -> {
                        DefaultKeyboardLayout(
                            state = state,
                            onIntent = onIntent,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    MainKeyboardLayout.Entry -> {
                        EntryKeyboardLayout(
                            state = state,
                            onIntent = onIntent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        UtilityDragPreview(
            dragState = utilityDragState,
            containerBoundsInRoot = contentBoundsInRoot,
        )
    }
}
