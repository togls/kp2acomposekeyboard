package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId
import kotlin.math.roundToInt

internal fun shouldShowRightUtilitySlot(
    rightItemId: KeyboardUtilityItemId?,
    isUtilityPanelExpanded: Boolean,
): Boolean {
    return rightItemId != null || isUtilityPanelExpanded
}

internal fun resolveUtilityDragPreviewOffset(
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

internal fun shouldShowPanelUtilityItem(
    itemId: KeyboardUtilityItemId,
    utilitySlots: KeyboardUtilitySlots,
): Boolean {
    // Panel items represent utilities available to pin; already pinned items
    // stay visible through their slot instead of being duplicated here.
    return itemId !in utilitySlots.centerItemIds &&
            itemId != utilitySlots.rightItemId
}

internal fun shouldShowDraggedSourceItem(
    itemId: KeyboardUtilityItemId,
    source: UtilityDragSource,
    draggedItemId: KeyboardUtilityItemId?,
    dragSource: UtilityDragSource?,
): Boolean {
    // Match both item and source so dragging from panel does not hide the same
    // utility that is already pinned in the row during reorder/replace flows.
    return itemId != draggedItemId || source != dragSource
}
