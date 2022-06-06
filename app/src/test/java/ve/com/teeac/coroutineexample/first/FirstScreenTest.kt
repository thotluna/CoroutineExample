package ve.com.teeac.coroutineexample.first

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FirstScreenTest{

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var repository: FirstRepository

    private lateinit var viewModel: FirstViewModel

    @Before
    fun setUp() {
        hiltRule.inject()
        viewModel = FirstViewModel(repository)
    }

    @Test
    fun `should return one screen with article of repository injected`(){
        composeTestRule.setContent {
            FirstScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Login").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Login Success")
    }
}