package io.github.togls.kp2acomposekeyboard.ui.keyboard.entry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryFieldPagingTest {
    @Test
    fun `previous and next targets clamp to bounds`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 150f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(50f, state.previousTargetPx(), 0.001f)
        assertEquals(250f, state.nextTargetPx(), 0.001f)
    }

    @Test
    fun `controls disable when content fits one page`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 0f,
            maxScrollOffsetPx = 0f,
            visibleFieldListAreaHeightPx = 200f,
            contentHeightPx = 180f,
        )

        assertFalse(state.previousEnabled)
        assertFalse(state.nextEnabled)
        assertEquals(0f, state.snapTargetPx(), 0.001f)
    }

    @Test
    fun `snap target uses nearest page and clamps`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 151f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(200f, state.snapTargetPx(), 0.001f)
    }

    @Test
    fun `zero visible height disables page math`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 100f,
            maxScrollOffsetPx = 200f,
            visibleFieldListAreaHeightPx = 0f,
            contentHeightPx = 300f,
        )

        assertFalse(state.previousEnabled)
        assertFalse(state.nextEnabled)
        assertEquals(0f, state.previousTargetPx(), 0.001f)
        assertEquals(0f, state.nextTargetPx(), 0.001f)
        assertEquals(0f, state.snapTargetPx(), 0.001f)
    }

    @Test
    fun `controls enable only in available directions`() {
        val top = EntryFieldPageState(
            currentOffsetPx = 0f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )
        val bottom = top.copy(currentOffsetPx = 260f)

        assertFalse(top.previousEnabled)
        assertTrue(top.nextEnabled)
        assertTrue(bottom.previousEnabled)
        assertFalse(bottom.nextEnabled)
    }

    @Test
    fun `snap target clamps at bottom boundary`() {
        val state = EntryFieldPageState(
            currentOffsetPx = 255f,
            maxScrollOffsetPx = 260f,
            visibleFieldListAreaHeightPx = 100f,
            contentHeightPx = 360f,
        )

        assertEquals(260f, state.snapTargetPx(), 0.001f)
    }
}
