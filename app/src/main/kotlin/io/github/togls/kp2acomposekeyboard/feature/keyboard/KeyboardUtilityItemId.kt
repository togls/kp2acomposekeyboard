package io.github.togls.kp2acomposekeyboard.feature.keyboard

interface KeyboardUtilityItemId {
    val storageValue: String

    companion object {
        val productionItems: List<KeyboardUtilityItemId> = listOf(SettingsUtilityItemId)

        fun fromStorageValue(value: String): KeyboardUtilityItemId? {
            return productionItems.firstOrNull { itemId ->
                itemId.storageValue == value
            }
        }
    }
}

data object SettingsUtilityItemId : KeyboardUtilityItemId {
    override val storageValue = "settings"
}

data object ClearEntryUtilityItemId : KeyboardUtilityItemId {
    override val storageValue = "clear_entry"
}
