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

    // Convierte el Flow frío retornado por Room en un StateFlow caliente mediante 'stateIn'.
    // WhileSubscribed(5000): Mantiene el flujo activo durante 5 segundos tras perder la última vista (ej. al girar la pantalla).
    val probadores: StateFlow<List<Probador>> = probadorDao.getAllProbadores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Obtención puntual de un probador por ID ejecutada en el hilo secundario IO
    suspend fun getProbador(id: Int): Probador? = withContext(Dispatchers.IO) {
        probadorDao.getProbadorById(id)
    }

    // Crea o actualiza un probador validando que el número no esté duplicado
    fun saveProbador(id: Int, numero: Int, ubicacion: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val guardado = withContext(Dispatchers.IO) {
                if (probadorDao.contarPorNumero(numero, id) > 0) {
                    false // Retorna false si el número ya pertenece a otro probador
                } else {
                    if (id == 0) {
                        // Creación de registro nuevo
                        val nuevoProbador = Probador(numero = numero, ubicacion = ubicacion)
                        probadorDao.insertProbador(nuevoProbador)
                    } else {
                        // Actualización de registro existente preservando la marca de tiempo activa (inicioMillis)
                        val existente = probadorDao.getProbadorById(id)
                        val probadorActualizado = existente?.copy(
                            numero = numero,
                            ubicacion = ubicacion
                        ) ?: Probador(id = id, numero = numero, ubicacion = ubicacion)
                        probadorDao.updateProbador(probadorActualizado)
                    }
                    true // Operación exitosa
                }
            }
            onResult(guardado) // Notifica el resultado a la UI en el hilo principal
        }
    }

    // Elimina el registro del probador en segundo plano
    fun deleteProbador(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            probadorDao.deleteProbador(probador)
        }
    }

    // Alterna el temporizador de uso: asigna el tiempo actual en milisegundos si pasa a ocupado, o null si pasa a libre
    fun toggleEstadoProbador(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoInicio = if (probador.inicioMillis == null) System.currentTimeMillis() else null
            val probadorActualizado = probador.copy(inicioMillis = nuevoInicio)
            probadorDao.updateProbador(probadorActualizado)
        }
    }

    // Alterna la disponibilidad operativa del probador (En Servicio / Fuera de Servicio) y reinicia el temporizador
    fun toggleEnServicio(probador: Probador) {
        viewModelScope.launch(Dispatchers.IO) {
            val actualizado = probador.copy(
                enServicio = !probador.enServicio,
                inicioMillis = null // Si estaba ocupado, detiene y limpia el temporizador
            )
            probadorDao.updateProbador(actualizado)
        }
    }

}