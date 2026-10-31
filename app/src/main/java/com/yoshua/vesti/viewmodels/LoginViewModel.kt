package com.yoshua.vesti.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoshua.vesti.repository.data.AppDatabase
import com.yoshua.vesti.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,       // error del campo email
    val passwordError: String? = null,    // error del campo contraseña
    val passwordVisible: Boolean = false, // mostrar u ocultar la contraseña
    val isLoading: Boolean = false,
    val error: String? = null,            // error general (credenciales inválidas)
    val loggedUser: User? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(
            email = value,
            error = null,
            // Si el campo ya tenía un error, se revisa de nuevo mientras el usuario escribe
            emailError = if (it.emailError != null) Validaciones.errorEmail(value) else null
        )
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = value,
            error = null,
            passwordError = if (it.passwordError != null) Validaciones.errorPassword(value) else null
        )
    }

    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun login() {
        val state = _uiState.value

        // Primero se valida cada campo; si alguno falla, se muestran los errores y no se consulta la base
        val emailError = Validaciones.errorEmail(state.email)
        val passwordError = Validaciones.errorPassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // pequeña pausa para mostrar el indicador de carga
            val user = withContext(Dispatchers.IO) {
                userDao.login(state.email.trim(), state.password)
            }
            _uiState.update {
                if (user != null) it.copy(isLoading = false, loggedUser = user, password = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}