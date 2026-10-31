package com.yoshua.vesti.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yoshua.vesti.repository.models.Probador
import kotlinx.coroutines.flow.Flow

@Dao
interface ProbadorDao {
    // READ (reactivo con Flow)
    @Query("SELECT * FROM probadores ORDER BY numero ASC")
    fun getAllProbadores(): Flow<List<Probador>>

    @Query("SELECT * FROM probadores WHERE id = :id")
    suspend fun getProbadorById(id: Int): Probador?

    // Cuenta los probadores que ya usan ese número (sin contar el que se está editando)
    @Query("SELECT COUNT(*) FROM probadores WHERE numero = :numero AND id != :id")
    suspend fun contarPorNumero(numero: Int, id: Int): Int

    // CREATE
    @Insert
    suspend fun insertProbador(probador: Probador): Long

    // UPDATE
    @Update
    suspend fun updateProbador(probador: Probador)

    // DELETE
    @Delete
    suspend fun deleteProbador(probador: Probador)
}