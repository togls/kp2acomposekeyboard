package io.github.togls.kp2acomposekeyboard.ui.keyboard.frame

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
import io.github.togls.kp2acomposekeyboard.ui.keyboard.entry.EntryKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.QuickActionDragPreview
import io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.QuickActionPanel
import io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.QuickActionBar
import io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions.rememberQuickActionDragState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.textinput.TextInputKeyboardLayout

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
        val quickActionDragState = rememberQuickActionDragState()

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            QuickActionBar(
                state = state,
                dragState = quickActionDragState,
                onIntent = onIntent,
            )

            if (state.isQuickActionPanelExpanded) {
                QuickActionPanel(
                    state = state,
                    dragState = quickActionDragState,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )
            } else {
                when (state.mainLayout) {
                    MainKeyboardLayout.TextInput -> {
                        TextInputKeyboardLayout(
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

        QuickActionDragPreview(
            dragState = quickActionDragState,
            containerBoundsInRoot = contentBoundsInRoot,
        )
    }
}
