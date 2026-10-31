package com.yoshua.vesti.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "probadores", indices = [Index(value = ["numero"], unique = true)])
data class Probador(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: Int,        // número visible del probador (ej.: 3 -> "Probador 03"); no se repite
    val ubicacion: String   // dónde está dentro de la tienda (ej.: "Vestuario mujer, pasillo 2")
)