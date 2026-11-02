package com.yoshua.vesti.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoshua.vesti.repository.data.AppDatabase
import com.yoshua.vesti.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Lista de usuarios reactiva observada desde la interfaz de usuario
    val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flujo mutable para enviar notificaciones de error a la UI
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    // Búsqueda de usuario por ID en hilo I/O
    suspend fun getUser(id: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    // Guarda o actualiza un usuario aplicando reglas de formato y validación de correo único
    fun saveUser(
        id: Int,
        name: String,
        email: String,
        password: String,
        imageUri: String?,
        tipoUsuarioId: Int,
        onSuccess: () -> Unit
    ) {
        // Valida que el nombre contenga únicamente caracteres alfabéticos
        if (!Validaciones.esSoloLetras(name)) {
            _message.value = "El nombre solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = User(id = id, name = name, email = email, password = password, profileImageUri = imageUri, tipoUsuarioId = tipoUsuarioId)
                    if (id == 0) userDao.insertUser(user) else userDao.updateUser(user)
                }
                onSuccess() // Ejecuta la navegación de regreso tras completar el guardado
            } catch (e: SQLiteConstraintException) {
                // Atrapa el error si la base de datos detecta un correo duplicado
                _message.value = "Ya existe un usuario con el email $email"
            }
        }
    }

    // Borra un usuario en segundo plano
    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }
}