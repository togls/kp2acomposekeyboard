package io.github.togls.kp2acomposekeyboard.feature.keyboard

data class KeyboardUtilitySlots(
    val centerItemIds: List<KeyboardUtilityItemId> = listOf(SettingsUtilityItemId),
    val rightItemId: KeyboardUtilityItemId? = null,
) {
    val pinnedCount: Int
        get() = centerItemIds.size + if (rightItemId == null) 0 else 1

    companion object {
        const val MAX_PINNED_ITEMS = 5
    }
}
