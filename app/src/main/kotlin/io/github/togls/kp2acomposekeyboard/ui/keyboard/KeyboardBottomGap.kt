package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Adds a small visual gap between the key area and the system navigation bar.
 *
 * This is intentionally separate from [KeyboardNavigationBarSpacer], because
 * the navigation bar spacer should only represent the real system inset.
 */
@Composable
internal fun KeyboardBottomGap(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val height = if (isLandscape) {
        0.dp
    } else {
        32.dp
    }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    )
}