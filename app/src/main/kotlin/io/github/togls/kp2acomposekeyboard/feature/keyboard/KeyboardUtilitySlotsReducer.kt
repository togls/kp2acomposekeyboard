package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots

class KeyboardUtilitySlotsReducer(
    private val allowedItemIds: List<KeyboardUtilityItemId> = KeyboardUtilityItemId.productionItems,
) {

    fun sanitize(slots: KeyboardUtilitySlots): KeyboardUtilitySlots {
        val allowedCenterItems = slots.centerItemIds
            .filter { itemId -> itemId in allowedItemIds }
            .distinct()

        val allowedRightItem = slots.rightItemId
            ?.takeIf { itemId -> itemId in allowedItemIds }
            ?.takeUnless { itemId -> itemId in allowedCenterItems }

        val maxCenterSize = if (allowedRightItem == null) {
            KeyboardUtilitySlots.MAX_PINNED_ITEMS
        } else {
            KeyboardUtilitySlots.MAX_PINNED_ITEMS - 1
        }

        return KeyboardUtilitySlots(
            centerItemIds = allowedCenterItems.take(maxCenterSize),
            rightItemId = allowedRightItem,
        )
    }

    fun moveToCenter(
        slots: KeyboardUtilitySlots,
        itemId: KeyboardUtilityItemId,
        targetIndex: Int,
    ): KeyboardUtilitySlots {
        val sanitizedSlots = sanitize(slots)
        if (!canInsertToCenter(sanitizedSlots, itemId)) {
            return sanitizedSlots
        }

        val withoutItem = remove(sanitizedSlots, itemId)
        val insertIndex = targetIndex.coerceIn(
            minimumValue = 0,
            maximumValue = withoutItem.centerItemIds.size,
        )
        val centerItems = withoutItem.centerItemIds.toMutableList().apply {
            add(insertIndex, itemId)
        }

        return sanitize(withoutItem.copy(centerItemIds = centerItems))
    }

    fun moveToRight(
        slots: KeyboardUtilitySlots,
        itemId: KeyboardUtilityItemId,
    ): KeyboardUtilitySlots {
        val sanitizedSlots = sanitize(slots)
        if (!canMoveToRight(itemId)) {
            return sanitizedSlots
        }

        // Moving to right is a replacement operation: an existing right item is
        // removed before assigning the new one, so max pinned count is unchanged.
        return sanitize(
            remove(sanitizedSlots, itemId).copy(rightItemId = itemId),
        )
    }

    fun remove(
        slots: KeyboardUtilitySlots,
        itemId: KeyboardUtilityItemId,
    ): KeyboardUtilitySlots {
        return sanitize(
            slots.copy(
                centerItemIds = slots.centerItemIds.filterNot { existingItemId ->
                    existingItemId == itemId
                },
                rightItemId = slots.rightItemId?.takeUnless { existingItemId ->
                    existingItemId == itemId
                },
            ),
        )
    }

    private fun canInsertToCenter(
        slots: KeyboardUtilitySlots,
        itemId: KeyboardUtilityItemId,
    ): Boolean {
        return itemId in allowedItemIds &&
                (itemId in slots.centerItemIds ||
                        itemId == slots.rightItemId ||
                        slots.pinnedCount < KeyboardUtilitySlots.MAX_PINNED_ITEMS)
    }

    private fun canMoveToRight(itemId: KeyboardUtilityItemId): Boolean {
        // Right-slot replacement is allowed even when pinned count is already
        // full, as long as the item is a supported production utility.
        return itemId in allowedItemIds
    }
}
