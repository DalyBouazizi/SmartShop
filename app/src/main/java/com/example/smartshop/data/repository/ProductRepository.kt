package com.example.smartshop.data.repository

import android.util.Log
import com.example.smartshop.data.local.ProductDao
import com.example.smartshop.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductRepository(private val productDao: ProductDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    private fun getUserProductsCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("products")

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insert(product: Product) {
        // Insert locally first
        productDao.insertProduct(product)

        // Sync to Firestore
        try {
            getUserProductsCollection()
                .document(product.id)
                .set(product)
                .await()
            Log.d("ProductRepository", "Product synced to Firestore: ${product.name}")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error syncing to Firestore", e)
        }
    }

    suspend fun update(product: Product) {
        productDao.updateProduct(product)

        try {
            getUserProductsCollection()
                .document(product.id)
                .set(product)
                .await()
            Log.d("ProductRepository", "Product updated in Firestore: ${product.name}")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating Firestore", e)
        }
    }

    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)

        try {
            getUserProductsCollection()
                .document(product.id)
                .delete()
                .await()
            Log.d("ProductRepository", "Product deleted from Firestore: ${product.name}")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error deleting from Firestore", e)
        }
    }

    // Sync Firestore -> Room (download cloud data)
    suspend fun syncFromFirestore() {
        try {
            val snapshot = getUserProductsCollection().get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)
            }

            products.forEach { product ->
                productDao.insertProduct(product)
            }

            Log.d("ProductRepository", "Synced ${products.size} products from Firestore")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error syncing from Firestore", e)
        }
    }

    // Listen for real-time updates - FIXED VERSION
    fun listenToFirestoreChanges(onUpdate: () -> Unit) {
        getUserProductsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepository", "Listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val product = change.document.toObject(Product::class.java)

                    // Launch coroutine in the repository's scope
                    coroutineScope.launch {
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                productDao.insertProduct(product)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                productDao.deleteProduct(product)
                            }
                        }
                    }
                }

                onUpdate()
            }
    }
}