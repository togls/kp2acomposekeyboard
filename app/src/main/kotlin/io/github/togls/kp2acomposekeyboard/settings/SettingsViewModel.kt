package io.github.togls.kp2acomposekeyboard.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.settings.SettingsRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>(
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        observeSettings()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ChangeThemeMode -> {
                saveSetting {
                    settingsRepository.updateThemeMode(intent.themeMode)
                }
            }

            is SettingsIntent.ChangeDynamicColorEnabled -> {
                saveSetting {
                    settingsRepository.updateDynamicColorEnabled(intent.enabled)
                }
            }

            is SettingsIntent.ChangeSessionTimeoutSeconds -> {
                saveSetting {
                    settingsRepository.updateSessionTimeoutSeconds(intent.seconds)
                }
            }

            is SettingsIntent.ChangeKeyboardHeightMode -> {
                saveSetting {
                    settingsRepository.updateKeyboardHeightMode(intent.heightMode)
                }
            }

            is SettingsIntent.ChangeHapticFeedbackEnabled -> {
                saveSetting {
                    settingsRepository.updateHapticFeedbackEnabled(intent.enabled)
                }
            }

            is SettingsIntent.ChangeKeySoundEnabled -> {
                saveSetting {
                    settingsRepository.updateKeySoundEnabled(intent.enabled)
                }
            }

            is SettingsIntent.ChangeKeyPreviewEnabled -> {
                saveSetting {
                    settingsRepository.updateKeyPreviewEnabled(intent.enabled)
                }
            }

            SettingsIntent.ResetToDefault -> {
                saveSetting {
                    settingsRepository.resetToDefault()
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        settings = settings,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun saveSetting(
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                block()
            }.onSuccess {
                _effect.emit(SettingsEffect.ShowSavedMessage)
            }.onFailure { throwable ->
                SecureLog.w(
                    message = "settings save failed",
                    throwable = throwable,
                    "errorType" to throwable::class.java.simpleName,
                )

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "保存设置失败",
                    )
                }

                _effect.emit(
                    SettingsEffect.ShowError(
                        message = "保存设置失败",
                    ),
                )
            }
        }
    }

    private companion object {
        // 设置页可能连续快速切换多个选项，保留少量 effect 缓冲避免 Snackbar 事件丢失。
        const val EFFECT_BUFFER_CAPACITY = 8
    }
}