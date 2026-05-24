package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import io.github.togls.kp2acomposekeyboard.ui.keyboard.shared.KeyboardTestTags
import org.junit.Assert.fail
import org.junit.Rule

class KeyboardHeightProbeTest {
    @get:Rule
    val composeRule = createComposeRule()

    //@Test
    fun printDefaultBounds() {
        composeRule.setContent {
            KeyboardImeContentTestContent(state = testTextInputState())
        }
        composeRule.waitForIdle()

        val defaultRoot = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val defaultContent = composeRule.onNodeWithTag(KeyboardTestTags.DefaultContent)
            .fetchSemanticsNode()
            .boundsInRoot
        val q = composeRule.onNodeWithTag(KeyboardTestTags.letterKey('q'))
            .fetchSemanticsNode().boundsInRoot
        val a = composeRule.onNodeWithTag(KeyboardTestTags.letterKey('a'))
            .fetchSemanticsNode().boundsInRoot
        val z = composeRule.onNodeWithTag(KeyboardTestTags.letterKey('z'))
            .fetchSemanticsNode().boundsInRoot
        val action = composeRule.onNodeWithText("?123").fetchSemanticsNode().boundsInRoot

        fail(
            "defaultRoot=$defaultRoot default=$defaultContent q=$q a=$a z=$z action=$action",
        )
    }

    //@Test
    fun printEntryBounds() {
        composeRule.setContent {
            KeyboardImeContentTestContent(state = testEntryState(EntryFieldDisplayMode.Paged))
        }
        composeRule.waitForIdle()

        val entryRoot = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val entryContent = composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent)
            .fetchSemanticsNode()
            .boundsInRoot
        val fixed = composeRule.onNodeWithTag(KeyboardTestTags.EntryFixedFields)
            .fetchSemanticsNode()
            .boundsInRoot
        val remaining = composeRule.onNodeWithTag(KeyboardTestTags.EntryRemainingFields)
            .fetchSemanticsNode()
            .boundsInRoot
        val entryActions = composeRule.onNodeWithTag(KeyboardTestTags.EntryActions)
            .fetchSemanticsNode()
            .boundsInRoot

        fail(
            "entryRoot=$entryRoot entry=$entryContent fixed=$fixed remaining=$remaining actions=$entryActions",
        )
    }
}
