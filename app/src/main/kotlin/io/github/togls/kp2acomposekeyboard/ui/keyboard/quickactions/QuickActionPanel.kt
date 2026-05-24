package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardAdaptiveMetrics

@Composable
internal fun QuickActionPanel(
    state: KeyboardUiState,
    dragState: QuickActionDragState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val panelItemWidth = adaptiveMetrics.keyHeight * PanelItemWidthScale

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = KeyboardMetrics.OuterPaddingHorizontal,
                vertical = KeyboardMetrics.OuterPaddingVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        ) {
            availableKeyboardQuickActions()
                // Pinned quick actions are already reachable from QuickActionBar; keeping
                // them in the panel would create two visible copies after drop.
                .filter { item ->
                    shouldShowPanelQuickAction(
                        itemId = item.id,
                        quickActionSlots = state.quickActionSlots,
                    )
                }
                .forEach { item ->
                    QuickActionSlot(
                        itemId = item.id,
                        onIntent = onIntent,
                        dragState = dragState,
                        dragSource = QuickActionDragSource.Panel,
                        onDrop = { itemId, source, target ->
                            dispatchQuickActionDrop(
                                itemId = itemId,
                                source = source,
                                target = target,
                                onIntent = onIntent,
                            )
                        },
                        modifier = Modifier
                            .width(panelItemWidth)
                            .height(adaptiveMetrics.keyHeight),
                    )
                }
        }
    }
}

private const val PanelItemWidthScale = 1.45f
