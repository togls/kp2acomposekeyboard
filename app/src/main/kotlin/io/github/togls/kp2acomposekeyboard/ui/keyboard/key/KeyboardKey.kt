package io.github.togls.kp2acomposekeyboard.ui.keyboard.key

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyColors
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardKeyEmphasis
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

/**
 * Displays a single text keyboard key with Material 3 colors, shape, elevation,
 * and press feedback.
 *
 * Text keys are still used by letter, number, and symbol layouts.
 */
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

    KeyboardKeySurface(
        modifier = modifier,
        enabled = enabled,
        contentDescription = contentDescription,
        emphasis = emphasis,
        onClick = onClick,
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
            fontWeight = keyFontWeight(emphasis),
        )
    }
}

/**
 * Displays a single icon keyboard key with the same visual behavior as text keys.
 *
 * The outer key owns the accessibility description, so the inner icon keeps its
 * contentDescription null to avoid duplicate TalkBack output.
 */
@Composable
internal fun KeyboardIconKey(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    emphasis: KeyboardKeyEmphasis = KeyboardKeyEmphasis.Normal,
    iconSize: Dp = KeyboardActionIconSize,
) {
    KeyboardKeySurface(
        modifier = modifier,
        enabled = enabled,
        contentDescription = contentDescription,
        emphasis = emphasis,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = painterResource(id = iconRes),
            contentDescription = null,
        )
    }
}

/**
 * Shared key container for both text and icon keys.
 *
 * Keep visual state handling here so all keyboard keys use the same press,
 * disabled, ripple, color, shape, and elevation behavior.
 */
@Composable
private fun KeyboardKeySurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    emphasis: KeyboardKeyEmphasis = KeyboardKeyEmphasis.Normal,
    content: @Composable () -> Unit,
) {
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current

    // Keep one interaction source so press state, ripple, and animations stay in sync.
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
        label = "keyboard-key-scale",
    )

    val shadowElevation by animateDpAsState(
        targetValue = when {
            !enabled -> 0.dp
            pressed -> 0.dp
            else -> 1.dp
        },
        label = "keyboard-key-shadow-elevation",
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
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = contentColor,
        // Keep the actual key color clean. Tonal elevation changes the visual color.
        tonalElevation = 0.dp,
        // Use only a very small physical shadow for the Gboard-like floating key effect.
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/**
 * Returns the text weight used by text keys for each visual emphasis.
 */
private fun keyFontWeight(emphasis: KeyboardKeyEmphasis): FontWeight {
    return when (emphasis) {
        KeyboardKeyEmphasis.Normal -> FontWeight.Normal
        KeyboardKeyEmphasis.Action -> FontWeight.SemiBold
        KeyboardKeyEmphasis.Sensitive -> FontWeight.Medium
    }
}

/**
 * Matches Material disabled content opacity.
 */
private const val DisabledKeyAlpha = 0.38f

private val KeyboardActionIconSize = 24.dp
