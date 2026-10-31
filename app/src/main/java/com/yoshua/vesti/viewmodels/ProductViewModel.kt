package com.example.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.repository.data.AppDatabase
import com.example.app.repository.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao = AppDatabase.getDatabase(application).productDao()

    // READ: lista reactiva expuesta a la vista
    val products: StateFlow<List<Product>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lectura puntual (se invoca desde una corrutina de la vista: LaunchedEffect)
    suspend fun getProduct(id: Int): Product? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    // CREATE / UPDATE
    fun saveProduct(id: Int, name: String, price: Double, stock: Int, imageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = Product(id = id, name = name, price = price, stock = stock, imageUri = imageUri)
            if (id == 0) productDao.insertProduct(product) else productDao.updateProduct(product)
        }
    }

    // DELETE
    fun deleteProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
    }
}
