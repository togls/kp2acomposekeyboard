package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.metrics.LocalKeyboardAdaptiveMetrics

@Composable
internal fun QuickActionBar(
    state: KeyboardUiState,
    dragState: QuickActionDragState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val sideSlotSize = adaptiveMetrics.keyHeight * SideSlotScale
    val centerSlotWidth = adaptiveMetrics.keyHeight * CenterSlotWidthScale
    val showRightSlot = shouldShowRightQuickActionSlot(
        rightItemId = state.quickActionSlots.rightItemId,
        isQuickActionPanelExpanded = state.isQuickActionPanelExpanded,
    )
    var centerContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var rightSlotBounds by remember { mutableStateOf<Rect?>(null) }
    val centerItemBounds = remember { mutableStateMapOf<KeyboardQuickActionId, Rect>() }

    SideEffect {
        // Bounds are refreshed after composition so drop detection uses the
        // latest row layout, including the optional right slot.
        dragState.updateDropTargets(
            centerItemBounds = state.quickActionSlots.centerItemIds.mapNotNull { itemId ->
                centerItemBounds[itemId]
            },
            centerContainerBounds = centerContainerBounds,
            rightSlotBounds = rightSlotBounds.takeIf { showRightSlot },
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickActionIconButton(
            modifier = Modifier
                .width(sideSlotSize)
                .height(sideSlotSize),
            iconRes = if (state.isQuickActionPanelExpanded) {
                R.drawable.ic_close_24
            } else {
                R.drawable.ic_apps_24
            },
            contentDescription = stringResource(
                if (state.isQuickActionPanelExpanded) {
                    R.string.cd_key_close_quick_action_panel
                } else {
                    R.string.cd_key_toggle_quick_action_panel
                },
            ),
            onClick = { onIntent(KeyboardIntent.ToggleQuickActionPanel) },
        )

        if (state.hasActiveSession && !state.isQuickActionPanelExpanded) {
            val entryName = state.currentEntryName
                ?.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.entry_name_unnamed)

            ExistingEntryHint(
                entryName = entryName,
                onIntent = onIntent,
                modifier = Modifier
                    .weight(1f)
                    .height(adaptiveMetrics.keyHeight),
            )
        } else {
            QuickActionCenterSlots(
                state = state,
                dragState = dragState,
                onIntent = onIntent,
                onItemBoundsChanged = { itemId, bounds ->
                    centerItemBounds[itemId] = bounds
                },
                itemWidth = centerSlotWidth,
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        centerContainerBounds = coordinates.boundsInRoot()
                    },
            )
        }

        if (showRightSlot) {
            QuickActionSlot(
                itemId = state.quickActionSlots.rightItemId,
                onIntent = onIntent,
                emptySlot = true,
                dragState = dragState,
                dragSource = QuickActionDragSource.Pinned,
                onDrop = { itemId, source, target ->
                    dispatchQuickActionDrop(
                        itemId = itemId,
                        source = source,
                        target = target,
                        onIntent = onIntent,
                    )
                },
                modifier = Modifier
                    .width(sideSlotSize)
                    .height(sideSlotSize)
                    .onGloballyPositioned { coordinates ->
                        rightSlotBounds = coordinates.boundsInRoot()
                    },
            )
        }
    }
}

@Composable
private fun QuickActionCenterSlots(
    state: KeyboardUiState,
    dragState: QuickActionDragState,
    onIntent: (KeyboardIntent) -> Unit,
    onItemBoundsChanged: (KeyboardQuickActionId, Rect) -> Unit,
    itemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val centerItems = state.quickActionSlots.centerItemIds

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (centerItems.isEmpty()) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(adaptiveMetrics.keyHeight),
            )
        } else {
            centerItems.forEach { itemId ->
                QuickActionSlot(
                    itemId = itemId,
                    onIntent = onIntent,
                    dragState = dragState,
                    dragSource = QuickActionDragSource.Pinned,
                    onBoundsChanged = onItemBoundsChanged,
                    onDrop = { droppedItemId, source, target ->
                        dispatchQuickActionDrop(
                            itemId = droppedItemId,
                            source = source,
                            target = target,
                            onIntent = onIntent,
                        )
                    },
                    modifier = Modifier
                        .width(itemWidth)
                        .height(adaptiveMetrics.keyHeight),
                )
            }
        }
    }
}

private const val SideSlotScale = 0.82f
private const val CenterSlotWidthScale = 1.45f
