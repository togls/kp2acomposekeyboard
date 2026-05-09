package io.github.togls.kp2acomposekeyboard.ui.keyboard.tokens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost

@Composable
internal fun KeyboardBottomSafeSpacer(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier.height(keyboardBottomSafePadding()),
    )
}

@Composable
private fun keyboardBottomSafePadding(): Dp {
    val density = LocalDensity.current
    val adaptiveMetrics = LocalKeyboardAdaptiveMetrics.current
    val navigationBottomPadding = with(density) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    if (navigationBottomPadding <= adaptiveMetrics.bottomSafePadding) {
        return adaptiveMetrics.bottomSafePadding
    }

    // IME 窗口在不同系统上可能已经避开导航栏；限制最大值避免双重 padding。
    return navigationBottomPadding.coerceAtMost(
        adaptiveMetrics.maxNavigationAwareBottomPadding,
    )
}