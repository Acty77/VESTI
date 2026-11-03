package com.yoshua.vesti.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prendas_perdidas")
data class PrendaPerdida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val probadorNumero: Int,
    val descripcion: String,
    val fotoUri: String?,
    val fechaMillis: Long = System.currentTimeMillis(),
    val resuelto: Boolean = false
)