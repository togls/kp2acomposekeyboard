package io.github.togls.kp2acomposekeyboard.ui.keyboard.utility

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsUtilityItemId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityRowPolicyTest {

    @Test
    fun shouldShowRightSlot_hidesEmptySlotWhenPanelIsClosed() {
        assertFalse(
            shouldShowRightUtilitySlot(
                rightItemId = null,
                isUtilityPanelExpanded = false,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsEmptySlotWhenPanelIsExpanded() {
        assertTrue(
            shouldShowRightUtilitySlot(
                rightItemId = null,
                isUtilityPanelExpanded = true,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsPinnedRightItemWhenPanelIsClosed() {
        assertTrue(
            shouldShowRightUtilitySlot(
                rightItemId = SettingsUtilityItemId,
                isUtilityPanelExpanded = false,
            ),
        )
    }

    @Test
    fun resolveDragPreviewOffset_centersPreviewOnPointerInsideContainer() {
        val offset = resolveUtilityDragPreviewOffset(
            positionInRoot = Offset(150f, 80f),
            containerBoundsInRoot = Rect(100f, 50f, 400f, 250f),
            previewSizePx = 40f,
        )

        assertEquals(IntOffset(30, 10), offset)
    }

    @Test
    fun shouldShowPanelUtilityItem_hidesPinnedCenterItem() {
        assertFalse(
            shouldShowPanelUtilityItem(
                itemId = SettingsUtilityItemId,
                utilitySlots = KeyboardUtilitySlots(
                    centerItemIds = listOf(SettingsUtilityItemId),
                ),
            ),
        )
    }

    @Test
    fun shouldShowPanelUtilityItem_hidesPinnedRightItem() {
        assertFalse(
            shouldShowPanelUtilityItem(
                itemId = SettingsUtilityItemId,
                utilitySlots = KeyboardUtilitySlots(
                    centerItemIds = emptyList(),
                    rightItemId = SettingsUtilityItemId,
                ),
            ),
        )
    }

    @Test
    fun shouldShowDraggedSourceItem_hidesOnlyMatchingDraggedSource() {
        assertFalse(
            shouldShowDraggedSourceItem(
                itemId = SettingsUtilityItemId,
                source = UtilityDragSource.Panel,
                draggedItemId = SettingsUtilityItemId,
                dragSource = UtilityDragSource.Panel,
            ),
        )
        assertTrue(
            shouldShowDraggedSourceItem(
                itemId = SettingsUtilityItemId,
                source = UtilityDragSource.Pinned,
                draggedItemId = SettingsUtilityItemId,
                dragSource = UtilityDragSource.Panel,
            ),
        )
    }
}
