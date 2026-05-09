package io.github.togls.kp2acomposekeyboard.session

import io.github.togls.kp2acomposekeyboard.security.SecureLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTimeoutController @Inject constructor(
    private val sessionRepository: KeyboardSessionRepository,
) {

    private var timeoutJob: Job? = null

    fun restartTimeout(
        scope: CoroutineScope,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ) {
        timeoutJob?.cancel()

        timeoutJob = scope.launch {
            SecureLog.d("Session timeout scheduled")
            delay(timeoutMillis)
            sessionRepository.clear()
        }
    }

    fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
        SecureLog.d("Session timeout canceled")
    }

    fun clearNow() {
        cancelTimeout()
        sessionRepository.clear()
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 60_000L
    }
}