package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilitySlotModelsTest {

    @Test
    fun resolveDropTarget_returnsRightWhenPointerInsideRightSlot() {
        val target = resolveUtilityDropTarget(
            positionInRoot = Offset(310f, 20f),
            centerItemBounds = emptyList(),
            centerContainerBounds = Rect(50f, 0f, 250f, 50f),
            rightSlotBounds = Rect(300f, 0f, 350f, 50f),
        )

        assertEquals(UtilityDropTarget.Right, target)
    }

    @Test
    fun resolveDropTarget_returnsCenterIndexByItemHalf() {
        val bounds = listOf(
            Rect(50f, 0f, 100f, 50f),
            Rect(110f, 0f, 160f, 50f),
        )

        assertEquals(
            UtilityDropTarget.Center(targetIndex = 0),
            resolveUtilityDropTarget(
                positionInRoot = Offset(60f, 20f),
                centerItemBounds = bounds,
                centerContainerBounds = Rect(50f, 0f, 160f, 50f),
                rightSlotBounds = null,
            ),
        )
        assertEquals(
            UtilityDropTarget.Center(targetIndex = 1),
            resolveUtilityDropTarget(
                positionInRoot = Offset(90f, 20f),
                centerItemBounds = bounds,
                centerContainerBounds = Rect(50f, 0f, 160f, 50f),
                rightSlotBounds = null,
            ),
        )
        assertEquals(
            UtilityDropTarget.Center(targetIndex = 2),
            resolveUtilityDropTarget(
                positionInRoot = Offset(150f, 20f),
                centerItemBounds = bounds,
                centerContainerBounds = Rect(50f, 0f, 160f, 50f),
                rightSlotBounds = null,
            ),
        )
    }

    @Test
    fun resolveDropTarget_returnsOutsideWhenPointerMissesTargets() {
        val target = resolveUtilityDropTarget(
            positionInRoot = Offset(10f, 200f),
            centerItemBounds = emptyList(),
            centerContainerBounds = Rect(50f, 0f, 160f, 50f),
            rightSlotBounds = Rect(300f, 0f, 350f, 50f),
        )

        assertEquals(UtilityDropTarget.Outside, target)
    }
}
