package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.R
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics

@Composable
internal fun UtilityItemSlot(
    itemId: KeyboardUtilityItemId?,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
    emptySlot: Boolean = false,
    iconSize: Dp = 24.dp,
    dragState: UtilityDragState? = null,
    dragSource: UtilityDragSource = UtilityDragSource.Pinned,
    onBoundsChanged: ((KeyboardUtilityItemId, Rect) -> Unit)? = null,
    onDrop: ((
        itemId: KeyboardUtilityItemId,
        source: UtilityDragSource,
        target: UtilityDropTarget?,
    ) -> Unit)? = null,
) {
    val item = itemId?.toKeyboardUtilityItem()

    when {
        item != null -> {
            var itemBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
            // Keep the source slot composed during drag so pointerInput can
            // finish the gesture, but hide it visually to avoid duplicate icons.
            val sourceItemAlpha = if (
                shouldShowDraggedSourceItem(
                    itemId = item.id,
                    source = dragSource,
                    draggedItemId = dragState?.draggedItemId,
                    dragSource = dragState?.dragSource,
                )
            ) {
                1f
            } else {
                HiddenDraggedSourceAlpha
            }
            val draggableModifier = if (dragState != null && onDrop != null) {
                Modifier
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        itemBoundsInRoot = bounds
                        onBoundsChanged?.invoke(item.id, bounds)
                    }
                    .pointerInput(item.id, dragSource) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localPosition ->
                                val sourceBounds = itemBoundsInRoot
                                    ?: return@detectDragGesturesAfterLongPress
                                dragState.startDrag(
                                    itemId = item.id,
                                    source = dragSource,
                                    sourceBounds = sourceBounds,
                                    localPointerPosition = localPosition,
                                )
                            },
                            onDrag = { change, _ ->
                                dragState.updateDrag(change.position)
                                change.consume()
                            },
                            onDragEnd = {
                                onDrop(
                                    item.id,
                                    dragState.dragSource ?: dragSource,
                                    dragState.hoveredDropTarget,
                                )
                                dragState.endDrag()
                            },
                            onDragCancel = {
                                dragState.endDrag()
                            },
                        )
                    }
            } else {
                Modifier.onGloballyPositioned { coordinates ->
                    onBoundsChanged?.invoke(item.id, coordinates.boundsInRoot())
                }
            }

            UtilityIconButton(
                modifier = modifier
                    .alpha(sourceItemAlpha)
                    .then(draggableModifier),
                iconRes = item.iconRes,
                contentDescription = stringResource(item.contentDescriptionRes),
                onClick = { onIntent(KeyboardIntent.ClickUtilityItem(item.id)) },
            )
        }

        emptySlot -> {
            EmptyUtilitySlot(
                modifier = modifier,
                iconSize = iconSize,
            )
        }
    }
}

@Composable
internal fun UtilityIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(LocalKeyboardAdaptiveMetrics.current.keyCornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyUtilitySlot(
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
) {
    Surface(
        modifier = modifier.alpha(EmptySlotAlpha),
        shape = RoundedCornerShape(LocalKeyboardAdaptiveMetrics.current.keyCornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = R.drawable.ic_apps_24),
                contentDescription = stringResource(R.string.cd_key_empty_utility_slot),
            )
        }
    }
}

private const val EmptySlotAlpha = 0.45f
private const val HiddenDraggedSourceAlpha = 0f
