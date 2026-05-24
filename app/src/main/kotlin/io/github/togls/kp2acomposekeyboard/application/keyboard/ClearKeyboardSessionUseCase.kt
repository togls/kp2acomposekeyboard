package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import javax.inject.Inject

class ClearKeyboardSessionUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke() {
        sessionRepository.clear()
    }
}
