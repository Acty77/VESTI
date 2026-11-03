package com.yoshua.vesti.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yoshua.vesti.repository.models.PrendaPerdida
import kotlinx.coroutines.flow.Flow

@Dao
interface PrendaPerdidaDao {
    @Query("SELECT * FROM prendas_perdidas ORDER BY fechaMillis DESC")
    fun getAllPrendasPerdidas(): Flow<List<PrendaPerdida>>

    @Insert
    suspend fun insertPrendaPerdida(prenda: PrendaPerdida): Long
}