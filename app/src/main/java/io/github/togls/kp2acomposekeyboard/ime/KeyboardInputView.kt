package io.github.togls.kp2acomposekeyboard.ime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.DefaultInputMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState

@Composable
fun KeyboardInputView(
    state: KeyboardUiState,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "KP2A Compose Keyboard",
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Plan 2.4 MVI 输入链路：${state.defaultInputMode.name}",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.CommitText("a")) },
                ) {
                    Text(text = if (state.isUppercase) "A" else "a")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.DeleteBackward) },
                ) {
                    Text(text = "⌫")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.CommitText(" ")) },
                ) {
                    Text(text = "空格")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.Enter) },
                ) {
                    Text(text = "换行")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.SwitchToLetters) },
                    enabled = state.defaultInputMode != DefaultInputMode.Letters,
                ) {
                    Text(text = "ABC")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.SwitchToNumbers) },
                    enabled = state.defaultInputMode != DefaultInputMode.Numbers,
                ) {
                    Text(text = "123")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.SwitchToSymbols) },
                    enabled = state.defaultInputMode != DefaultInputMode.Symbols,
                ) {
                    Text(text = "符号")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(KeyboardIntent.ToggleUppercase) },
                ) {
                    Text(text = "⇧")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyboardInputViewPreview() {
    MaterialTheme {
        KeyboardInputView(
            state = KeyboardUiState(),
            onIntent = {},
        )
    }
}