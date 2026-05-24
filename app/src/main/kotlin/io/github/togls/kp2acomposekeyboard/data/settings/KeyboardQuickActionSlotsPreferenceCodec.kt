package io.github.togls.kp2acomposekeyboard.data.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardQuickActionSlotsReducer

object KeyboardQuickActionSlotsPreferenceCodec {

    private val reducer = KeyboardQuickActionSlotsReducer()

    fun encode(slots: KeyboardQuickActionSlots): String {
        val sanitizedSlots = reducer.sanitize(slots)
        val centerValue = sanitizedSlots.centerItemIds.joinToString(separator = ",") { itemId ->
            itemId.storageValue
        }
        val rightValue = sanitizedSlots.rightItemId?.storageValue.orEmpty()

        // Keep the value compact and non-sensitive: only quick-action IDs are stored.
        return "center=$centerValue;right=$rightValue"
    }

    fun decode(rawValue: String?): KeyboardQuickActionSlots {
        if (rawValue.isNullOrBlank()) {
            return KeyboardQuickActionSlots()
        }

        val valuesByKey = rawValue.split(";").mapNotNull { part ->
            val keyValue = part.split("=", limit = 2)
            if (keyValue.size != 2 || keyValue[0].isBlank()) {
                null
            } else {
                keyValue[0] to keyValue[1]
            }
        }.toMap()

        // Unknown fields are ignored for forward compatibility, but the two
        // known fields must exist so a malformed value falls back safely.
        if (!valuesByKey.containsKey(CENTER_KEY) || !valuesByKey.containsKey(RIGHT_KEY)) {
            return KeyboardQuickActionSlots()
        }

        val centerValue = valuesByKey.getValue(CENTER_KEY)
        val rightValue = valuesByKey.getValue(RIGHT_KEY)

        val centerItems = centerValue
            .split(",")
            .mapNotNull { value -> value.takeIf { it.isNotBlank() } }
            .mapNotNull(KeyboardQuickActionId::fromStorageValue)

        val rightItem = rightValue
            .takeIf { value -> value.isNotBlank() }
            ?.let(KeyboardQuickActionId::fromStorageValue)

        return reducer.sanitize(
            KeyboardQuickActionSlots(
                centerItemIds = centerItems,
                rightItemId = rightItem,
            ),
        )
    }

    private const val CENTER_KEY = "center"
    private const val RIGHT_KEY = "right"
}
