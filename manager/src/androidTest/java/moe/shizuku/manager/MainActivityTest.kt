package moe.shizuku.manager

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityRendersAComposeRoot() {
        composeRule.onRoot().assertExists()
    }
}
