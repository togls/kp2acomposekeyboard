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

@Composable
fun KeyboardInputView(
    onCommitText: (String) -> Unit,
    onDeleteBackward: () -> Unit,
    onSendEnter: () -> Unit,
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

            Text(text = "Plan 1.4 最小输入验证")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onCommitText("a") },
                ) {
                    Text(text = "a")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onDeleteBackward,
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
                    onClick = { onCommitText(" ") },
                ) {
                    Text(text = "空格")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSendEnter,
                ) {
                    Text(text = "换行")
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
            onCommitText = {},
            onDeleteBackward = {},
            onSendEnter = {},
        )
    }
}