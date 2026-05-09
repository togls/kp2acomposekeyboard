package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.togls.kp2acomposekeyboard.kp2a.Kp2aContract
import io.github.togls.kp2acomposekeyboard.kp2a.Kp2aEntryResultParser
import io.github.togls.kp2acomposekeyboard.kp2a.Kp2aPluginAccess
import io.github.togls.kp2acomposekeyboard.security.DebugLog
import io.github.togls.kp2acomposekeyboard.settings.KeyboardSettings
import io.github.togls.kp2acomposekeyboard.settings.SettingsRepository
import io.github.togls.kp2acomposekeyboard.ui.theme.KeyboardTheme
import keepass2android.pluginsdk.Strings
import javax.inject.Inject

@AndroidEntryPoint
class EntryPickerActivity : ComponentActivity() {

    @Inject
    lateinit var resultParser: Kp2aEntryResultParser

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val viewModel: EntryPickerViewModel by viewModels()
    private var kp2aLaunchStarted = false

    private val kp2aLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleKp2aResult(result.resultCode, result.data)
        }

    private val kp2aPluginSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleReturnedFromPluginSettings()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = KeyboardSettings(),
            )

            KeyboardTheme(settings = settings) {
                LaunchedEffect(Unit) {
                    viewModel.onIntent(EntryPickerIntent.StartSelection)
                    launchKp2aOnce()
                }

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            EntryPickerEffect.Finish -> finish()
                        }
                    }
                }

                EntryPickerScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                )
            }
        }
    }

    private fun handleKp2aResult(
        resultCode: Int,
        data: Intent?,
    ) {
        DebugLog.bundleKeys(
            message = "kp2a result received",
            bundle = data?.extras,
            "resultCode" to resultCode,
            "hasData" to (data != null),
        )

        when {
            resultCode == RESULT_CANCELED -> {
                viewModel.onIntent(EntryPickerIntent.Kp2aResultCancelled)
            }

            Kp2aContract.isSuccessfulResult(
                resultCode = resultCode,
                data = data,
            ) -> {
                viewModel.onIntent(
                    EntryPickerIntent.Kp2aResultSucceeded(
                        result = resultParser.parse(data),
                    ),
                )
            }

            else -> {
                viewModel.onIntent(EntryPickerIntent.Kp2aResultFailed)
            }
        }
    }

    private fun launchKp2aOnce() {
        if (kp2aLaunchStarted) {
            DebugLog.d("skip launch KP2A because launch already started")
            return
        }

        kp2aLaunchStarted = true

        try {
            if (!Kp2aPluginAccess.hasRequiredAccess(this)) {
                DebugLog.d("kp2a plugin access not granted")
                openKp2aPluginSettings()
                return
            }

            val intent = Kp2aContract.createQueryEntryIntent(searchText = null)

            DebugLog.intent(
                message = "launch kp2a query entry",
                intent = intent,
                "queryMode" to "manual",
            )

            kp2aLauncher.launch(intent)
        } catch (throwable: ActivityNotFoundException) {
            handleKp2aLaunchFailure(throwable)
        } catch (throwable: SecurityException) {
            handleKp2aLaunchFailure(throwable)
        } catch (throwable: Throwable) {
            handleKp2aLaunchFailure(throwable)
        }
    }

    private fun handleReturnedFromPluginSettings() {
        DebugLog.d("returned from kp2a plugin settings")

        kp2aLaunchStarted = false

        if (!Kp2aPluginAccess.hasRequiredAccess(this)) {
            DebugLog.d("kp2a plugin access still not granted")
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
            return
        }

        DebugLog.d("kp2a plugin access granted")
        launchKp2aOnce()
    }

    private fun openKp2aPluginSettings() {
        try {
            val intent = Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS).apply {
                putExtra(Strings.EXTRA_PLUGIN_PACKAGE, packageName)
            }

            DebugLog.intent(
                message = "open kp2a plugin settings",
                intent = intent,
                "pluginPackage" to packageName,
            )

            kp2aPluginSettingsLauncher.launch(intent)
        } catch (throwable: ActivityNotFoundException) {
            handleKp2aLaunchFailure(throwable)
        } catch (throwable: SecurityException) {
            handleKp2aLaunchFailure(throwable)
        } catch (throwable: Throwable) {
            handleKp2aLaunchFailure(throwable)
        }
    }

    private fun handleKp2aLaunchFailure(throwable: Throwable) {
        kp2aLaunchStarted = false

        DebugLog.w(
            message = "kp2a launch failed",
            throwable = throwable,
            "errorType" to throwable::class.java.simpleName,
        )

        viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
    }
}
