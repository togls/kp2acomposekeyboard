package io.github.togls.kp2acomposekeyboard.ime

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
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
            val imeId = imeId()
            val additionalSubtypes = KeyboardSubtypeRegistry.additionalSubtypes(settings)
            manager.setAdditionalInputMethodSubtypes(imeId, additionalSubtypes)
            val enabledSubtypeCount = synchronizeExplicitlyEnabledSubtypes(manager, imeId, settings)
            SecureLog.d(
                message = "ime subtypes synchronized",
                "englishUsEnabled" to settings.englishUsSubtypeEnabled,
                "additionalSubtypeCount" to additionalSubtypes.size,
                "enabledSubtypeCount" to enabledSubtypeCount,
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

    private fun synchronizeExplicitlyEnabledSubtypes(
        manager: InputMethodManager,
        imeId: String,
        settings: KeyboardSettings,
    ): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return -1
        }

        val enabledSubtypeHashCodes = KeyboardSubtypeRegistry.explicitlyEnabledSubtypeHashCodes(settings)
        manager.setExplicitlyEnabledInputMethodSubtypes(imeId, enabledSubtypeHashCodes)
        return enabledSubtypeHashCodes.size
    }

    private fun imeId(): String {
        return ComponentName(context, KeyboardImeService::class.java).flattenToShortString()
    }
}
