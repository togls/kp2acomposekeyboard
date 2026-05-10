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
    return itemId !in utilitySlots.centerItemIds &&
            itemId != utilitySlots.rightItemId
}

internal fun shouldShowDraggedSourceItem(
    itemId: KeyboardUtilityItemId,
    source: UtilityDragSource,
    draggedItemId: KeyboardUtilityItemId?,
    dragSource: UtilityDragSource?,
): Boolean {
    return itemId != draggedItemId || source != dragSource
}
