package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: KeyboardKeyEmphasis = KeyboardKeyEmphasis.Normal,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val keyColors = keyboardKeyColors(emphasis)

    val containerColor by animateColorAsState(
        targetValue = if (pressed && enabled) {
            keyColors.pressedContainerColor
        } else {
            keyColors.containerColor
        },
        label = "keyboard-key-container-color",
    )

    val contentColor by animateColorAsState(
        targetValue = keyColors.contentColor,
        label = "keyboard-key-content-color",
    )

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) {
            KeyboardMetrics.PressedScale
        } else {
            KeyboardMetrics.NormalScale
        },
        label = "keyboard-key-scale"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            !enabled -> 0.dp
            pressed -> KeyboardMetrics.PressedElevation
            emphasis == KeyboardKeyEmphasis.Action -> KeyboardMetrics.ActionElevation
            else -> KeyboardMetrics.NormalElevation
        },
        label = "keyboard-key-elevation",
    )

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = KeyboardMetrics.KeyMinHeight)
            .graphicsLayer{
                scaleX = scale
                scaleY = scale
            }
            .alpha(if (enabled) 1f else DisabledKeyAlpha)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(KeyboardMetrics.KeyCornerRadius),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        shadowElevation = elevation,
    ) {
        Text(
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = KeyboardMetrics.KeyHorizontalPadding,
                    vertical = KeyboardMetrics.KeyVerticalPadding,
                ),
            ),
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = when (emphasis) {
                KeyboardKeyEmphasis.Normal -> FontWeight.Normal
                KeyboardKeyEmphasis.Action -> FontWeight.SemiBold
                KeyboardKeyEmphasis.Sensitive -> FontWeight.Medium
            }
        )
    }
}

@Composable
private fun keyboardKeyColors(
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

private data class KeyboardKeyColors(
    val containerColor: androidx.compose.ui.graphics.Color,
    val pressedContainerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
)

enum class KeyboardKeyEmphasis {
    Normal,
    Action,
    Sensitive,
}

private const val DisabledKeyAlpha = 0.38f