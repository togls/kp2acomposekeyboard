package io.github.togls.kp2acomposekeyboard.ui.keyboard.quickactions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.SettingsQuickActionId
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
                isQuickActionPanelExpanded = false,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsEmptySlotWhenPanelIsExpanded() {
        assertTrue(
            shouldShowRightUtilitySlot(
                rightItemId = null,
                isQuickActionPanelExpanded = true,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsPinnedRightItemWhenPanelIsClosed() {
        assertTrue(
            shouldShowRightUtilitySlot(
                rightItemId = SettingsQuickActionId,
                isQuickActionPanelExpanded = false,
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
                itemId = SettingsQuickActionId,
                quickActionSlots = KeyboardQuickActionSlots(
                    centerItemIds = listOf(SettingsQuickActionId),
                ),
            ),
        )
    }

    @Test
    fun shouldShowPanelUtilityItem_hidesPinnedRightItem() {
        assertFalse(
            shouldShowPanelUtilityItem(
                itemId = SettingsQuickActionId,
                quickActionSlots = KeyboardQuickActionSlots(
                    centerItemIds = emptyList(),
                    rightItemId = SettingsQuickActionId,
                ),
            ),
        )
    }

    @Test
    fun shouldShowDraggedSourceItem_hidesOnlyMatchingDraggedSource() {
        assertFalse(
            shouldShowDraggedSourceItem(
                itemId = SettingsQuickActionId,
                source = UtilityDragSource.Panel,
                draggedItemId = SettingsQuickActionId,
                dragSource = UtilityDragSource.Panel,
            ),
        )
        assertTrue(
            shouldShowDraggedSourceItem(
                itemId = SettingsQuickActionId,
                source = UtilityDragSource.Pinned,
                draggedItemId = SettingsQuickActionId,
                dragSource = UtilityDragSource.Panel,
            ),
        )
    }
}
