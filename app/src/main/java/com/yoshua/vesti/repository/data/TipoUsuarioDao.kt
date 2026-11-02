package com.yoshua.vesti.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yoshua.vesti.repository.models.TipoUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoUsuarioDao {

    // Obtiene la lista completa de roles ordenada por ID.
    // Al usar Flow, la UI se actualiza automáticamente si se agregan o modifican roles.
    @Query("SELECT * FROM tipo_usuarios ORDER BY id ASC")
    fun getAll(): Flow<List<TipoUsuario>>

    // Busca un rol por su clave primaria (id).
    // Es 'suspend' para ejecutarse fuera del hilo principal de UI (Main Thread).
    @Query("SELECT * FROM tipo_usuarios WHERE id = :id")
    suspend fun getById(id: Int): TipoUsuario?

    // Inserta un nuevo rol.
    // 'onConflict = OnConflictStrategy.ABORT': Si se intenta insertar un ID duplicado, aborta la transacción y lanza una excepción.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tipo: TipoUsuario): Long

    // Actualiza los datos de un tipo de usuario existente.
    @Update
    suspend fun update(tipo: TipoUsuario)

    // Elimina un tipo de usuario de la tabla.
    @Delete
    suspend fun delete(tipo: TipoUsuario)
}