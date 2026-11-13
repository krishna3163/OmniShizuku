package moe.shizuku.manager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.screen.LabsScreen
import org.junit.Rule
import org.junit.Test

class LabsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchFiltersLabsAndClearRestoresCatalogue() {
        composeRule.setContent {
            MaterialTheme {
                LabsScreen(bottomPadding = 0.dp, onOpenDetail = {})
            }
        }

        composeRule.onNodeWithText("Audio Stream Master").assertIsDisplayed()
        composeRule.onNodeWithText("Search tools").performTextInput("display")
        composeRule.onNodeWithText("Display").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.onNodeWithText("Audio Stream Master").assertIsDisplayed()
    }

    @Test
    fun selectingLabInvokesDetailCallback() {
        var selected: Detail? = null
        composeRule.setContent {
            MaterialTheme {
                LabsScreen(bottomPadding = 0.dp, onOpenDetail = { selected = it })
            }
        }

        composeRule.onNodeWithText("Audio Stream Master").performClick()

        assert(selected == Detail.AUDIO_MASTER)
    }
}
