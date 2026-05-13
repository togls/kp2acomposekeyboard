package io.github.togls.kp2acomposekeyboard.ui.keyboard

internal object KeyboardTestTags {
    const val Root = "keyboard-root"
    const val CandidateRow = "keyboard-candidate-row"
    const val DefaultContent = "keyboard-default-content"
    const val LetterReferenceRow = "keyboard-letter-reference-row"
    const val WidthPolicySample = "keyboard-width-policy-sample"
    const val EntryNormalContent = "keyboard-entry-normal-content"
    const val EntryFixedFields = "keyboard-entry-fixed-fields"
    const val EntryRemainingFields = "keyboard-entry-remaining-fields"
    const val EntryActions = "keyboard-entry-actions"
    const val EntryExpandedContent = "keyboard-entry-expanded-content"
    const val EntryExpandedFields = "keyboard-entry-expanded-fields"
    const val PreviousPage = "keyboard-previous-page"
    const val NextPage = "keyboard-next-page"

    fun field(fieldId: String): String = "keyboard-field-$fieldId"
}
