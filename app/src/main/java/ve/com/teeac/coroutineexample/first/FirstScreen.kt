package ve.com.teeac.coroutineexample.first

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FirstScreen(
    viewModel: FirstViewModel = hiltViewModel()
) {

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = viewModel.state)
            Button(onClick = {
                viewModel.onLogin()
            }) {
                Text(text = "Login")
            }
        }
    }

}