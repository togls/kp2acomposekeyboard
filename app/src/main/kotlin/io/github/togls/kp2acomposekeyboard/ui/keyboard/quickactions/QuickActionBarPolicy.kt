package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import kotlin.math.roundToInt

internal fun shouldShowRightQuickActionSlot(
    rightItemId: KeyboardQuickActionId?,
    isQuickActionPanelExpanded: Boolean,
): Boolean {
    return rightItemId != null || isQuickActionPanelExpanded
}

internal fun resolveQuickActionDragPreviewOffset(
    positionInRoot: Offset,
    containerBoundsInRoot: Rect,
    previewSizePx: Float,
): IntOffset {
    // Convert root-space pointer coordinates into the overlay container's
    // local space, then center the floating preview under the pointer.
    val halfPreviewSizePx = previewSizePx / 2f
    return IntOffset(
        x = (positionInRoot.x - containerBoundsInRoot.left - halfPreviewSizePx).roundToInt(),
        y = (positionInRoot.y - containerBoundsInRoot.top - halfPreviewSizePx).roundToInt(),
    )
}

internal fun shouldShowPanelQuickAction(
    itemId: KeyboardQuickActionId,
    quickActionSlots: KeyboardQuickActionSlots,
): Boolean {
    // Panel items represent quick actions available to pin; already pinned items
    // stay visible through their slot instead of being duplicated here.
    return itemId !in quickActionSlots.centerItemIds &&
            itemId != quickActionSlots.rightItemId
}

internal fun shouldShowDraggedSourceAction(
    itemId: KeyboardQuickActionId,
    source: QuickActionDragSource,
    draggedItemId: KeyboardQuickActionId?,
    dragSource: QuickActionDragSource?,
): Boolean {
    // Match both item and source so dragging from panel does not hide the same
    // quick action that is already pinned in the row during reorder/replace flows.
    return itemId != draggedItemId || source != dragSource
}
