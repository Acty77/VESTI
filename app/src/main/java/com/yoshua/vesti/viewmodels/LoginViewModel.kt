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
    val emailError: String? = null,       // Mensaje de error para el campo email
    val passwordError: String? = null,    // Mensaje de error para el campo contraseña
    val passwordVisible: Boolean = false, // Booleano para alternar la visibilidad de la contraseña
    val isLoading: Boolean = false,       // Estado de carga para activar el indicador visual (Progress)
    val error: String? = null,            // Mensaje de error global (ej: credenciales incorrectas)
    val loggedUser: User? = null          // Instancia del usuario autenticado en la sesión activa
)

/**
 * ViewModel que gestiona el proceso de autenticación de usuarios.
 * Hereda de AndroidViewModel para disponer del contexto global de la aplicación sin provocar fugas de memoria.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia del DAO de usuarios obtenido desde el Singleton de AppDatabase
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Estado privado mutable accesible solo dentro de este ViewModel para mantener encapsulamiento
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado público inmutable expuesto a la UI como StateFlow
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Actualiza el email en el estado y revalida dinámicamente si ya existía un error previo
    fun onEmailChange(value: String) = _uiState.update {
        it.copy(
            email = value,
            error = null, // Limpia el error global al escribir
            emailError = if (it.emailError != null) Validaciones.errorEmail(value) else null
        )
    }

    // Actualiza la contraseña en el estado y revalida dinámicamente
    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = value,
            error = null,
            passwordError = if (it.passwordError != null) Validaciones.errorPassword(value) else null
        )
    }

    // Alterna el booleano de visibilidad de la contraseña
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    // Procesa el inicio de sesión del usuario de forma asíncrona
    fun login() {
        val state = _uiState.value

        // Valida campos localmente antes de consultar la base de datos para optimizar recursos
        val emailError = Validaciones.errorEmail(state.email)
        val passwordError = Validaciones.errorPassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return // Cancela la ejecución si los campos no cumplen con el formato
        }

        // Ejecuta la consulta de BD dentro de la corrutina vinculada al ciclo de vida del ViewModel
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // Retardo artificial para permitir mostrar la animación de carga al usuario

            // Cambia el contexto de ejecución al hilo I/O especializado en lectura/escritura de datos
            val user = withContext(Dispatchers.IO) {
                userDao.login(state.email.trim(), state.password)
            }

            // Actualiza el estado final según el resultado devuelto por SQLite
            _uiState.update {
                if (user != null) it.copy(isLoading = false, loggedUser = user, password = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    // Cierra la sesión activa reiniciando el estado al valor por defecto
    fun logout() {
        _uiState.value = LoginUiState()
    }
}