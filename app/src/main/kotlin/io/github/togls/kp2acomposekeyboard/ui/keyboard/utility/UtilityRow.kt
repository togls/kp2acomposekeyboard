package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

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
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.key.ExistingEntryHint
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun UtilityRow(
    state: KeyboardUiState,
    dragState: UtilityDragState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val sideSlotSize = adaptiveMetrics.keyHeight * SideSlotScale
    var centerContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var rightSlotBounds by remember { mutableStateOf<Rect?>(null) }
    val centerItemBounds = remember { mutableStateMapOf<KeyboardUtilityItemId, Rect>() }

    SideEffect {
        dragState.updateDropTargets(
            centerItemBounds = state.utilitySlots.centerItemIds.mapNotNull { itemId ->
                centerItemBounds[itemId]
            },
            centerContainerBounds = centerContainerBounds,
            rightSlotBounds = rightSlotBounds,
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardMetrics.KeySpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UtilityIconButton(
            modifier = Modifier
                .width(sideSlotSize)
                .height(sideSlotSize),
            iconRes = if (state.isUtilityPanelExpanded) {
                R.drawable.ic_close_24
            } else {
                R.drawable.ic_apps_24
            },
            contentDescription = stringResource(
                if (state.isUtilityPanelExpanded) {
                    R.string.cd_key_close_utility_panel
                } else {
                    R.string.cd_key_toggle_utility_panel
                },
            ),
            onClick = { onIntent(KeyboardIntent.ToggleUtilityPanel) },
        )

        if (state.hasActiveSession && !state.isUtilityPanelExpanded) {
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
            UtilityCenterSlots(
                state = state,
                dragState = dragState,
                onIntent = onIntent,
                onItemBoundsChanged = { itemId, bounds ->
                    centerItemBounds[itemId] = bounds
                },
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        centerContainerBounds = coordinates.boundsInRoot()
                    },
            )
        }

        UtilityItemSlot(
            itemId = state.utilitySlots.rightItemId,
            onIntent = onIntent,
            emptySlot = true,
            dragState = dragState,
            dragSource = UtilityDragSource.Pinned,
            onDrop = { itemId, source, target ->
                dispatchUtilityDrop(
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

@Composable
private fun UtilityCenterSlots(
    state: KeyboardUiState,
    dragState: UtilityDragState,
    onIntent: (KeyboardIntent) -> Unit,
    onItemBoundsChanged: (KeyboardUtilityItemId, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val centerItems = state.utilitySlots.centerItemIds

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
                UtilityItemSlot(
                    itemId = itemId,
                    onIntent = onIntent,
                    dragState = dragState,
                    dragSource = UtilityDragSource.Pinned,
                    onBoundsChanged = onItemBoundsChanged,
                    onDrop = { droppedItemId, source, target ->
                        dispatchUtilityDrop(
                            itemId = droppedItemId,
                            source = source,
                            target = target,
                            onIntent = onIntent,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(adaptiveMetrics.keyHeight),
                )
            }
        }
    }
}

private const val SideSlotScale = 0.82f
