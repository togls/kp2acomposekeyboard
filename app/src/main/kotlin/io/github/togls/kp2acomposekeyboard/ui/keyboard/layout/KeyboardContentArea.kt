package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.feature.keyboard.MainKeyboardLayout
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityPanel
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.UtilityRow
import io.github.togls.kp2acomposekeyboard.ui.keyboard.utility.rememberUtilityDragState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.LocalKeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.isKeyboardLandscape

/**
 * Hosts the main keyboard content and resolves metrics that depend on the
 * available content height.
 */
@Composable
internal fun KeyboardContentArea(
    state: KeyboardUiState,
    adaptiveMetrics: KeyboardAdaptiveMetrics,
    isLandscape: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        val contentMetrics = adaptiveMetrics.copy(
            keyHeight = when (state.mainLayout) {
                MainKeyboardLayout.Default -> {
                    maxHeight.resolveDefaultKeyHeight(
                        state = state,
                        isLandscape = isLandscape,
                    )
                }

                MainKeyboardLayout.Entry -> {
                    adaptiveMetrics.keyHeight
                }
            },
        )

        CompositionLocalProvider(
            LocalKeyboardAdaptiveMetrics provides contentMetrics,
        ) {
            val utilityDragState = rememberUtilityDragState()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(KeyboardMetrics.RowSpacing),
            ) {
                UtilityRow(
                    state = state,
                    dragState = utilityDragState,
                    onIntent = onIntent,
                )

                if (state.isUtilityPanelExpanded) {
                    UtilityPanel(
                        dragState = utilityDragState,
                        onIntent = onIntent,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    when (state.mainLayout) {
                        MainKeyboardLayout.Default -> {
                            DefaultKeyboardLayout(
                                state = state,
                                onIntent = onIntent,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        MainKeyboardLayout.Entry -> {
                            EntryKeyboardLayout(
                                state = state,
                                onIntent = onIntent,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resolves the default keyboard key height from the actual available content
 * height, so extra rows such as the active-session hint can fit safely.
 */
private fun Dp.resolveDefaultKeyHeight(
    state: KeyboardUiState,
    isLandscape: Boolean,
): Dp {
    val rowCount = state.defaultVisualRowCount(isLandscape)
    val spacingCount = (rowCount - 1).coerceAtLeast(0)

    val availableHeight = this -
            KeyboardMetrics.OuterPaddingVertical * 2 -
            KeyboardMetrics.RowSpacing * spacingCount

    return maxOf(
        availableHeight / rowCount.toFloat(),
        MinResolvedKeyHeight,
    )
}

/**
 * Returns the number of visible rows used by the default keyboard layout.
 */
private fun KeyboardUiState.defaultVisualRowCount(
    isLandscape: Boolean,
): Int {
    var rowCount = DefaultInputRowCount + DefaultActionRowCount

    if (!isLandscape) {
        rowCount += DefaultUtilityRowCount
    }

    return rowCount
}

private const val DefaultInputRowCount = 3
private const val DefaultActionRowCount = 1
private const val DefaultUtilityRowCount = 1

private val MinResolvedKeyHeight = 34.dp
