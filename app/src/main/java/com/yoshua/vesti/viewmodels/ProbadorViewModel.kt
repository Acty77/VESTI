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
                    val probador = Probador(id = id, numero = numero, ubicacion = ubicacion)
                    if (id == 0) probadorDao.insertProbador(probador) else probadorDao.updateProbador(probador)
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
}