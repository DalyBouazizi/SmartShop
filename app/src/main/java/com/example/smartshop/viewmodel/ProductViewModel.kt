package com.example.smartshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.data.local.AppDatabase
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Prêt")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)

        loadProducts()
        syncFromCloud()
        setupRealtimeSync()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.allProducts.collect { productList ->
                _products.value = productList
            }
        }
    }

    private fun syncFromCloud() {
        viewModelScope.launch {
            _syncStatus.value = "Synchronisation..."
            repository.syncFromFirestore()
            _syncStatus.value = "Synchronisé"
        }
    }

    private fun setupRealtimeSync() {
        repository.listenToFirestoreChanges {
            _syncStatus.value = "Mise à jour reçue"
        }
    }

    fun addProduct(name: String, quantity: Int, price: Double) {
        viewModelScope.launch {
            val product = Product(
                id = UUID.randomUUID().toString(),
                name = name,
                quantity = quantity,
                price = price
            )
            repository.insert(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product.copy(lastModified = System.currentTimeMillis()))
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    suspend fun getProductById(id: String): Product? {
        return repository.getProductById(id)
    }
}