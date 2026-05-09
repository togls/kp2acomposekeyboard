package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.feature.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout

fun KeyboardSessionSnapshot.toEntryUiState(
    previousState: KeyboardUiState,
): KeyboardUiState {
    return previousState.copy(
        mainLayout = MainKeyboardLayout.Entry,
        entryFieldDisplayMode = EntryFieldDisplayMode.Paged,
        currentEntryName = entryName,
        hasActiveSession = true,
        fixedFields = fixedFields,
        extraFields = extraFields,
        allFields = allFields,
        extraFieldPageIndex = 0,
    )
}