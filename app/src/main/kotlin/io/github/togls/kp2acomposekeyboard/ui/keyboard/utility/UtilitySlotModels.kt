package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId

internal sealed interface UtilityDragSource {
    data object Panel : UtilityDragSource
    data object Pinned : UtilityDragSource
}

internal sealed interface UtilityDropTarget {
    data class Center(
        val targetIndex: Int,
    ) : UtilityDropTarget

    data object Right : UtilityDropTarget
    data object Outside : UtilityDropTarget
}

internal fun resolveUtilityDropTarget(
    positionInRoot: Offset,
    centerItemBounds: List<Rect>,
    centerContainerBounds: Rect?,
    rightSlotBounds: Rect?,
): UtilityDropTarget {
    if (rightSlotBounds?.contains(positionInRoot) == true) {
        return UtilityDropTarget.Right
    }

    if (centerContainerBounds?.contains(positionInRoot) != true) {
        return UtilityDropTarget.Outside
    }

    if (centerItemBounds.isEmpty()) {
        return UtilityDropTarget.Center(targetIndex = 0)
    }

    centerItemBounds.forEachIndexed { index, bounds ->
        if (positionInRoot.x < bounds.center.x) {
            return UtilityDropTarget.Center(targetIndex = index)
        }
    }

    return UtilityDropTarget.Center(targetIndex = centerItemBounds.size)
}

internal fun dispatchUtilityDrop(
    itemId: KeyboardUtilityItemId,
    source: UtilityDragSource,
    target: UtilityDropTarget?,
    onIntent: (KeyboardIntent) -> Unit,
) {
    when (target) {
        is UtilityDropTarget.Center -> {
            onIntent(
                KeyboardIntent.MoveUtilityItemToCenter(
                    itemId = itemId,
                    targetIndex = target.targetIndex,
                ),
            )
        }

        UtilityDropTarget.Right -> {
            onIntent(KeyboardIntent.MoveUtilityItemToRight(itemId))
        }

        UtilityDropTarget.Outside -> {
            if (source == UtilityDragSource.Pinned) {
                onIntent(KeyboardIntent.RemoveUtilityItem(itemId))
            }
        }

        null -> Unit
    }
}
