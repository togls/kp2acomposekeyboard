package io.github.togls.kp2acomposekeyboard.feature.entrypicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.R

@Composable
fun EntryPickerScreen(
    state: EntryPickerUiState,
    onIntent: (EntryPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.status) {
                EntryPickerStatus.Idle,
                EntryPickerStatus.Selecting,
                    -> SelectingContent(
                    message = state.message,
                    onCancel = { onIntent(EntryPickerIntent.Cancel) },
                )

                EntryPickerStatus.Completed -> CompletedContent(
                    message = state.message,
                )

                EntryPickerStatus.Failed -> FailedContent(
                    message = state.message,
                    onRetry = { onIntent(EntryPickerIntent.Retry) },
                    onCancel = { onIntent(EntryPickerIntent.Cancel) },
                )

                EntryPickerStatus.Cancelled -> CancelledContent(
                    message = state.message,
                )
            }
        }
    }
}

@Composable
private fun SelectingContent(
    message: String?,
    onCancel: () -> Unit,
) {
    CircularProgressIndicator()

    Text(
        text = message ?: stringResource(R.string.entry_picker_opening_kp2a),
        style = MaterialTheme.typography.titleMedium,
    )

    OutlinedButton(onClick = onCancel) {
        Text(text = stringResource(R.string.action_cancel))
    }
}

@Composable
private fun CompletedContent(
    message: String?,
) {
    Text(
        text = message ?: stringResource(R.string.entry_picker_selection_completed),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun FailedContent(
    message: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(
        text = stringResource(R.string.entry_picker_selection_failed_title),
        style = MaterialTheme.typography.titleMedium,
    )

    Text(
        text = message ?: stringResource(R.string.entry_picker_kp2a_unavailable),
        style = MaterialTheme.typography.bodyMedium,
    )

    Button(onClick = onRetry) {
        Text(text = stringResource(R.string.action_retry))
    }

    OutlinedButton(onClick = onCancel) {
        Text(text = stringResource(R.string.action_cancel))
    }
}

@Composable
private fun CancelledContent(
    message: String?,
) {
    Text(
        text = message ?: stringResource(R.string.entry_picker_selection_cancelled),
        style = MaterialTheme.typography.titleMedium,
    )
}
