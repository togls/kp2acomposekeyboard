package io.github.togls.kp2acomposekeyboard.domain.keyboard

interface KeyboardQuickActionId {
    val storageValue: String

    companion object {
        val productionItems: List<KeyboardQuickActionId> = listOf(SettingsQuickActionId)

        fun fromStorageValue(value: String): KeyboardQuickActionId? {
            return productionItems.firstOrNull { itemId ->
                itemId.storageValue == value
            }
        }
    }
}

data object SettingsQuickActionId : KeyboardQuickActionId {
    override val storageValue = "settings"
}

data object ClearEntryQuickActionId : KeyboardQuickActionId {
    override val storageValue = "clear_entry"
}
