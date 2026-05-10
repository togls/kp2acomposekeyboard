package io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keys.style.KeyboardKeyEmphasis

/**
 * Defines the color set used by a keyboard key in different visual states.
 *
 * @property containerColor The default background color of the key.
 * @property onContainerColor The color used for the key content, such as text or icons.
 * @property pressedContainerColor The background color used while the key is pressed.
 */
internal data class KeyboardKeyColors(
    val containerColor: Color,
    val onContainerColor: Color,
    val pressedContainerColor: Color,
) {
    companion object {

        /**
         * Creates key colors based on the given visual emphasis.
         *
         * This keeps color decisions centralized so that all keyboard keys
         * follow the same Material theme rules.
         */
        @Composable
        internal fun from(
            emphasis: KeyboardKeyEmphasis,
        ): KeyboardKeyColors {
            val colorScheme = MaterialTheme.colorScheme

            return when (emphasis) {
                // Standard key style used for normal text input keys.
                KeyboardKeyEmphasis.Normal -> KeyboardKeyColors(
                    containerColor = colorScheme.surfaceBright,
                    onContainerColor = colorScheme.onSurface,
                    pressedContainerColor = colorScheme.surfaceContainerHigh,
                )

                // Prominent style used for action keys, such as delete or enter.
                KeyboardKeyEmphasis.Action -> KeyboardKeyColors(
                    containerColor = colorScheme.secondaryContainer,
                    onContainerColor = colorScheme.onSecondaryContainer,
                    pressedContainerColor = lerp(
                        colorScheme.primaryContainer,
                        colorScheme.primary,
                        0.18f,
                    ),
                )

                // Distinct style used for sensitive keys, such as password-related fields.
                KeyboardKeyEmphasis.Sensitive -> KeyboardKeyColors(
                    containerColor = colorScheme.tertiaryContainer,
                    onContainerColor = colorScheme.onTertiaryContainer,
                    pressedContainerColor = lerp(
                        colorScheme.tertiaryContainer,
                        colorScheme.tertiary,
                        0.18f,
                    ),
                )
            }
        }
    }
}