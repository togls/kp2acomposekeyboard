package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun UtilityDragPreview(
    dragState: UtilityDragState,
    containerBoundsInRoot: Rect?,
    modifier: Modifier = Modifier,
) {
    val item = dragState.draggedItemId?.toKeyboardUtilityItem() ?: return
    val containerBounds = containerBoundsInRoot ?: return
    val previewSize = LocalKeyboardAdaptiveMetrics.current.keyHeight * DragPreviewScale
    val previewSizePx = with(LocalDensity.current) { previewSize.toPx() }

    // The drag state stores root coordinates, while this overlay is positioned
    // inside KeyboardContentArea; subtract the container origin before drawing.
    Surface(
        modifier = modifier
            .offset {
                resolveUtilityDragPreviewOffset(
                    positionInRoot = dragState.positionInRoot,
                    containerBoundsInRoot = containerBounds,
                    previewSizePx = previewSizePx,
                )
            }
            .size(previewSize)
            .alpha(DragPreviewAlpha),
        shape = RoundedCornerShape(LocalKeyboardAdaptiveMetrics.current.keyCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
            )
        }
    }
}

private const val DragPreviewScale = 1.08f
private const val DragPreviewAlpha = 0.92f
