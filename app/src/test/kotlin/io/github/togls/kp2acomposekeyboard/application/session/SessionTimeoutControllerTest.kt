package io.github.togls.kp2acomposekeyboard.application.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeoutControllerTest {

    @Test
    fun defaultTimeoutMillis_isFiveMinuteFallback() {
        assertEquals(300_000L, SessionTimeoutController.DEFAULT_TIMEOUT_MILLIS)
    }
}
