package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.domain.keyboard.ClearEntryQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId

internal data class KeyboardQuickAction(
    val id: KeyboardQuickActionId,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
    @param:StringRes val contentDescriptionRes: Int,
)

internal fun availableKeyboardQuickActions(): List<KeyboardQuickAction> {
    return AvailableKeyboardQuickActions
}

internal fun KeyboardQuickActionId.toKeyboardQuickAction(): KeyboardQuickAction? {
    return AvailableKeyboardQuickActions.firstOrNull { action ->
        action.id == this
    }
}

private val AvailableKeyboardQuickActions = listOf(
    KeyboardQuickAction(
        id = SettingsQuickActionId,
        iconRes = R.drawable.ic_settings_24,
        labelRes = R.string.keyboard_quick_action_settings,
        contentDescriptionRes = R.string.cd_key_open_settings,
    ),
    KeyboardQuickAction(
        id = ClearEntryQuickActionId,
        iconRes = R.drawable.ic_delete_sweep_24,
        labelRes = R.string.keyboard_quick_action_clear_entry,
        contentDescriptionRes = R.string.cd_key_clear_entry,
    ),
)
