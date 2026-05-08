package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
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

@AndroidEntryPoint
class EntryPickerActivity : ComponentActivity() {

    private val viewModel: EntryPickerViewModel by viewModels()

    private val kp2aLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        Log.d("Kp2aKeyboardIme", "receive Kp2a")

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

    companion object {
        const val EXTRA_TARGET_PACKAGE_NAME =
            "io.github.togls.kp2acomposekeyboard.extra.TARGET_PACKAGE_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.onIntent(EntryPickerIntent.StartSelection)
                }

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            EntryPickerEffect.LaunchKp2a -> launchKp2a()
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

    private fun launchKp2a() {
        try {
            val targetPackageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE_NAME)
            val searchText = Kp2aContract.appQuery(targetPackageName)

            kp2aLauncher.launch(
                Kp2aContract.createQueryEntryIntent(searchText),
            )
        } catch (_: ActivityNotFoundException) {
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        } catch (_: SecurityException) {
            viewModel.onIntent(EntryPickerIntent.Kp2aLaunchFailed)
        }
    }
}