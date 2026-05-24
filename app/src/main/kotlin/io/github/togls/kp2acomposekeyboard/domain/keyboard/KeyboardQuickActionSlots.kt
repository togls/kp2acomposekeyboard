package io.github.togls.kp2acomposekeyboard.domain.keyboard

data class KeyboardQuickActionSlots(
    val centerItemIds: List<KeyboardQuickActionId> = listOf(SettingsQuickActionId),
    val rightItemId: KeyboardQuickActionId? = null,
) {
    val pinnedCount: Int
        get() = centerItemIds.size + if (rightItemId == null) 0 else 1

    companion object {
        const val MAX_PINNED_ITEMS = 5
    }
}
