package io.github.togls.kp2acomposekeyboard.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.keyboardSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "keyboard_settings",
)

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    val settings: Flow<KeyboardSettings> = context.keyboardSettingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            preferences.toKeyboardSettings()
        }

    suspend fun updateThemeMode(themeMode: KeyboardThemeMode) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateSessionTimeoutSeconds(seconds: Int) {
        context.keyboardSettingsDataStore.edit { preferences ->
            // 超时时间影响敏感字段在内存中的存活时长，写入前先收敛到允许范围。
            preferences[Keys.SESSION_TIMEOUT_SECONDS] = seconds.coerceIn(
                minimumValue = KeyboardSettings.MIN_SESSION_TIMEOUT_SECONDS,
                maximumValue = KeyboardSettings.MAX_SESSION_TIMEOUT_SECONDS,
            )
        }
    }

    suspend fun updateKeyboardHeightMode(heightMode: KeyboardHeightMode) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.KEYBOARD_HEIGHT_MODE] = heightMode.name
        }
    }

    suspend fun updateHapticFeedbackEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun updateKeySoundEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateKeyPreviewEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences[Keys.SHOW_KEY_PREVIEW] = enabled
        }
    }

    suspend fun resetToDefault() {
        context.keyboardSettingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun Preferences.toKeyboardSettings(): KeyboardSettings {
        return KeyboardSettings(
            themeMode = readEnum(
                key = Keys.THEME_MODE,
                defaultValue = KeyboardThemeMode.System,
            ),
            useDynamicColor = this[Keys.USE_DYNAMIC_COLOR]
                ?: KeyboardSettings().useDynamicColor,
            sessionTimeoutSeconds = (
                    this[Keys.SESSION_TIMEOUT_SECONDS]
                        ?: KeyboardSettings.DEFAULT_SESSION_TIMEOUT_SECONDS
                    ).coerceIn(
                    minimumValue = KeyboardSettings.MIN_SESSION_TIMEOUT_SECONDS,
                    maximumValue = KeyboardSettings.MAX_SESSION_TIMEOUT_SECONDS,
                ),
            keyboardHeightMode = readEnum(
                key = Keys.KEYBOARD_HEIGHT_MODE,
                defaultValue = KeyboardHeightMode.Normal,
            ),
            hapticFeedbackEnabled = this[Keys.HAPTIC_FEEDBACK_ENABLED]
                ?: KeyboardSettings().hapticFeedbackEnabled,
            keySoundEnabled = this[Keys.KEY_SOUND_ENABLED]
                ?: KeyboardSettings().keySoundEnabled,
            showKeyPreview = this[Keys.SHOW_KEY_PREVIEW]
                ?: KeyboardSettings().showKeyPreview,
        )
    }

    private inline fun <reified T : Enum<T>> Preferences.readEnum(
        key: Preferences.Key<String>,
        defaultValue: T,
    ): T {
        val rawValue = this[key] ?: return defaultValue

        return runCatching {
            enumValueOf<T>(rawValue)
        }.getOrDefault(defaultValue)
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val SESSION_TIMEOUT_SECONDS = intPreferencesKey("session_timeout_seconds")
        val KEYBOARD_HEIGHT_MODE = stringPreferencesKey("keyboard_height_mode")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("key_sound_enabled")
        val SHOW_KEY_PREVIEW = booleanPreferencesKey("show_key_preview")
    }
}