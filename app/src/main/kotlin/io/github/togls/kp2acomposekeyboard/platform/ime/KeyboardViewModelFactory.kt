package io.github.togls.kp2acomposekeyboard.platform.ime

import io.github.togls.kp2acomposekeyboard.application.settings.KeyboardSettingsStore
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.application.session.SessionTimeoutController
import javax.inject.Inject

class KeyboardViewModelFactory @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
    private val sessionTimeoutController: SessionTimeoutController,
    private val settingsStore: KeyboardSettingsStore,
) {

    fun create(): KeyboardViewModel {
        return KeyboardViewModel(
            sessionRepository = sessionRepository,
            sessionTimeoutController = sessionTimeoutController,
            settingsStore = settingsStore,
        )
    }
}
