package io.github.togls.kp2acomposekeyboard.application.keyboard

import io.github.togls.kp2acomposekeyboard.data.session.KeyboardSessionRepository
import io.github.togls.kp2acomposekeyboard.data.session.toSnapshot
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSessionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveKeyboardSessionSnapshotUseCase @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    operator fun invoke(): Flow<KeyboardSessionSnapshot?> {
        return sessionRepository.session.map { session ->
            session?.toSnapshot()
        }
    }
}
