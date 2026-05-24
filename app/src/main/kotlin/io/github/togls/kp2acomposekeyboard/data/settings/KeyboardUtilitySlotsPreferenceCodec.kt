package io.github.togls.kp2acomposekeyboard.data.settings

import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.domain.keyboard.KeyboardUtilitySlotsReducer

object KeyboardUtilitySlotsPreferenceCodec {

    private val reducer = KeyboardUtilitySlotsReducer()

    fun encode(slots: KeyboardUtilitySlots): String {
        val sanitizedSlots = reducer.sanitize(slots)
        val centerValue = sanitizedSlots.centerItemIds.joinToString(separator = ",") { itemId ->
            itemId.storageValue
        }
        val rightValue = sanitizedSlots.rightItemId?.storageValue.orEmpty()

        // Keep the value compact and non-sensitive: only utility IDs are stored.
        return "center=$centerValue;right=$rightValue"
    }

    fun decode(rawValue: String?): KeyboardUtilitySlots {
        if (rawValue.isNullOrBlank()) {
            return KeyboardUtilitySlots()
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
            return KeyboardUtilitySlots()
        }

        val centerValue = valuesByKey.getValue(CENTER_KEY)
        val rightValue = valuesByKey.getValue(RIGHT_KEY)

        val centerItems = centerValue
            .split(",")
            .mapNotNull { value -> value.takeIf { it.isNotBlank() } }
            .mapNotNull(KeyboardUtilityItemId::fromStorageValue)

        val rightItem = rightValue
            .takeIf { value -> value.isNotBlank() }
            ?.let(KeyboardUtilityItemId::fromStorageValue)

        return reducer.sanitize(
            KeyboardUtilitySlots(
                centerItemIds = centerItems,
                rightItemId = rightItem,
            ),
        )
    }

    private const val CENTER_KEY = "center"
    private const val RIGHT_KEY = "right"
}
