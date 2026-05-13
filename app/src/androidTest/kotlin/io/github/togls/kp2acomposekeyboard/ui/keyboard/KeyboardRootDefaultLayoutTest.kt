package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class KeyboardRootDefaultLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersDefaultKeyboard() {
        composeRule.setContent {
            KeyboardRootTestContent(state = testDefaultState())
        }

        composeRule.onNodeWithTag(KeyboardTestTags.Root).assertIsDisplayed()
        composeRule.onNodeWithText("q").assertIsDisplayed()
        composeRule.onNodeWithText("p").assertIsDisplayed()
    }

    @Test
    fun tenKeyReferenceRowUsesConsistentWidths() {
        composeRule.setContent {
            KeyboardRootTestContent(state = testDefaultState())
        }

        val keyBounds = "qwertyuiop".associateWith { letter ->
            composeRule.onNodeWithTag(KeyboardTestTags.letterKey(letter))
                .fetchSemanticsNode()
                .boundsInRoot
        }
        val widths = keyBounds.values.map { it.width }
        val firstWidth = widths.first()

        widths.forEach { width ->
            assertEquals("keyBounds=$keyBounds", firstWidth, width, 0.5f)
        }
    }

    @Test
    fun defaultRowsFitInsideKeyboardContent() {
        composeRule.setContent {
            KeyboardRootTestContent(state = testDefaultState())
        }

        val contentBounds = composeRule.onNodeWithTag(KeyboardTestTags.DefaultContent)
            .fetchSemanticsNode()
            .boundsInRoot
        val switchKeyBounds = composeRule.onNodeWithTag(KeyboardTestTags.DefaultSwitchKey)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "contentBounds=$contentBounds switchKeyBounds=$switchKeyBounds",
            switchKeyBounds.bottom <= contentBounds.bottom,
        )
    }
}
