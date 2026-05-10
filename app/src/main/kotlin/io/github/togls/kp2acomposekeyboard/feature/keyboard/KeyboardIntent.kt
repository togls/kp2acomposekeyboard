package io.github.togls.kp2acomposekeyboard.feature.keyboard

sealed interface KeyboardIntent {
    data object SelectEntry : KeyboardIntent
    data object OpenSettings : KeyboardIntent
    data object ClearEntry : KeyboardIntent
    data object ToggleUtilityPanel : KeyboardIntent
    data object CloseUtilityPanel : KeyboardIntent
    data class ClickUtilityItem(val itemId: KeyboardUtilityItemId) : KeyboardIntent
    data class MoveUtilityItemToCenter(
        val itemId: KeyboardUtilityItemId,
        val targetIndex: Int,
    ) : KeyboardIntent
    data class MoveUtilityItemToRight(val itemId: KeyboardUtilityItemId) : KeyboardIntent
    data class RemoveUtilityItem(val itemId: KeyboardUtilityItemId) : KeyboardIntent

    data object SwitchToDefaultLayout : KeyboardIntent
    data object SwitchToEntryLayout : KeyboardIntent

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
