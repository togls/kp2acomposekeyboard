package io.github.togls.kp2acomposekeyboard.platform.ime

import io.github.togls.kp2acomposekeyboard.application.keyboard.CommitKeyboardFieldUseCase
import io.github.togls.kp2acomposekeyboard.application.keyboard.ObserveKeyboardSessionSnapshotUseCase
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import javax.inject.Inject

class KeyboardViewModelFactory @Inject constructor(
    private val observeKeyboardSessionSnapshot: ObserveKeyboardSessionSnapshotUseCase,
    private val commitKeyboardField: CommitKeyboardFieldUseCase,
    private val sessionTimeoutController: SessionTimeoutController,
    private val settingsStore: KeyboardSettingsStore,
) {

    fun create(): KeyboardViewModel {
        return KeyboardViewModel(
            observeKeyboardSessionSnapshot = observeKeyboardSessionSnapshot,
            commitKeyboardField = commitKeyboardField,
            sessionTimeoutController = sessionTimeoutController,
            settingsStore = settingsStore,
        )
    }
}
