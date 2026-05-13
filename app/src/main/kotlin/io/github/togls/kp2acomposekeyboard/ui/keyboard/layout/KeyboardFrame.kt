package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUiState
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardBottomGap
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardNavigationBarSpacer
import io.github.togls.kp2acomposekeyboard.ui.keyboard.KeyboardTestTags
import io.github.togls.kp2acomposekeyboard.ui.keyboard.keyboardBottomGapHeight
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardAdaptiveMetrics
import io.github.togls.kp2acomposekeyboard.ui.keyboard.style.KeyboardMetrics

@Composable
internal fun KeyboardFrame(
    state: KeyboardUiState,
    adaptiveMetrics: KeyboardAdaptiveMetrics,
    isLandscape: Boolean,
    onIntent: (KeyboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .testTag(KeyboardTestTags.Root),
    ) {
        val density = LocalDensity.current
        val navigationSpacerHeight = with(density) {
            WindowInsets.navigationBars
                .getBottom(this)
                .toDp()
                .coerceAtMost(adaptiveMetrics.maxNavigationAwareBottomPadding)
        }
        val bottomSpacerHeight = keyboardBottomGapHeight(isLandscape)
        val metrics = remember(
            maxWidth,
            maxHeight,
            adaptiveMetrics.keyHeight,
            navigationSpacerHeight,
            bottomSpacerHeight,
            density.density,
        ) {
            calculateKeyboardLayoutMetrics(
                KeyboardLayoutInput(
                    totalWidth = maxWidth,
                    totalHeight = maxHeight,
                    candidateRowHeight = adaptiveMetrics.keyHeight,
                    horizontalPadding = KeyboardMetrics.OuterPaddingHorizontal,
                    verticalOuterPadding = KeyboardMetrics.OuterPaddingVertical,
                    keySpacing = KeyboardMetrics.KeySpacing,
                    rowSpacing = KeyboardMetrics.RowSpacing,
                    bottomSpacerHeight = bottomSpacerHeight,
                    navigationSpacerHeight = navigationSpacerHeight,
                    sideKeyStandardKeyCount = 7,
                    pixelSnapDensity = density.density,
                ),
            )
        }

        CompositionLocalProvider(LocalKeyboardLayoutMetrics provides metrics) {
            Column(modifier = Modifier.fillMaxWidth()) {
                KeyboardContentArea(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )

                KeyboardBottomGap(isLandscape = isLandscape)

                KeyboardNavigationBarSpacer(
                    height = navigationSpacerHeight,
                )
            }
        }
    }
}
