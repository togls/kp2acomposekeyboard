package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.domain.keyboard.ClearEntryQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId

internal data class KeyboardUtilityItem(
    val id: KeyboardQuickActionId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
)

internal fun availableKeyboardUtilityItems(): List<KeyboardUtilityItem> {
    return AvailableKeyboardUtilityItems
}

internal fun KeyboardQuickActionId.toKeyboardUtilityItem(): KeyboardUtilityItem? {
    return AvailableKeyboardUtilityItems.firstOrNull { item ->
        item.id == this
    }
}

private val AvailableKeyboardUtilityItems = listOf(
    KeyboardUtilityItem(
        id = SettingsQuickActionId,
        iconRes = R.drawable.ic_settings_24,
        labelRes = R.string.keyboard_utility_settings,
        contentDescriptionRes = R.string.cd_key_open_settings,
    ),
    KeyboardUtilityItem(
        id = ClearEntryQuickActionId,
        iconRes = R.drawable.ic_delete_sweep_24,
        labelRes = R.string.keyboard_utility_clear_entry,
        contentDescriptionRes = R.string.cd_key_clear_entry,
    ),
)
