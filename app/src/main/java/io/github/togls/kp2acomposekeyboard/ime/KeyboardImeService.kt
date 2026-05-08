package io.github.togls.kp2acomposekeyboard.ime

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
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardEffect
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        super.onCreate()

        viewModel = viewModelFactory.create()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        collectKeyboardEffects()

        Log.d(TAG, "onCreate")
    }

    override fun onBindInput() {
        super.onBindInput()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        Log.d(TAG, "onBindInput")
    }

    override fun onCreateInputView(): View {
        Log.d(TAG, "onCreateInputView")

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
                MaterialTheme {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    KeyboardInputView(
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
        Log.d(TAG, "onStartInputView restarting=$restarting inputType=${info?.inputType}")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        Log.d(TAG, "onFinishInputView finishingInput=$finishingInput")
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
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        viewModelStore.clear()
        Log.d(TAG, "onDestroy")
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

            KeyboardEffect.LaunchEntryPicker -> {
                Log.d(TAG, "LaunchEntryPicker is not implemented yet")
            }

            KeyboardEffect.LaunchSettings -> {
                Log.d(TAG, "LaunchSettings is not implemented yet")
            }

            is KeyboardEffect.ScrollExpandedFields -> {
                // 展开字段滚动需要等字段区域组件存在后再处理。
                Log.d(TAG, "ScrollExpandedFields is not implemented yet")
            }
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

    companion object {
        private const val TAG = "Kp2aKeyboardIme"
    }
}