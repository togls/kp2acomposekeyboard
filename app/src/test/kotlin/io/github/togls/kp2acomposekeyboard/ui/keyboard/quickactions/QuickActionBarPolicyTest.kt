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

class QuickActionBarPolicyTest {

    @Test
    fun shouldShowRightSlot_hidesEmptySlotWhenPanelIsClosed() {
        assertFalse(
            shouldShowRightQuickActionSlot(
                rightItemId = null,
                isQuickActionPanelExpanded = false,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsEmptySlotWhenPanelIsExpanded() {
        assertTrue(
            shouldShowRightQuickActionSlot(
                rightItemId = null,
                isQuickActionPanelExpanded = true,
            ),
        )
    }

    @Test
    fun shouldShowRightSlot_showsPinnedRightItemWhenPanelIsClosed() {
        assertTrue(
            shouldShowRightQuickActionSlot(
                rightItemId = SettingsQuickActionId,
                isQuickActionPanelExpanded = false,
            ),
        )
    }

    @Test
    fun resolveDragPreviewOffset_centersPreviewOnPointerInsideContainer() {
        val offset = resolveQuickActionDragPreviewOffset(
            positionInRoot = Offset(150f, 80f),
            containerBoundsInRoot = Rect(100f, 50f, 400f, 250f),
            previewSizePx = 40f,
        )

        assertEquals(IntOffset(30, 10), offset)
    }

    @Test
    fun shouldShowPanelQuickAction_hidesPinnedCenterItem() {
        assertFalse(
            shouldShowPanelQuickAction(
                itemId = SettingsQuickActionId,
                quickActionSlots = KeyboardQuickActionSlots(
                    centerItemIds = listOf(SettingsQuickActionId),
                ),
            ),
        )
    }

    @Test
    fun shouldShowPanelQuickAction_hidesPinnedRightItem() {
        assertFalse(
            shouldShowPanelQuickAction(
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
            shouldShowDraggedSourceAction(
                itemId = SettingsQuickActionId,
                source = QuickActionDragSource.Panel,
                draggedItemId = SettingsQuickActionId,
                dragSource = QuickActionDragSource.Panel,
            ),
        )
        assertTrue(
            shouldShowDraggedSourceAction(
                itemId = SettingsQuickActionId,
                source = QuickActionDragSource.Pinned,
                draggedItemId = SettingsQuickActionId,
                dragSource = QuickActionDragSource.Panel,
            ),
        )
    }
}
