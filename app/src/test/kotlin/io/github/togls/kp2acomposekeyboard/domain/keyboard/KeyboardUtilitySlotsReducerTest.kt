package io.github.togls.kp2acomposekeyboard.domain.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardUtilitySlotsReducerTest {

    @Test
    fun defaultSlots_pinSettingsInCenterAndLeaveRightEmpty() {
        val slots = KeyboardUtilitySlots()

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun moveToRight_removesItemFromCenter() {
        val slots = KeyboardUtilitySlotsReducer()
            .moveToRight(KeyboardUtilitySlots(), SettingsUtilityItemId)

        assertEquals(emptyList<KeyboardUtilityItemId>(), slots.centerItemIds)
        assertEquals(SettingsUtilityItemId, slots.rightItemId)
    }

    @Test
    fun moveToCenter_removesItemFromRightAndInsertsAtIndex() {
        val reducer = KeyboardUtilitySlotsReducer()

        val slots = reducer.moveToCenter(
            slots = KeyboardUtilitySlots(
                centerItemIds = emptyList(),
                rightItemId = SettingsUtilityItemId,
            ),
            itemId = SettingsUtilityItemId,
            targetIndex = 0,
        )

        assertEquals(listOf(SettingsUtilityItemId), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    @Test
    fun reducerForTest_reordersExistingCenterItem() {
        val first = SettingsUtilityItemId
        val second = FakeUtilityItemId("second")
        val third = FakeUtilityItemId("third")
        val reducer = KeyboardUtilitySlotsReducer(
            allowedItemIds = listOf(first, second, third),
        )

        val slots = reducer.moveToCenter(
            slots = KeyboardUtilitySlots(centerItemIds = listOf(first, second, third)),
            itemId = first,
            targetIndex = 2,
        )

        assertEquals(listOf(second, third, first), slots.centerItemIds)
    }

    @Test
    fun reducerForTest_rejectsSixthCenterItem() {
        val items = (1..6).map { index -> FakeUtilityItemId("item-$index") }
        val reducer = KeyboardUtilitySlotsReducer(allowedItemIds = items)
        val fullSlots = KeyboardUtilitySlots(centerItemIds = items.take(5))

        val slots = reducer.moveToCenter(
            slots = fullSlots,
            itemId = items[5],
            targetIndex = 5,
        )

        assertEquals(items.take(5), slots.centerItemIds)
    }

    @Test
    fun reducerForTest_moveToRightReplacesExistingRightItemWithoutIncreasingPinnedCount() {
        val centerItems = (1..4).map { index -> FakeUtilityItemId("center-$index") }
        val oldRight = FakeUtilityItemId("old-right")
        val newRight = FakeUtilityItemId("new-right")
        val reducer = KeyboardUtilitySlotsReducer(
            allowedItemIds = centerItems + oldRight + newRight,
        )

        val slots = reducer.moveToRight(
            slots = KeyboardUtilitySlots(
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
        val known = SettingsUtilityItemId
        val unknown = FakeUtilityItemId("unknown")

        val slots = KeyboardUtilitySlotsReducer().sanitize(
            KeyboardUtilitySlots(
                centerItemIds = listOf(known, known, unknown),
                rightItemId = known,
            ),
        )

        assertEquals(listOf(known), slots.centerItemIds)
        assertNull(slots.rightItemId)
    }

    private data class FakeUtilityItemId(
        override val storageValue: String,
    ) : KeyboardUtilityItemId
}
