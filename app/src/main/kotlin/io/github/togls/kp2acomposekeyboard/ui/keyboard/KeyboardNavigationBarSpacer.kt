package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Reserves the bottom navigation bar area for the IME.
 *
 * The keyboard background still comes from the parent Surface, while the
 * interactive key area stays above the system navigation bar.
 */
@Composable
internal fun KeyboardNavigationBarSpacer(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    )
}