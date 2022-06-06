package ve.com.teeac.coroutineexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ve.com.teeac.coroutineexample.first.FirstScreen
import ve.com.teeac.coroutineexample.ui.theme.CoroutineExampleTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoroutineExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    FirstScreen()
                }
            }
        }
    }
}
