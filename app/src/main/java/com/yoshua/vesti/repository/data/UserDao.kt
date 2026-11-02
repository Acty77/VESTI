package com.yoshua.vesti.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yoshua.vesti.repository.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Retorna todos los usuarios ordenados alfabéticamente por su nombre.
    // Retorna Flow para observación reactiva continua en la pantalla de gestión de usuarios.
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    // Obtiene un usuario específico utilizando su ID.
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    // Consulta directa para verificar credenciales de inicio de sesión.
    // Retorna el objeto User si el correo y la contraseña coinciden; de lo contrario, retorna null.
    // 'LIMIT 1': Optimiza la consulta deteniendo la búsqueda en la primera coincidencia encontrada.
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // Inserta un nuevo usuario en la base de datos.
    // 'OnConflictStrategy.ABORT': Garantiza que no se puedan insertar usuarios con correos o IDs repetidos (mantiene la integridad de datos).
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // Actualiza la información de un usuario existente (ej. cambio de nombre, clave o rol).
    @Update
    suspend fun updateUser(user: User)

    // Elimina un usuario de la base de datos SQLite.
    @Delete
    suspend fun deleteUser(user: User)
}