package moe.shizuku.manager.audio

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class AudioMasterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun customProfileCanBeSavedFromTheRealScreen() {
        composeRule.setContent {
            MaterialTheme {
                AudioMasterScreen(bottomPadding = 0.dp, onBack = {})
            }
        }

        composeRule.onNodeWithText("Custom profile name").performTextInput("Instrumented")
        composeRule.onNodeWithText("Save current state as profile").performClick()
        composeRule.onNodeWithText("Instrumented saved").assertIsDisplayed()
    }

    @Test
    fun restoreControlIsAlwaysVisibleForRecovery() {
        composeRule.setContent {
            MaterialTheme {
                AudioMasterScreen(bottomPadding = 0.dp, onBack = {})
            }
        }

        composeRule.onNodeWithText("Restore previous state").assertIsDisplayed()
    }
}
