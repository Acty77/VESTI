package com.example.app.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.app.repository.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // READ (reactivo con Flow)
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    // UPDATE
    @Update
    suspend fun updateProduct(product: Product)

    // DELETE
    @Delete
    suspend fun deleteProduct(product: Product)
}
