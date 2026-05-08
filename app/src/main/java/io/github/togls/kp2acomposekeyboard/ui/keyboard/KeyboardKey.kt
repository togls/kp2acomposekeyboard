package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: KeyboardKeyEmphasis = KeyboardKeyEmphasis.Normal,
) {
    ElevatedButton(
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        enabled = enabled,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp,
        ),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = when (emphasis) {
                KeyboardKeyEmphasis.Normal -> MaterialTheme.colorScheme.surfaceContainerHighest
                KeyboardKeyEmphasis.Action -> MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = when (emphasis) {
                KeyboardKeyEmphasis.Normal -> MaterialTheme.colorScheme.onSurface
                KeyboardKeyEmphasis.Action -> MaterialTheme.colorScheme.onPrimaryContainer
            },
        ),
    ) {
        Text(text = text)
    }
}

enum class KeyboardKeyEmphasis {
    Normal,
    Action,
}