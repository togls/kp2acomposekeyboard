package io.github.togls.kp2acomposekeyboard.ui.keyboard.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class KeyboardLayoutMetricsTest {
    @Test
    fun `standard key width uses ten key reference row`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 106.dp,
                totalHeight = 300.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 0.dp,
                verticalOuterPadding = 0.dp,
                keySpacing = 2.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(8.8f, metrics.standardKeyWidth.value, 0.001f)
    }

    @Test
    fun `row height subtracts candidate paddings spacers and three row gaps`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 10.dp,
                keySpacing = 6.dp,
                rowSpacing = 5.dp,
                bottomSpacerHeight = 20.dp,
                navigationSpacerHeight = 25.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(35f, metrics.keyboardRowHeight.value, 0.001f)
        assertEquals(75f, metrics.remainingFieldsAreaHeight.value, 0.001f)
    }

    @Test
    fun `bottom and navigation spacers may be zero`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 220.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 10.dp,
                keySpacing = 6.dp,
                rowSpacing = 5.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(36.25f, metrics.keyboardRowHeight.value, 0.001f)
    }

    @Test
    fun `field width supports three and four columns`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 320.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 10.dp,
                verticalOuterPadding = 8.dp,
                keySpacing = 5.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(96.666f, metrics.fieldKeyWidth(3).value, 0.001f)
        assertEquals(71.25f, metrics.fieldKeyWidth(4).value, 0.001f)
    }

    @Test
    fun `fixed widths snap down to device pixels when density is provided`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 360.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 8.dp,
                keySpacing = 6.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
                pixelSnapDensity = 3.5f,
            ),
        )

        assertEquals(28.857f, metrics.standardKeyWidth.value, 0.001f)
    }

    @Test
    fun `small height and large spacing do not produce negative dimensions`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 10.dp,
                totalHeight = 10.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 20.dp,
                verticalOuterPadding = 20.dp,
                keySpacing = 6.dp,
                rowSpacing = 40.dp,
                bottomSpacerHeight = 20.dp,
                navigationSpacerHeight = 20.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertEquals(0f, metrics.standardKeyWidth.value, 0.001f)
        assertEquals(0f, metrics.sideKeyWidth.value, 0.001f)
        assertEquals(0f, metrics.keyboardRowHeight.value, 0.001f)
        assertEquals(40f, metrics.remainingFieldsAreaHeight.value, 0.001f)
        assertEquals(0f, metrics.fieldKeyWidth(3).value, 0.001f)
    }

    @Test
    fun `field width rejects invalid columns`() {
        val metrics = calculateKeyboardLayoutMetrics(
            KeyboardLayoutInput(
                totalWidth = 300.dp,
                totalHeight = 260.dp,
                candidateRowHeight = 40.dp,
                horizontalPadding = 8.dp,
                verticalOuterPadding = 8.dp,
                keySpacing = 6.dp,
                rowSpacing = 4.dp,
                bottomSpacerHeight = 0.dp,
                navigationSpacerHeight = 0.dp,
                sideKeyStandardKeyCount = 7,
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            metrics.fieldKeyWidth(0)
        }
    }
}
