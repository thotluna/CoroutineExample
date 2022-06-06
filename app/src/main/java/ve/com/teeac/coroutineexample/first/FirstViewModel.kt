package ve.com.teeac.coroutineexample.first

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import ve.com.teeac.coroutineexample.core.Result

@HiltViewModel
class FirstViewModel @Inject constructor(
    private val repository: FirstRepository
): ViewModel() {

    private var _state by mutableStateOf("")
    val state get() = _state

    fun onLogin() {
        viewModelScope.launch {
            val result = repository.makeLoginRequest()
            when (result) {
                is Result.Success -> {
                    _state = result.data
                    Timber.d("Success: ${result.data}")
                    Timber.d("_State: $_state")
                    Timber.d("State: $_state")
                }
                is Result.Error -> {
                    Timber.e("Error: ${result.exception}")
                }
            }
        }
    }
}