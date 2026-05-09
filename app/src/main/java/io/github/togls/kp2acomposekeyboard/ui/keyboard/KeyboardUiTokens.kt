package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.unit.dp

internal val Int.dpCompat
    get() = dp

internal object KeyboardMetrics {
    val OuterPaddingHorizontal = 8.dp
    val OuterPaddingVertical = 8.dp

    val RowSpacing = 7.dp
    val KeySpacing = 6.dp

    val KeyMinHeight = 46.dp
    val KeyHorizontalPadding = 8.dp
    val KeyVerticalPadding = 0.dp

    val KeyCornerRadius = 18.dp
    val FieldKeyCornerRadius = 20.dp

    val PressedScale = 0.97f
    val NormalScale = 1f

    val NormalElevation = 1.dp
    val ActionElevation = 2.dp
    val PressedElevation = 0.dp
}