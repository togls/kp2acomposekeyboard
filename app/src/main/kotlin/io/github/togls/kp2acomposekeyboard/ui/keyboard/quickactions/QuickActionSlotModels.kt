package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId

internal sealed interface QuickActionDragSource {
    data object Panel : QuickActionDragSource
    data object Pinned : QuickActionDragSource
}

internal sealed interface QuickActionDropTarget {
    data class Center(
        val targetIndex: Int,
    ) : QuickActionDropTarget

    data object Right : QuickActionDropTarget
    data object Outside : QuickActionDropTarget
}

internal fun resolveQuickActionDropTarget(
    positionInRoot: Offset,
    centerItemBounds: List<Rect>,
    centerContainerBounds: Rect?,
    rightSlotBounds: Rect?,
): QuickActionDropTarget {
    if (rightSlotBounds?.contains(positionInRoot) == true) {
        return QuickActionDropTarget.Right
    }

    if (centerContainerBounds?.contains(positionInRoot) != true) {
        return QuickActionDropTarget.Outside
    }

    if (centerItemBounds.isEmpty()) {
        return QuickActionDropTarget.Center(targetIndex = 0)
    }

    // Use each item's midpoint instead of container width ratios so insertion
    // stays accurate when slots have gaps or different widths.
    centerItemBounds.forEachIndexed { index, bounds ->
        if (positionInRoot.x < bounds.center.x) {
            return QuickActionDropTarget.Center(targetIndex = index)
        }
    }

    return QuickActionDropTarget.Center(targetIndex = centerItemBounds.size)
}

internal fun dispatchQuickActionDrop(
    itemId: KeyboardQuickActionId,
    source: QuickActionDragSource,
    target: QuickActionDropTarget?,
    onIntent: (KeyboardIntent) -> Unit,
) {
    when (target) {
        is QuickActionDropTarget.Center -> {
            onIntent(
                KeyboardIntent.MoveQuickActionToCenter(
                    itemId = itemId,
                    targetIndex = target.targetIndex,
                ),
            )
        }

        QuickActionDropTarget.Right -> {
            onIntent(KeyboardIntent.MoveQuickActionToRight(itemId))
        }

        QuickActionDropTarget.Outside -> {
            // Only pinned quick actions can be removed by dropping outside; panel
            // items are not persisted until the final drop target is valid.
            if (source == QuickActionDragSource.Pinned) {
                onIntent(KeyboardIntent.RemoveQuickAction(itemId))
            }
        }

        null -> Unit
    }
}
