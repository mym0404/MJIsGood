package happy.mjstudio.sopt27.presentation.signin

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import happy.mjstudio.sopt27.authentication.Authenticator
import happy.mjstudio.sopt27.di.AuthenticatorModule.Companion.AUTHENTICATOR_TYPE
import happy.mjstudio.sopt27.utils.SimpleEventLiveData
import kotlinx.coroutines.launch
import javax.inject.Named

class SignInViewModel @ViewModelInject constructor(
    @Named(AUTHENTICATOR_TYPE) private val authenticator: Authenticator, /*@Assisted private val savedStateHandle: SavedStateHandle*/
) : ViewModel() {
    // StateFlow data binding support is coming in AGP 4.3
    // https://twitter.com/manuelvicnt/status/1314621067831521282
    val id = MutableLiveData("")
    val pw = MutableLiveData("")

    private var isAutoSignInTried = false

    val onSignInSuccess = SimpleEventLiveData()
    val onSignInFail = SimpleEventLiveData()

    suspend fun canAutoSignIn(): Boolean {
        if (isAutoSignInTried) return false
        isAutoSignInTried = true

        return authenticator.canAutoSignIn()
    }

    fun tryManualSignIn() = viewModelScope.launch {
        if (matchWithLastSignInInfo()) {
            onSignInSuccess.emit()
        } else {
            onSignInFail.emit()
        }
    }

    private suspend fun matchWithLastSignInInfo() = authenticator.signInWithId(id.value!!, pw.value!!)
}