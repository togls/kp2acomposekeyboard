package io.github.togls.kp2acomposekeyboard.feature.settings

import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilityItemId
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlots
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardUtilitySlotsReducer

object KeyboardUtilitySlotsPreferenceCodec {

    private val reducer = KeyboardUtilitySlotsReducer()

    fun encode(slots: KeyboardUtilitySlots): String {
        val sanitizedSlots = reducer.sanitize(slots)
        val centerValue = sanitizedSlots.centerItemIds.joinToString(separator = ",") { itemId ->
            itemId.storageValue
        }
        val rightValue = sanitizedSlots.rightItemId?.storageValue.orEmpty()

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
