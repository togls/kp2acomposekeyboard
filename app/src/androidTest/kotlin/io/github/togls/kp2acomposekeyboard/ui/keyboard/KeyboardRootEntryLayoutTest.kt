package io.github.togls.kp2acomposekeyboard.ui.keyboard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.github.togls.kp2acomposekeyboard.domain.keyboard.EntryFieldDisplayMode
import org.junit.Rule
import org.junit.Test

class KeyboardRootEntryLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun normalEntryHasNoPageControls() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Paged))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.PreviousPage)).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(KeyboardTestTags.NextPage)).assertCountEquals(0)
    }

    @Test
    fun normalRemainingFieldsScrollVertically() {
        composeRule.setContent {
            KeyboardRootTestContent(
                testEntryState(
                    EntryFieldDisplayMode.Paged,
                    extraFieldCount = 12
                )
            )
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryRemainingFields)
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Extra 11").assertIsDisplayed()
    }

    @Test
    fun expandedEntryShowsPageControls() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryState(EntryFieldDisplayMode.Expanded))
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryExpandedContent).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.PreviousPage).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.NextPage).assertIsDisplayed()
    }

    @Test
    fun expandedControlsDisableWhenContentFitsOnePage() {
        composeRule.setContent {
            KeyboardRootTestContent(
                testEntryState(EntryFieldDisplayMode.Expanded, extraFieldCount = 0),
            )
        }

        composeRule.onNodeWithTag(KeyboardTestTags.PreviousPage).assertIsNotEnabled()
        composeRule.onNodeWithTag(KeyboardTestTags.NextPage).assertIsNotEnabled()
    }

    @Test
    fun emptyEntryLayoutShowsLanguageSwitchAndNoFieldButtons() {
        composeRule.setContent {
            KeyboardRootTestContent(testEntryEmptyState())
        }

        composeRule.onNodeWithTag(KeyboardTestTags.EntryNormalContent).assertIsDisplayed()
        composeRule.onNodeWithTag(KeyboardTestTags.LanguageSwitchKey).assertIsDisplayed()
        composeRule.onAllNodes(hasText("Username")).assertCountEquals(0)
        composeRule.onAllNodes(hasText("Password")).assertCountEquals(0)
        composeRule.onAllNodes(hasText("TOTP")).assertCountEquals(0)
    }
}
