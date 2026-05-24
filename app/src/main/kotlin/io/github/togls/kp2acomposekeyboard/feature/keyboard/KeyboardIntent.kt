package io.github.togls.kp2acomposekeyboard.feature.keyboard

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId

sealed interface KeyboardIntent {
    data object SelectEntry : KeyboardIntent
    data object OpenSettings : KeyboardIntent
    data object ClearEntry : KeyboardIntent
    data object ToggleQuickActionPanel : KeyboardIntent
    data object CloseQuickActionPanel : KeyboardIntent
    data class ClickQuickAction(val itemId: KeyboardQuickActionId) : KeyboardIntent
    data class MoveQuickActionToCenter(
        val itemId: KeyboardQuickActionId,
        val targetIndex: Int,
    ) : KeyboardIntent

    data class MoveQuickActionToRight(val itemId: KeyboardQuickActionId) : KeyboardIntent
    data class RemoveQuickAction(val itemId: KeyboardQuickActionId) : KeyboardIntent

    data object SwitchToTextInput : KeyboardIntent
    data object SwitchToEntry : KeyboardIntent
    data class ChangeSubtype(val subtype: KeyboardSubtype) : KeyboardIntent
    data object SwitchLanguage : KeyboardIntent

    data object SwitchToLetters : KeyboardIntent
    data object SwitchToNumbers : KeyboardIntent
    data object SwitchToSymbols : KeyboardIntent

    data object ToggleUppercase : KeyboardIntent

    data class CommitText(val text: String) : KeyboardIntent
    data class CommitField(val fieldId: String) : KeyboardIntent

    data object DeleteBackward : KeyboardIntent
    data object Enter : KeyboardIntent

    data object PrevExtraFieldPage : KeyboardIntent
    data object NextExtraFieldPage : KeyboardIntent

    data object ExpandFields : KeyboardIntent
    data object CollapseFields : KeyboardIntent

    data object ScrollExpandedFieldsUp : KeyboardIntent
    data object ScrollExpandedFieldsDown : KeyboardIntent
}
