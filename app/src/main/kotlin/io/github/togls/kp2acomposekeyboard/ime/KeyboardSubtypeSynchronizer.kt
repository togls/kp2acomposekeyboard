package io.github.togls.kp2acomposekeyboard.ime

import android.content.ComponentName
import android.content.Context
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.togls.kp2acomposekeyboard.feature.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardSubtypeSynchronizer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val inputMethodManager: InputMethodManager? =
        context.getSystemService(InputMethodManager::class.java)

    @Suppress("DEPRECATION")
    fun synchronize(settings: KeyboardSettings): Boolean {
        val manager = inputMethodManager
        if (manager == null) {
            SecureLog.w(
                message = "input method manager unavailable for subtype sync",
                throwable = null,
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
            )
            return false
        }

        return runCatching {
            val additionalSubtypes = KeyboardSubtypeRegistry.additionalSubtypes(settings)
            manager.setAdditionalInputMethodSubtypes(imeId(), additionalSubtypes)
            SecureLog.d(
                message = "ime subtypes synchronized",
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
                "additionalSubtypeCount" to additionalSubtypes.size,
            )
            true
        }.getOrElse { error ->
            SecureLog.w(
                message = "ime subtype sync failed",
                throwable = error,
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
                "errorType" to error::class.java.simpleName,
            )
            false
        }
    }

    private fun imeId(): String {
        return ComponentName(context, KeyboardImeService::class.java).flattenToShortString()
    }
}
