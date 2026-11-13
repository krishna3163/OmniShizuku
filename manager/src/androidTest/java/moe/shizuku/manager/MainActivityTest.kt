package moe.shizuku.manager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun composeContentCanBeRenderedAndFound() {
        composeRule.setContent {
            Text("OmniShizuku")
        }
        composeRule.onRoot().assertIsDisplayed()
        composeRule.onNodeWithText("OmniShizuku").assertIsDisplayed()
    }
}
