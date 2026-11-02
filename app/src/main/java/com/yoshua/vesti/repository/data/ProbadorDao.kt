package com.yoshua.vesti.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yoshua.vesti.repository.models.Probador
import kotlinx.coroutines.flow.Flow

@Dao // Le indica a Room que genere automáticamente la implementación interna de las consultas SQL
interface ProbadorDao {

    // Obtiene todos los probadores ordenados numéricamente.
    // Retorna 'Flow': Un flujo de datos reactivo. Cuando la tabla se modifica, Room emite automáticamente la nueva lista a la UI sin requerir 'suspend'.
    @Query("SELECT * FROM probadores ORDER BY numero ASC")
    fun getAllProbadores(): Flow<List<Probador>>

    // Busca un probador específico por su ID único.
    // 'suspend': Obliga a que la consulta se ejecute en un hilo secundario (corrutina) para no bloquear la pantalla.
    // Retorna 'Probador?' porque el resultado puede ser nulo si el ID no se encuentra.
    @Query("SELECT * FROM probadores WHERE id = :id")
    suspend fun getProbadorById(id: Int): Probador?

    // Valida la regla de negocio: "No pueden existir dos probadores con el mismo número".
    // Cuenta cuántos registros tienen dicho número, ignorando el id actual (necesario al editar para no chocarse a sí mismo).
    @Query("SELECT COUNT(*) FROM probadores WHERE numero = :numero AND id != :id")
    suspend fun contarPorNumero(numero: Int, id: Int): Int

    // Inserta un probador nuevo en SQLite.
    // Retorna 'Long', que es el id autonumérico autogenerado por la base de datos.
    @Insert
    suspend fun insertProbador(probador: Probador): Long

    // Actualiza los campos de un probador en la base de datos buscando coincidencia por su ID.
    @Update
    suspend fun updateProbador(probador: Probador)

    // Elimina la fila de la tabla correspondiente al probador proporcionado.
    @Delete
    suspend fun deleteProbador(probador: Probador)
}