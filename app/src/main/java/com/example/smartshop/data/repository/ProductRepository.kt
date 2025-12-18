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
            .document(auth.currentUser?.uid ?: "guest")
            .collection("products")

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insert(product: Product) {
        productDao.insertProduct(product)

        if (auth.currentUser != null) {
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
    }

    suspend fun update(product: Product) {
        productDao.updateProduct(product)

        if (auth.currentUser != null) {
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
    }

    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)

        if (auth.currentUser != null) {
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
    }

    suspend fun syncFromFirestore() {
        if (auth.currentUser == null) {
            Log.d("ProductRepository", "No user logged in, skipping Firestore sync")
            return
        }

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

    fun listenToFirestoreChanges(onUpdate: () -> Unit) {
        if (auth.currentUser == null) {
            Log.d("ProductRepository", "No user logged in, skipping Firestore listener")
            return
        }

        getUserProductsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepository", "Listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val product = change.document.toObject(Product::class.java)

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
