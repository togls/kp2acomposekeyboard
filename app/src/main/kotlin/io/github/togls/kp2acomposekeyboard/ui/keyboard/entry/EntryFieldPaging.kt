package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import kotlin.math.round

internal data class EntryFieldPageState(
    val currentOffsetPx: Float,
    val maxScrollOffsetPx: Float,
    val visibleFieldListAreaHeightPx: Float,
    val contentHeightPx: Float,
) {
    val previousEnabled: Boolean
        get() = canPage && clampedOffsetPx > 0f

    val nextEnabled: Boolean
        get() = canPage && clampedOffsetPx < maxOffsetPx

    fun previousTargetPx(): Float {
        if (!canPage) return 0f
        return (clampedOffsetPx - visibleFieldListAreaHeightPx).coerceAtLeast(0f)
    }

    fun nextTargetPx(): Float {
        if (!canPage) return 0f
        return (clampedOffsetPx + visibleFieldListAreaHeightPx).coerceAtMost(maxOffsetPx)
    }

    fun snapTargetPx(): Float {
        if (!canPage) return 0f
        val targetPage = round(clampedOffsetPx / visibleFieldListAreaHeightPx)
        return (targetPage * visibleFieldListAreaHeightPx).coerceIn(0f, maxOffsetPx)
    }

    private val clampedOffsetPx: Float
        get() = currentOffsetPx.coerceIn(0f, maxOffsetPx)

    private val maxOffsetPx: Float
        get() = maxScrollOffsetPx.coerceAtLeast(0f)

    private val canPage: Boolean
        get() = visibleFieldListAreaHeightPx > 0f &&
                contentHeightPx > visibleFieldListAreaHeightPx &&
                maxOffsetPx > 0f
}
