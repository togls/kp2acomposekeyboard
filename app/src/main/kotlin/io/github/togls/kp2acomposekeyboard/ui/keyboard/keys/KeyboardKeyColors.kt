package io.github.togls.kp2acomposekeyboard.ui.keyboard.keys

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal data class KeyboardKeyColors(
    val containerColor: Color,
    val pressedContainerColor: Color,
    val contentColor: Color,
) {
    companion object {
        @Composable
        internal fun from(
            emphasis: KeyboardKeyEmphasis,
        ): KeyboardKeyColors {
            val colorScheme = MaterialTheme.colorScheme

            return when (emphasis) {
                KeyboardKeyEmphasis.Normal -> KeyboardKeyColors(
                    containerColor = colorScheme.surfaceContainerHighest,
                    pressedContainerColor = colorScheme.surfaceContainerHigh,
                    contentColor = colorScheme.onSurface,
                )

                KeyboardKeyEmphasis.Action -> KeyboardKeyColors(
                    contentColor = colorScheme.primaryContainer,
                    pressedContainerColor = colorScheme.primary.copy(alpha = 0.24f),
                    containerColor = colorScheme.onPrimaryContainer,
                )

                KeyboardKeyEmphasis.Sensitive -> KeyboardKeyColors(
                    containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                    pressedContainerColor = colorScheme.tertiary.copy(alpha = 0.22f),
                    contentColor = colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}
