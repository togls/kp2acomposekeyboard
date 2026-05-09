package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import android.app.Activity
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
import io.github.togls.kp2acomposekeyboard.security.DebugLog
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.security.SecureLogEvent
import keepass2android.pluginsdk.AccessManager
import keepass2android.pluginsdk.Kp2aControl
import keepass2android.pluginsdk.Strings

@AndroidEntryPoint
class EntryPickerActivity : ComponentActivity() {

    private val viewModel: EntryPickerViewModel by viewModels()

    private var kp2aLaunchStarted = false

    private val kp2aLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            DebugLog.d(
                "KP2A result received: resultCode=${result.resultCode}, data=${result.data != null}",
            )

            if (result.resultCode != Activity.RESULT_OK) {
                DebugLog.d("KP2A result canceled or failed")

                viewModel.onIntent(
                    EntryPickerIntent.Kp2aLaunchFailed,
                )
                return@registerForActivityResult
            }

            val data = result.data

            if (data == null) {
                viewModel.onIntent(
                    EntryPickerIntent.Kp2aLaunchFailed,
                )
                return@registerForActivityResult
            }

            runCatching {
                Kp2aControl.getEntryFieldsFromIntent(data)
            }.onSuccess { fields ->
                DebugLog.d("$fields")

                if (fields.isEmpty()) {
                    DebugLog.d("KP2A returned empty fields")

                    viewModel.onIntent(
                        EntryPickerIntent.Kp2aLaunchFailed,
                    )
                    return@onSuccess
                }

                viewModel.onIntent(
                    EntryPickerIntent.Kp2aEntrySelected(fields = fields),
                )
            }.onFailure { throwable ->
                DebugLog.d(
                    "Failed to parse KP2A result: ${throwable::class.java.simpleName}: ${throwable.message}",
                )

                viewModel.onIntent(
                    EntryPickerIntent.Kp2aLaunchFailed,
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

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

    private fun launchKp2aOnce() {
        if (kp2aLaunchStarted) {
            DebugLog.d("skip launch KP2A because launch already started")
            return
        }

        kp2aLaunchStarted = true

        try {
            if (AccessManager.getAllHostPackages(this).isEmpty()) {
                DebugLog.d("kp2a: plugin access not granted, open plugin settings")
                openKp2aPluginSettings()
                return
            }

            val targetPackageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE_NAME)
            val searchText = Kp2aContract.appQuery(targetPackageName)

            DebugLog.d(
                "launch KP2A query entry: targetPackageName=$targetPackageName, searchText=$searchText",
            )

            SecureLog.debug(SecureLogEvent.Kp2aLaunchRequested)

            val kp2aIntent = Kp2aControl.getQueryEntryIntent(searchText)

            DebugLog.d(
                "KP2A intent created: action=${kp2aIntent.action}, package=${kp2aIntent.`package`}, component=${kp2aIntent.component}",
            )

            kp2aLauncher.launch(kp2aIntent)

            DebugLog.d("KP2A launcher.launch() called")
        } catch (throwable: ActivityNotFoundException) {
            DebugLog.d("KP2A launch failed: ActivityNotFoundException")

            kp2aLaunchStarted = false

            viewModel.onIntent(
                EntryPickerIntent.Kp2aLaunchFailed,
            )
        } catch (throwable: SecurityException) {
            DebugLog.d("KP2A launch failed: SecurityException: ${throwable.message}")

            kp2aLaunchStarted = false

            viewModel.onIntent(
                EntryPickerIntent.Kp2aLaunchFailed,
            )
        } catch (throwable: Throwable) {
            DebugLog.d(
                "KP2A launch failed: ${throwable::class.java.simpleName}: ${throwable.message}",
            )

            kp2aLaunchStarted = false

            viewModel.onIntent(
                EntryPickerIntent.Kp2aLaunchFailed,
            )
        }
    }

    private val kp2aPluginSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            DebugLog.d("kp2a: returned from plugin settings")

            kp2aLaunchStarted = false

            if (AccessManager.getAllHostPackages(this).isEmpty()) {
                DebugLog.d("kp2a: plugin access still not granted")
                viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
                return@registerForActivityResult
            }

            DebugLog.d("kp2a: plugin access granted, launch query again")
            launchKp2aOnce()
        }

    private fun openKp2aPluginSettings() {
        try {
            val intent = Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS).apply {
                putExtra(Strings.EXTRA_PLUGIN_PACKAGE, packageName)
            }

            DebugLog.d(
                "kp2a: open KP2A plugin settings: " +
                        "action=${intent.action}, pluginPackage=$packageName",
            )

            kp2aPluginSettingsLauncher.launch(intent)
        } catch (throwable: ActivityNotFoundException) {
            kp2aLaunchStarted = false
            DebugLog.d("kp2a: plugin settings ActivityNotFoundException")
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        } catch (throwable: SecurityException) {
            kp2aLaunchStarted = false
            DebugLog.d("kp2a: plugin settings SecurityException: ${throwable.message}")
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        } catch (throwable: Throwable) {
            kp2aLaunchStarted = false
            DebugLog.d(
                "kp2a: plugin settings failed: " +
                        "${throwable::class.java.simpleName}: ${throwable.message}",
            )
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        }
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE_NAME =
            "io.github.togls.kp2acomposekeyboard.extra.TARGET_PACKAGE_NAME"
    }
}
