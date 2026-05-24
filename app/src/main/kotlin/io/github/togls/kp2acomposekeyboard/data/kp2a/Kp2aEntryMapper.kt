package io.github.togls.kp2acomposekeyboard.data.kp2a

import io.github.togls.kp2acomposekeyboard.domain.field.KeyboardField
import io.github.togls.kp2acomposekeyboard.domain.session.KeyboardSession
import io.github.togls.kp2acomposekeyboard.domain.policy.KeyboardFieldClassifier
import io.github.togls.kp2acomposekeyboard.domain.policy.SensitiveFieldPolicy
import keepass2android.pluginsdk.KeepassDefs
import keepass2android.pluginsdk.Strings
import javax.inject.Inject

class Kp2aEntryMapper @Inject constructor(
    private val fieldClassifier: KeyboardFieldClassifier,
    private val sensitiveFieldPolicy: SensitiveFieldPolicy,
) {

    fun mapToSession(result: Kp2aEntryResult): KeyboardSession? {
        val fields = result.fields
            .asSequence()
            .filterNot { entry -> entry.key == KeepassDefs.TitleField }
            .filterNot { entry -> entry.key.startsWith(Strings.PREFIX_BINARY) }
            .filter { entry -> entry.key.isNotBlank() && entry.value.isNotEmpty() }
            .mapIndexed { index, entry ->
                mapField(
                    index = index,
                    key = entry.key,
                    value = entry.value,
                    protectedFields = result.protectedFields,
                )
            }
            .toList()

        if (fields.isEmpty()) {
            return null
        }

        return KeyboardSession(
            entryId = result.entryId,
            entryName = result.fields[KeepassDefs.TitleField]
                ?.takeIf { title -> title.isNotBlank() },
            fields = fields,
            createdAtMillis = System.currentTimeMillis(),
        )
    }

    private fun mapField(
        index: Int,
        key: String,
        value: String,
        protectedFields: Set<String>,
    ): KeyboardField {
        val normalizedKey = normalizeKp2aFieldKey(key)
        val type = fieldClassifier.classify(normalizedKey)

        return KeyboardField(
            id = createFieldId(
                index = index,
                key = key,
            ),
            key = key,
            label = fieldClassifier.displayLabel(
                key = normalizedKey,
                type = type,
            ),
            value = value,
            type = type,
            sensitive = sensitiveFieldPolicy.isSensitive(
                key = normalizedKey,
                type = type,
                protectedFields = protectedFields,
            ),
        )
    }

    private fun normalizeKp2aFieldKey(key: String): String {
        return when (key) {
            KeepassDefs.UserNameField -> "username"
            KeepassDefs.PasswordField -> "password"
            KeepassDefs.UrlField -> "url"
            KeepassDefs.NotesField -> "notes"
            else -> key
        }
    }

    private fun createFieldId(
        index: Int,
        key: String,
    ): String {
        val keyHash = key.hashCode().toUInt().toString(radix = 16)

        return "kp2a_${index}_$keyHash"
    }
}
