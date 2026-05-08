package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import android.content.ActivityNotFoundException
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
import io.github.togls.kp2acomposekeyboard.security.SecureLog
import io.github.togls.kp2acomposekeyboard.security.SecureLogEvent

@AndroidEntryPoint
class EntryPickerActivity : ComponentActivity() {

    private val viewModel: EntryPickerViewModel by viewModels()

    private var kp2aLaunchStarted = false

    private val kp2aLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data

        when {
            result.resultCode == RESULT_CANCELED -> {
                viewModel.onIntent(EntryPickerIntent.Kp2aResultCancelled)
            }

            Kp2aContract.isSuccessfulResult(
                resultCode = result.resultCode,
                data = data,
            ) -> {
                viewModel.onIntent(
                    EntryPickerIntent.Kp2aResultSucceeded(
                        result = Kp2aContract.parseEntryResult(data),
                    ),
                )
            }

            else -> {
                viewModel.onIntent(EntryPickerIntent.Kp2aResultFailed)
            }
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
            return
        }

        kp2aLaunchStarted = true

        try {
            val targetPackageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE_NAME)
            val searchText = Kp2aContract.appQuery(targetPackageName)

            SecureLog.debug(SecureLogEvent.Kp2aLaunchRequested)

            kp2aLauncher.launch(
                Kp2aContract.createQueryEntryIntent(searchText),
            )
        } catch (_: ActivityNotFoundException) {
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        } catch (_: SecurityException) {
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        }
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE_NAME =
            "io.github.togls.kp2acomposekeyboard.extra.TARGET_PACKAGE_NAME"
    }
}