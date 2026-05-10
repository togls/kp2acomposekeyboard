package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

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
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun UtilityPanel(
    state: KeyboardUiState,
    dragState: UtilityDragState,
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
            availableKeyboardUtilityItems()
                // Pinned utilities are already reachable from UtilityRow; keeping
                // them in the panel would create two visible copies after drop.
                .filter { item ->
                    shouldShowPanelUtilityItem(
                        itemId = item.id,
                        utilitySlots = state.utilitySlots,
                    )
                }
                .forEach { item ->
                    UtilityItemSlot(
                        itemId = item.id,
                        onIntent = onIntent,
                        dragState = dragState,
                        dragSource = UtilityDragSource.Panel,
                        onDrop = { itemId, source, target ->
                            dispatchUtilityDrop(
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
