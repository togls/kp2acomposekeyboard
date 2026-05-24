package io.github.togls.kp2acomposekeyboard.ime

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
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
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardSubtype
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardViewModel
import io.github.togls.kp2acomposekeyboard.domain.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.feature.settings.SettingsActivity
import io.github.togls.kp2acomposekeyboard.feature.settings.SettingsRepository
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardRoot
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main IME service for KP2A Compose Keyboard.
 *
 * InputMethodService is not an Activity, but Compose, ViewModel, Lifecycle, and
 * rememberSaveable still require ViewTree owners. This service owns those objects
 * manually and installs them onto the IME view tree.
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

    @Inject
    lateinit var subtypeSynchronizer: KeyboardSubtypeSynchronizer

    private lateinit var viewModel: KeyboardViewModel

    /**
     * IME services do not have an Activity lifecycle, so the lifecycle state is
     * driven manually from InputMethodService callbacks.
     */
    private val lifecycleRegistry = LifecycleRegistry(this)

    /**
     * Saved state support required by Compose and rememberSaveable.
     */
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    /**
     * Coroutine scope tied to the service lifecycle.
     *
     * SupervisorJob prevents one failed effect handler from cancelling all effect
     * collection. Main.immediate is used because InputConnection and UI operations
     * must run on the main thread.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /**
     * ViewModel store owned by this IME service.
     */
    override val viewModelStore = ViewModelStore()

    /**
     * The active InputConnection can change when the focused editor changes, so it
     * is resolved lazily through a provider.
     */
    private val inputConnectionDispatcher by lazy {
        InputConnectionDispatcher(
            inputConnectionProvider = { currentInputConnection },
        )
    }

    private val inputMethodManager: InputMethodManager? by lazy {
        getSystemService(InputMethodManager::class.java)
    }

    private val imeId: String
        get() = ComponentName(this, KeyboardImeService::class.java).flattenToShortString()

    private val imeToken: IBinder?
        get() = window?.window?.attributes?.token

    /**
     * Tracks whether the IME is currently launching the entry picker.
     *
     * Normal IME destruction -> clear the current session.
     * Temporary destruction caused by EntryPicker launch -> keep the current session.
     * User cancels KP2A -> keep the previous session.
     * User selects a new entry -> repository replaces the previous session.
     */
    private var entryPickerFlowActive = false

    override fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        super.onCreate()

        viewModel = viewModelFactory.create()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        collectKeyboardEffects()
        collectSettingsForSubtypeSync()

        SecureLog.d("IME created")
    }

    override fun onBindInput() {
        super.onBindInput()

        // The IME is now bound to an input target and can enter STARTED state.
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        SecureLog.d("onBindInput")
    }

    override fun onCreateInputView(): View {
        SecureLog.d("Input view created")

        // Some ROMs may access the decor view before ComposeView is fully attached.
        installViewTreeOwners(window?.window?.decorView)

        // Use a safe default before settings are collected inside Compose.
        configureImeNavigationBar(isDarkTheme = false)

        syncCurrentSubtypeFromSystem()

        return ComposeView(this).apply {
            installViewTreeOwners(this)

            /*
             * The IME window container is created by the system.
             *
             * On some ROMs or Android versions, Compose may look up ViewTree owners
             * from rootView or decorView instead of only the ComposeView itself.
             * Reinstalling owners after attach makes the lookup more reliable.
             */
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

            /*
             * IME input views may be repeatedly attached and detached by the system.
             *
             * Disposing the composition on detach prevents stale keyboard views,
             * state collectors, and callbacks from leaking.
             */
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                    initialValue = KeyboardSettings(),
                )

                val isDarkTheme = shouldUseDarkTheme(settings)

                // Keep the system navigation bar in sync with the active theme.
                configureImeNavigationBar(isDarkTheme = isDarkTheme)

                KeyboardTheme(settings = settings) {
                    KeyboardRoot(
                        state = state,
                        settings = settings,
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

        SecureLog.d(
            "onStartInput",
            "restarting" to restarting,
            "inputType" to attribute?.inputType,
        )
    }

    override fun onStartInputView(
        info: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(info, restarting)

        // The input view is visible, so Compose can move to RESUMED state.
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        installViewTreeOwners(window?.window?.decorView)

        syncCurrentSubtypeFromSystem()

        // A new input view session starts outside the entry picker flow by default.
        entryPickerFlowActive = false

        SecureLog.d("input view started")
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        applyCurrentSubtype(newSubtype)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        // The input view is no longer visible, but the service may still be alive.
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        SecureLog.d("Input view finished")
        super.onFinishInputView(finishingInput)
    }

    override fun onUnbindInput() {
        // The input target is unbound. Keep CREATED state until the next bind or destroy.
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        SecureLog.d("onUnbindInput")
        super.onUnbindInput()
    }

    override fun onWindowShown() {
        super.onWindowShown()

        // The decor view may become available again when the IME window is shown.
        installViewTreeOwners(window?.window?.decorView)

        SecureLog.d("onWindowShown")
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()

        /*
         * Force the input view to show during development.
         *
         * This avoids false negatives caused by hardware keyboard state, emulator
         * behavior, or vendor-specific IME policies.
         */
        return true
    }

    override fun onDestroy() {
        /*
         * Clear the current entry only during normal IME destruction.
         *
         * When launching EntryPicker, the system may temporarily destroy the IME.
         * In that case, the session must be preserved until the picker returns.
         */
        if (::viewModel.isInitialized && !entryPickerFlowActive) {
            viewModel.onIntent(KeyboardIntent.ClearEntry)
        }

        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        viewModelStore.clear()

        SecureLog.d("ime destroyed")
        super.onDestroy()
    }

    /**
     * Collects one-off effects emitted by the ViewModel.
     *
     * Examples include committing text, deleting text, sending Enter, and launching
     * picker or settings screens.
     */
    private fun collectKeyboardEffects() {
        serviceScope.launch {
            viewModel.effect.collect { effect ->
                handleKeyboardEffect(effect)
            }
        }
    }

    /**
     * Keeps dynamically registered subtypes aligned with app settings.
     */
    private fun collectSettingsForSubtypeSync() {
        serviceScope.launch {
            settingsRepository.settings.collect { settings ->
                subtypeSynchronizer.synchronize(settings)
            }
        }
    }

    /**
     * Handles side effects requested by the keyboard UI.
     */
    private fun handleKeyboardEffect(effect: KeyboardEffect) {
        when (effect) {
            is KeyboardEffect.CommitText -> {
                // The text may contain passwords, TOTP codes, or recovery codes. Never log it.
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
                SecureLog.d("Launch entry picker requested")
            }

            KeyboardEffect.LaunchSettings -> {
                launchSettingsActivity()
                SecureLog.d("Launch settings requested")
            }

            is KeyboardEffect.SwitchToSubtype -> {
                switchToSubtype(effect.subtype)
            }

            KeyboardEffect.SwitchToNextInputMethod -> {
                switchToNextKeyboard()
            }
        }
    }

    private fun syncCurrentSubtypeFromSystem() {
        val subtype = inputMethodManager?.currentInputMethodSubtype
        applyCurrentSubtype(subtype)
    }

    private fun applyCurrentSubtype(subtype: InputMethodSubtype?) {
        val keyboardSubtype = KeyboardSubtypeRegistry.fromInputMethodSubtype(subtype)
        viewModel.onIntent(KeyboardIntent.ChangeSubtype(keyboardSubtype))
        SecureLog.d(
            message = "ime subtype applied",
            "subtype" to keyboardSubtype.name,
        )
    }

    private fun switchToSubtype(subtype: KeyboardSubtype) {
        val inputMethodSubtype = KeyboardSubtypeRegistry.inputMethodSubtypeFor(subtype)
        if (inputMethodSubtype == null) {
            SecureLog.w(
                message = "subtype switch ignored",
                throwable = null,
                "subtype" to subtype.name,
            )
            return
        }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                switchInputMethod(imeId, inputMethodSubtype)
            } else {
                val token = imeToken ?: error("IME token unavailable")
                @Suppress("DEPRECATION")
                inputMethodManager?.setInputMethodAndSubtype(token, imeId, inputMethodSubtype)
                    ?: error("InputMethodManager unavailable")
            }
        }.onFailure { error ->
            SecureLog.w(
                message = "subtype switch failed",
                throwable = error,
                "subtype" to subtype.name,
                "errorType" to error::class.java.simpleName,
            )
            switchToNextKeyboard()
        }
    }

    private fun switchToNextKeyboard() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                switchToNextInputMethod(false)
            } else {
                val token = imeToken ?: error("IME token unavailable")
                @Suppress("DEPRECATION")
                inputMethodManager?.switchToNextInputMethod(token, false)
                    ?: error("InputMethodManager unavailable")
            }
        }.onFailure { error ->
            SecureLog.w(
                message = "next input method switch failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        }
    }

    /**
     * Launches the entry picker from the IME service.
     */
    private fun launchEntryPickerActivity() {
        entryPickerFlowActive = true

        val intent = Intent(this, EntryPickerActivity::class.java).apply {
            // InputMethodService is not an Activity context, so a new task is required.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // The picker should not appear in the recent tasks list.
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        SecureLog.intent(
            message = "launch entry picker activity",
            intent = intent,
        )

        try {
            startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            // Reset the flag so onDestroy does not treat this as an active picker flow.
            entryPickerFlowActive = false

            SecureLog.w(
                message = "entry picker activity launch failed",
                throwable = error,
            )
        } catch (error: SecurityException) {
            // Reset the flag when the system blocks the launch.
            entryPickerFlowActive = false

            SecureLog.w(
                message = "entry picker activity launch failed",
                throwable = error,
            )
        }
    }

    /**
     * Installs the owners required by Compose onto the given View.
     *
     * IME views are not normal Activity content views, so LifecycleOwner,
     * SavedStateRegistryOwner, and ViewModelStoreOwner must be attached manually.
     */
    private fun installViewTreeOwners(view: View?) {
        if (view == null) {
            return
        }

        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
    }

    /**
     * Configures the system navigation bar for the IME window.
     *
     * Goals:
     * 1. Make the navigation bar transparent so the keyboard can visually extend
     *    into the navigation area.
     * 2. Disable Android 10+ contrast enforcement to avoid unexpected system scrims.
     * 3. Use dark or light navigation icons based on the active theme.
     */
    private fun configureImeNavigationBar(isDarkTheme: Boolean) {
        val imeWindow = window?.window ?: return

        imeWindow.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            imeWindow.isNavigationBarContrastEnforced = false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val lightNavigationBarFlag = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

            imeWindow.decorView.systemUiVisibility = if (isDarkTheme) {
                // Dark theme should use light navigation bar icons.
                imeWindow.decorView.systemUiVisibility and lightNavigationBarFlag.inv()
            } else {
                // Light theme should use dark navigation bar icons.
                imeWindow.decorView.systemUiVisibility or lightNavigationBarFlag
            }
        }
    }

    /**
     * Returns whether the keyboard should use a dark theme.
     *
     * The current implementation follows the system night mode. The settings
     * parameter is kept for future theme modes, such as system, light, or dark.
     */
    private fun shouldUseDarkTheme(settings: KeyboardSettings): Boolean {
        return resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Launches the settings screen from the IME service.
     */
    private fun launchSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            // InputMethodService is not an Activity context, so a new task is required.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        SecureLog.intent(
            message = "launch settings activity",
            intent = intent,
        )

        try {
            startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            SecureLog.w(
                message = "settings activity launch failed",
                throwable = error,
                "errorType" to error::class.java.simpleName,
            )
        } catch (error: SecurityException) {
            SecureLog.w(
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
