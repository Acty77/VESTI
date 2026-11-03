package com.yoshua.vesti.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yoshua.vesti.repository.data.AppDatabase
import com.yoshua.vesti.repository.models.PrendaPerdida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PrendaPerdidaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).prendaPerdidaDao()

    val prendas: StateFlow<List<PrendaPerdida>> = dao.getAllPrendasPerdidas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarPrenda(numProbador: Int, descripcion: String, fotoUri: String?, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val nueva = PrendaPerdida(
                probadorNumero = numProbador,
                descripcion = descripcion,
                fotoUri = fotoUri
            )
            dao.insertPrendaPerdida(nueva)
            launch(Dispatchers.Main) { onSuccess() }
        }
    }
}