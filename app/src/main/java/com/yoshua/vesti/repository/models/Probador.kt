package com.yoshua.vesti.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "probadores", indices = [Index(value = ["numero"], unique = true)])
data class Probador(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: Int,        // número visible del probador (ej.: 3 -> "Probador 03"); no se repite
    val ubicacion: String,// dónde está dentro de la tienda (ej.: "Vestuario mujer, pasillo 2")
    val inicioMillis: Long? = null,
    val enServicio: Boolean = true
)

fun calcularEstado(probador: Probador, ahoraMillis: Long, umbralMinutos: Int=10 ): EstadoProbador{
    if (!probador.enServicio) return EstadoProbador.FUERA_DE_SERVICIO
    val inicio = probador.inicioMillis ?: return EstadoProbador.LIBRE
    val minutosEnUso = (ahoraMillis - inicio) / (1000 * 60)
    return if (minutosEnUso >= umbralMinutos) EstadoProbador.EXCEDIDO else EstadoProbador.OCUPADO
}