package ve.com.teeac.coroutineexample.first

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import ve.com.teeac.coroutineexample.core.Result
import javax.inject.Inject

class FirstRepository
@Inject constructor()
{

    suspend fun makeLoginRequest(): Result<String> {
        return withContext(Dispatchers.IO) {
            delay(2000)
            Timber.d("Login request")
            return@withContext Result.Success("Login Success")
        }
    }
}