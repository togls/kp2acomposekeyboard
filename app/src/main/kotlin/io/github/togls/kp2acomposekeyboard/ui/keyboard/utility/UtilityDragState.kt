package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId

@Stable
internal class UtilityDragState {
    var draggedItemId by mutableStateOf<KeyboardUtilityItemId?>(null)
        private set
    var dragSource by mutableStateOf<UtilityDragSource?>(null)
        private set
    var positionInRoot by mutableStateOf(Offset.Zero)
        private set
    var hoveredDropTarget by mutableStateOf<UtilityDropTarget?>(null)
        private set
    var sourceBoundsInRoot by mutableStateOf<Rect?>(null)
        private set

    private var centerItemBounds by mutableStateOf(emptyList<Rect>())
    private var centerContainerBounds by mutableStateOf<Rect?>(null)
    private var rightSlotBounds by mutableStateOf<Rect?>(null)

    fun updateDropTargets(
        centerItemBounds: List<Rect>,
        centerContainerBounds: Rect?,
        rightSlotBounds: Rect?,
    ) {
        this.centerItemBounds = centerItemBounds
        this.centerContainerBounds = centerContainerBounds
        this.rightSlotBounds = rightSlotBounds
    }

    fun startDrag(
        itemId: KeyboardUtilityItemId,
        source: UtilityDragSource,
        sourceBounds: Rect,
        localPointerPosition: Offset,
    ) {
        draggedItemId = itemId
        dragSource = source
        sourceBoundsInRoot = sourceBounds
        positionInRoot = sourceBounds.topLeft + localPointerPosition
        hoveredDropTarget = resolveDropTarget(positionInRoot)
    }

    fun updateDrag(localPointerPosition: Offset) {
        val sourceBounds = sourceBoundsInRoot ?: return
        positionInRoot = sourceBounds.topLeft + localPointerPosition
        hoveredDropTarget = resolveDropTarget(positionInRoot)
    }

    fun endDrag() {
        draggedItemId = null
        dragSource = null
        positionInRoot = Offset.Zero
        hoveredDropTarget = null
        sourceBoundsInRoot = null
    }

    fun resolveDropTarget(positionInRoot: Offset): UtilityDropTarget {
        return resolveUtilityDropTarget(
            positionInRoot = positionInRoot,
            centerItemBounds = centerItemBounds,
            centerContainerBounds = centerContainerBounds,
            rightSlotBounds = rightSlotBounds,
        )
    }
}

@Composable
internal fun rememberUtilityDragState(): UtilityDragState {
    return remember { UtilityDragState() }
}
