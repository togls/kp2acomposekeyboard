package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToString
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.feature.keyboard.KeyboardIntent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KeyboardRootSensitiveDataTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sensitiveValuesDoNotAppearInTextContentDescriptionsOrSemanticsDump() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Paged))
        }

        val semanticsDump = composeRule.onRoot(useUnmergedTree = true).printToString()

        forbiddenSensitiveValues.forEach { forbiddenValue ->
            composeRule.onAllNodes(hasText(forbiddenValue, substring = true)).assertCountEquals(0)
            composeRule.onAllNodes(hasContentDescription(forbiddenValue, substring = true))
                .assertCountEquals(0)
            assertFalse(semanticsDump.contains(forbiddenValue))
        }
    }

    @Test
    fun fieldClickSendsFieldIdOnly() {
        val intents = mutableListOf<KeyboardIntent>()
        composeRule.setContent {
            KeyboardRootTestContent(
                state = testEntryState(EntryFieldDisplayMode.Paged),
                onIntent = intents::add,
            )
        }

        composeRule.onNodeWithTag(KeyboardTestTags.field("password")).performClick()

        assertTrue(intents.contains(KeyboardIntent.CommitField("password")))
        assertFalse(intents.contains(KeyboardIntent.CommitText(PASSWORD_SHOULD_NOT_APPEAR)))
    }
}
