package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a small visual gap between the key area and the system navigation bar.
 *
 * This is intentionally separate from [KeyboardNavigationBarSpacer], because
 * the navigation bar spacer should only represent the real system inset.
 */
@Composable
internal fun KeyboardBottomSpacer(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(keyboardBottomGapHeight(isLandscape)),
    )
}

internal fun keyboardBottomGapHeight(isLandscape: Boolean): Dp = if (isLandscape) {
    0.dp
} else {
    32.dp
}
