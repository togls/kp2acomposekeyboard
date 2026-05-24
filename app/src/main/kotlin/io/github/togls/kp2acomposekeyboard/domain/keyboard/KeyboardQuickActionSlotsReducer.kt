package io.github.togls.kp2acomposekeyboard.domain.keyboard

class KeyboardQuickActionSlotsReducer(
    private val allowedItemIds: List<KeyboardQuickActionId> = KeyboardQuickActionId.productionItems,
) {

    fun sanitize(slots: KeyboardQuickActionSlots): KeyboardQuickActionSlots {
        val allowedCenterItems = slots.centerItemIds
            .filter { itemId -> itemId in allowedItemIds }
            .distinct()

        val allowedRightItem = slots.rightItemId
            ?.takeIf { itemId -> itemId in allowedItemIds }
            ?.takeUnless { itemId -> itemId in allowedCenterItems }

        val maxCenterSize = if (allowedRightItem == null) {
            KeyboardQuickActionSlots.MAX_PINNED_ITEMS
        } else {
            KeyboardQuickActionSlots.MAX_PINNED_ITEMS - 1
        }

        return KeyboardQuickActionSlots(
            centerItemIds = allowedCenterItems.take(maxCenterSize),
            rightItemId = allowedRightItem,
        )
    }

    fun moveToCenter(
        slots: KeyboardQuickActionSlots,
        itemId: KeyboardQuickActionId,
        targetIndex: Int,
    ): KeyboardQuickActionSlots {
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
        slots: KeyboardQuickActionSlots,
        itemId: KeyboardQuickActionId,
    ): KeyboardQuickActionSlots {
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
        slots: KeyboardQuickActionSlots,
        itemId: KeyboardQuickActionId,
    ): KeyboardQuickActionSlots {
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
        slots: KeyboardQuickActionSlots,
        itemId: KeyboardQuickActionId,
    ): Boolean {
        return itemId in allowedItemIds &&
                (itemId in slots.centerItemIds ||
                        itemId == slots.rightItemId ||
                        slots.pinnedCount < KeyboardQuickActionSlots.MAX_PINNED_ITEMS)
    }

    private fun canMoveToRight(itemId: KeyboardQuickActionId): Boolean {
        // Right-slot replacement is allowed even when pinned count is already
        // full, as long as the item is a supported production quick action.
        return itemId in allowedItemIds
    }
}
