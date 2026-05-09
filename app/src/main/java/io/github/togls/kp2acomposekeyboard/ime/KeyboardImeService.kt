package io.github.togls.kp2acomposekeyboard.ime

import android.content.ActivityNotFoundException
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint
import io.github.togls.kp2acomposekeyboard.feature.entrypicker.EntryPickerActivity
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardEffect
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import io.github.togls.kp2acomposekeyboard.feature.settings.SettingsActivity
import io.github.togls.kp2acomposekeyboard.security.DebugLog
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.security.SecureLogEvent
import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.settings.SettingsRepository
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardRoot
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main input method host for KP2A Compose Keyboard.
 */
@AndroidEntryPoint
class KeyboardImeService :
    InputMethodService(),
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    @Inject
    lateinit var viewModelFactory: KeyboardViewModelFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: KeyboardViewModel

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore = ViewModelStore()

    private val inputConnectionDispatcher by lazy {
        InputConnectionDispatcher(
            inputConnectionProvider = { currentInputConnection },
        )
    }

    /**
     * 普通销毁 IME -> 清理 Session
     * 启动 EntryPicker 导致 IME 临时销毁 -> 不清理 Session
     * 用户取消 KP2A -> 旧 Session 保留
     * 用户成功选择新条目 -> Repository 覆盖旧 Session
     */
    private var entryPickerFlowActive = false

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        super.onCreate()

        viewModel = viewModelFactory.create()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        collectKeyboardEffects()

        SecureLog.debug(SecureLogEvent.ImeCreated, TAG)
    }

    override fun onBindInput() {
        super.onBindInput()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        Log.d(TAG, "onBindInput")
    }

    override fun onCreateInputView(): View {
        SecureLog.debug(SecureLogEvent.InputViewCreated, TAG)

        installViewTreeOwners(window?.window?.decorView)

        return ComposeView(this).apply {
            installViewTreeOwners(this)

            // IME 的窗口父容器由系统创建；部分 ROM 上 Compose 会从 rootView/parentPanel 查找 owner。
            addOnAttachStateChangeListener(
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(view: View) {
                        installViewTreeOwners(view)
                        installViewTreeOwners(view.rootView)
                        installViewTreeOwners(window?.window?.decorView)
                    }

                    override fun onViewDetachedFromWindow(view: View) = Unit
                },
            )

            // IME 输入视图可能被系统反复 attach/detach，detach 时释放 composition，避免旧键盘视图泄漏。
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                    initialValue = KeyboardSettings(),
                )

                KeyboardTheme(settings = settings) {
                    KeyboardRoot(
                        state = state,
                        onIntent = viewModel::onIntent,
                    )
                }
            }
        }
    }

    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)
        Log.d(TAG, "onStartInput restarting=$restarting inputType=${attribute?.inputType}")
    }

    override fun onStartInputView(
        info: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        installViewTreeOwners(window?.window?.decorView)
        entryPickerFlowActive = false
        DebugLog.d("input view started")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        SecureLog.debug(SecureLogEvent.InputViewFinished, TAG)
        super.onFinishInputView(finishingInput)
    }

    override fun onUnbindInput() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        Log.d(TAG, "onUnbindInput")
        super.onUnbindInput()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        installViewTreeOwners(window?.window?.decorView)
        Log.d(TAG, "onWindowShown")
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        // 开发阶段强制显示输入视图，避免硬键盘状态或厂商策略让系统误判“不需要软键盘”。
        return true
    }

    override fun onDestroy() {
        if (::viewModel.isInitialized && !entryPickerFlowActive) {
            viewModel.onIntent(KeyboardIntent.ClearEntry)
        }

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        viewModelStore.clear()
        DebugLog.d("ime destroyed")
        super.onDestroy()
    }

    private fun collectKeyboardEffects() {
        serviceScope.launch {
            viewModel.effect.collect { effect ->
                handleKeyboardEffect(effect)
            }
        }
    }

    private fun handleKeyboardEffect(effect: KeyboardEffect) {
        when (effect) {
            is KeyboardEffect.CommitText -> {
                // effect.text 后续可能是密码、TOTP 或恢复码，禁止打印。
                inputConnectionDispatcher.commitText(effect.text)
            }

            KeyboardEffect.DeleteBackward -> {
                inputConnectionDispatcher.deleteBackward()
            }

            KeyboardEffect.SendEnter -> {
                inputConnectionDispatcher.sendEnter()
            }

            is KeyboardEffect.LaunchEntryPicker -> {
                launchEntryPickerActivity()
                SecureLog.debug(SecureLogEvent.LaunchEntryPickerRequested, TAG)
            }

            KeyboardEffect.LaunchSettings -> {
                launchSettingsActivity()
                SecureLog.debug(SecureLogEvent.LaunchSettingsRequested, TAG)
            }
        }
    }

    private fun launchEntryPickerActivity() {
        entryPickerFlowActive = true

        val intent = Intent(this, EntryPickerActivity::class.java).apply {
            // InputMethodService 不是 Activity Context；从 Service 启动 Activity 必须使用新任务。
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        DebugLog.intent(
            message = "launch entry picker activity",
            intent = intent,
        )

        try {
            startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            entryPickerFlowActive = false
            DebugLog.w(
                message = "entry picker activity launch failed",
                throwable = error,
            )
        } catch (error: SecurityException) {
            entryPickerFlowActive = false
            DebugLog.w(
                message = "entry picker activity launch failed",
                throwable = error,
            )
        }
    }

    private fun installViewTreeOwners(view: View?) {
        if (view == null) {
            return
        }

        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
    }

    private fun launchSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            // InputMethodService 不是 Activity Context，从 IME 启动设置页必须新建任务。
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        DebugLog.intent(
            message = "launch settings activity",
            intent = intent,
        )

        try {
            startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            DebugLog.w(
                message = "settings activity launch failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        } catch (error: SecurityException) {
            DebugLog.w(
                message = "settings activity launch failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        }
    }

    companion object {
        private const val TAG = "Kp2aKeyboardIme"
    }
}