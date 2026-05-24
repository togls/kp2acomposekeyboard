package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.ClearEntryUtilityItemId
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.feature.keyboard.SettingsUtilityItemId

internal data class KeyboardUtilityItem(
    val id: KeyboardUtilityItemId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
)

internal fun availableKeyboardUtilityItems(): List<KeyboardUtilityItem> {
    return AvailableKeyboardUtilityItems
}

internal fun KeyboardUtilityItemId.toKeyboardUtilityItem(): KeyboardUtilityItem? {
    return AvailableKeyboardUtilityItems.firstOrNull { item ->
        item.id == this
    }
}

private val AvailableKeyboardUtilityItems = listOf(
    KeyboardUtilityItem(
        id = SettingsUtilityItemId,
        iconRes = R.drawable.ic_settings_24,
        labelRes = R.string.keyboard_utility_settings,
        contentDescriptionRes = R.string.cd_key_open_settings,
    ),
    KeyboardUtilityItem(
        id = ClearEntryUtilityItemId,
        iconRes = R.drawable.ic_delete_sweep_24,
        labelRes = R.string.keyboard_utility_clear_entry,
        contentDescriptionRes = R.string.cd_key_clear_entry,
    ),
)
