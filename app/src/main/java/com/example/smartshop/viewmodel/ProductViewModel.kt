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

    private val _cart = MutableStateFlow<List<Product>>(emptyList())
    val cart: StateFlow<List<Product>> = _cart.asStateFlow()

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
            repository.syncFromFirestore()
        }
    }

    private fun setupRealtimeSync() {
        repository.listenToFirestoreChanges { }
    }

    fun addProduct(
        name: String,
        quantity: Int,
        price: Double,
        imageUrl: String,
        rating: Float,
        isDeal: Boolean
    ) {
        viewModelScope.launch {
            val product = Product(
                id = UUID.randomUUID().toString(),
                name = name,
                quantity = quantity,
                price = price,
                imageUrl = imageUrl,
                rating = rating,
                isDealOfTheDay = isDeal
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

    // ---------- CART LOGIC (no stock change until confirm) ----------

    fun addItemToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.id == product.id }
        if (idx >= 0) {
            val existing = current[idx]
            current[idx] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(product.copy(quantity = 1))
        }
        _cart.value = current
    }

    fun changeCartQuantity(productId: String, delta: Int) {
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.id == productId }
        if (idx < 0) return
        val item = current[idx]
        val newQty = item.quantity + delta
        if (newQty <= 0) {
            current.removeAt(idx)
        } else {
            current[idx] = item.copy(quantity = newQty)
        }
        _cart.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cart.value.toMutableList()
        current.removeAll { it.id == productId }
        _cart.value = current
    }

    fun confirmOrder(paymentMethod: String, address: String) {
        // For school project: just deduct from stock and clear cart, no real payment.
        viewModelScope.launch {
            val cartSnapshot = _cart.value.toList()
            val currentProducts = _products.value.toMutableList()

            cartSnapshot.forEach { cartItem ->
                val idx = currentProducts.indexOfFirst { it.id == cartItem.id }
                if (idx >= 0) {
                    val product = currentProducts[idx]
                    val newStock = (product.quantity - cartItem.quantity).coerceAtLeast(0)
                    val updated = product.copy(quantity = newStock)
                    currentProducts[idx] = updated
                    repository.update(updated)
                }
            }

            _cart.value = emptyList()
        }
    }
}
