package io.github.togls.kp2acomposekeyboard.ime

import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import io.github.togls.kp2acomposekeyboard.session.KeyboardSessionRepository
import javax.inject.Inject

class KeyboardViewModelFactory @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    fun create(): KeyboardViewModel {
        return KeyboardViewModel(
            sessionRepository = sessionRepository,
        )
    }
}