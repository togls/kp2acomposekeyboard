package io.github.togls.kp2acomposekeyboard.domain.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardQuickActionSlotsReducerTest {

    @Test
    fun defaultSlots_pinSettingsInCenterAndLeaveRightEmpty() {
        val slots = KeyboardQuickActionSlots()

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun moveToRight_removesItemFromCenter() {
        val slots = KeyboardQuickActionSlotsReducer()
            .moveToRight(KeyboardQuickActionSlots(), SettingsQuickActionId)

        assertEquals(emptyList<KeyboardQuickActionId>(), slots.centerItemIds)
        assertEquals(SettingsQuickActionId, slots.rightItemId)
    }

    @Test
    fun moveToCenter_removesItemFromRightAndInsertsAtIndex() {
        val reducer = KeyboardQuickActionSlotsReducer()

        val slots = reducer.moveToCenter(
            slots = KeyboardQuickActionSlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsQuickActionId,
            ),
            itemId = SettingsQuickActionId,
            targetIndex = 0,
        )

        assertEquals(listOf(SettingsQuickActionId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun reducerForTest_reordersExistingCenterItem() {
        val first = SettingsQuickActionId
        val second = FakeQuickActionId("second")
        val third = FakeQuickActionId("third")
        val reducer = KeyboardQuickActionSlotsReducer(
            allowedItemIds = listOf(first, second, third),
        )

        val slots = reducer.moveToCenter(
            slots = KeyboardQuickActionSlots(centerItemIds = listOf(first, second, third)),
            itemId = first,
            targetIndex = 2,
        )

        assertEquals(listOf(second, third, first), slots.centerItemIds)
    }

    @Test
    fun reducerForTest_rejectsSixthCenterItem() {
        val items = (1..6).map { index -> FakeQuickActionId("item-$index") }
        val reducer = KeyboardQuickActionSlotsReducer(allowedItemIds = items)
        val fullSlots = KeyboardQuickActionSlots(centerItemIds = items.take(5))

        val slots = reducer.moveToCenter(
            slots = fullSlots,
            itemId = items[5],
            targetIndex = 5,
        )

        assertEquals(items.take(5), slots.centerItemIds)
    }

    @Test
    fun reducerForTest_moveToRightReplacesExistingRightItemWithoutIncreasingPinnedCount() {
        val centerItems = (1..4).map { index -> FakeQuickActionId("center-$index") }
        val oldRight = FakeQuickActionId("old-right")
        val newRight = FakeQuickActionId("new-right")
        val reducer = KeyboardQuickActionSlotsReducer(
            allowedItemIds = centerItems + oldRight + newRight,
        )

        val slots = reducer.moveToRight(
            slots = KeyboardQuickActionSlots(
                centerItemIds = centerItems,
                rightItemId = oldRight,
            ),
            itemId = newRight,
        )

        assertEquals(centerItems, slots.centerItemIds)
        assertEquals(newRight, slots.rightItemId)
        assertEquals(5, slots.pinnedCount)
    }

    @Test
    fun reducerForTest_sanitizedRemovesDuplicatesAndUnknownIds() {
        val known = SettingsQuickActionId
        val unknown = FakeQuickActionId("unknown")

        val slots = KeyboardQuickActionSlotsReducer().sanitize(
            KeyboardQuickActionSlots(
                centerItemIds = listOf(known, known, unknown),
                rightItemId = known,
            ),
        )

        assertEquals(listOf(known), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    private data class FakeQuickActionId(
        override val storageValue: String,
    ) : KeyboardQuickActionId
}
