package com.yoshua.vesti.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoshua.vesti.repository.data.AppDatabase
import com.yoshua.vesti.repository.models.TipoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TipoUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tipoUsuarioDao()

    // Lista de tipos de usuario expuesta a la UI mediante un StateFlow caliente
    val tipos: StateFlow<List<TipoUsuario>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flujo para la gestión de mensajes de error o advertencia en la pantalla
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Limpia el mensaje de la UI
    fun clearMessage() { _message.value = null }

    /** Crea o actualiza un tipo de usuario aplicando validaciones de formato y captura de excepciones de BD */
    fun save(id: Int, nombre: String, onSuccess: () -> Unit = {}) {
        val valor = nombre.trim()
        if (valor.isEmpty() || valor.length > TipoUsuario.MAX_LARGO) {
            _message.value = "El tipo debe tener entre 1 y ${TipoUsuario.MAX_LARGO} caracteres"
            return
        }
        // Aplica expresión regular para permitir solo texto sin caracteres especiales ni números
        if (!Validaciones.esSoloLetras(valor)) {
            _message.value = "El tipo solo puede contener letras y un espacio entre palabras (sin números ni símbolos)"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val tipo = TipoUsuario(id = id, tipoUsuario = valor)
                    if (id == 0) dao.insert(tipo) else dao.update(tipo)
                }
                _message.value = null
                onSuccess() // Ejecuta el callback de éxito
            } catch (e: SQLiteConstraintException) {
                // Atrapa el error cuando la base de datos rechaza una inserción duplicada por clave única
                _message.value = "El tipo \"$valor\" ya existe"
            }
        }
    }

    /** Elimina un tipo de usuario controlando la restricción de integridad referencial (FK) */
    fun delete(tipo: TipoUsuario) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.delete(tipo) }
            } catch (e: SQLiteConstraintException) {
                // Impide borrar un rol asignado a usuarios registrados en la tabla 'users'
                _message.value = "No se puede eliminar \"${tipo.tipoUsuario}\": hay usuarios con ese tipo"
            }
        }
    }
}