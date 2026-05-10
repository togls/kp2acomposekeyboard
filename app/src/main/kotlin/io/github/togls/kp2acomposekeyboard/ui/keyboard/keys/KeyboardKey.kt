package io.github.togls.kp2acomposekeyboard.ui.keyboard.keys

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens.LocalKeyboardAdaptiveMetrics

@Composable
internal fun KeyboardKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    emphasis: KeyboardKeyEmphasis = KeyboardKeyEmphasis.Normal,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val keyColors = KeyboardKeyColors.from(emphasis)

    val containerColor by animateColorAsState(
        targetValue = if (pressed && enabled) {
            keyColors.pressedContainerColor
        } else {
            keyColors.containerColor
        },
        label = "keyboard-key-container-color",
    )

    val contentColor by animateColorAsState(
        targetValue = keyColors.onContainerColor,
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
            .height(adaptiveMetrics.keyHeight)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
            .graphicsLayer {
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
        shape = RoundedCornerShape(adaptiveMetrics.keyCornerRadius),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        shadowElevation = elevation,
    ) {
        Text(
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = adaptiveMetrics.keyHorizontalPadding,
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

private const val DisabledKeyAlpha = 0.38f