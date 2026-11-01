package com.yoshua.vesti.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoshua.vesti.repository.data.AppDatabase
import com.yoshua.vesti.repository.models.Probador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProbadorViewModel(application: Application) : AndroidViewModel(application) {

    private val probadorDao = AppDatabase.getDatabase(application).probadorDao()

    // READ: lista reactiva expuesta a la vista
    val probadores: StateFlow<List<Probador>> = probadorDao.getAllProbadores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getProbador(id: Int): Probador? = withContext(Dispatchers.IO) {
        probadorDao.getProbadorById(id)
    }

    // CREATE / UPDATE. Avisa con onResult(false) si el número ya lo usa otro probador.
    fun saveProbador(id: Int, numero: Int, ubicacion: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val guardado = withContext(Dispatchers.IO) {
                if (probadorDao.contarPorNumero(numero, id) > 0) {
                    false
                } else {
                    if (id == 0) {
                        // Crear nuevo probador (inicioMillis es null por defecto)
                        val nuevoProbador = Probador(numero = numero, ubicacion = ubicacion)
                        probadorDao.insertProbador(nuevoProbador)
                    } else {
                        // Actualizar existente preservando su inicioMillis activo
                        val existente = probadorDao.getProbadorById(id)
                        val probadorActualizado = existente?.copy(
                            numero = numero,
                            ubicacion = ubicacion
                        ) ?: Probador(id = id, numero = numero, ubicacion = ubicacion)
                        probadorDao.updateProbador(probadorActualizado)
                    }
                    true
                }
            }
            onResult(guardado)
        }
    }

    // DELETE
    fun deleteProbador(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            probadorDao.deleteProbador(probador)
        }
    }

    // Cambia el estado: si está libre guarda System.currentTimeMillis(), si está ocupado/excedido lo vuelve null
    fun toggleEstadoProbador(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoInicio = if (probador.inicioMillis == null) System.currentTimeMillis() else null
            val probadorActualizado = probador.copy(inicioMillis = nuevoInicio)
            probadorDao.updateProbador(probadorActualizado)
        }
    }

    fun toggleEnServicio(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualizado = probador.copy(
                enServicio = !probador.enServicio,
                inicioMillis = null // Al sacar de servicio se libera el temporizador si estaba ocupado
            )
            probadorDao.updateProbador(actualizado)
        }
    }

}