package com.smartshop.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Login state — private mutable, public read-only
    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState = _loginState.asStateFlow()

    // Register state
    private val _registerState = MutableStateFlow<Resource<Unit>?>(null)
    val registerState = _registerState.asStateFlow()

    // Forgot password state
    private val _forgotPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    val isLoggedIn: Boolean
        get() = authRepository.currentUser != null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = authRepository.login(email, password)
            _loginState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error   -> Resource.Error(result.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            val result = authRepository.register(name, email, password)
            _registerState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error   -> Resource.Error(result.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = Resource.Loading
            val result = authRepository.forgotPassword(email)
            _forgotPasswordState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error   -> Resource.Error(result.message)
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    fun logout() = authRepository.logout()

    // Reset states when leaving screen
    fun resetLoginState() { _loginState.value = null }
    fun resetRegisterState() { _registerState.value = null }
}