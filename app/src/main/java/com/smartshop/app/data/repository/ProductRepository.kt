package com.smartshop.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.smartshop.app.data.model.Product
import com.smartshop.app.data.model.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Real-time product stream
    fun getProducts(): Flow<Resource<List<Product>>> = callbackFlow {

        // Emit loading first
        trySend(Resource.Loading)

        val listener = firestore
            .collection("products")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    // Convert Firestore document to Product object
                    // copy(id = doc.id) adds the document ID to the object
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(Resource.Success(products))
            }

        // Remove listener when Flow is cancelled
        // This prevents memory leaks
        awaitClose { listener.remove() }
    }


    // One-time fetch for product detail
    suspend fun getProductById(productId: String): Resource<Product> {
        return try {
            val doc = firestore
                .collection("products")
                .document(productId)
                .get()
                .await()

            val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Product not found")

            Resource.Success(product)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching product")
        }
    }

    // Get all products with the same name — for price comparison
    fun getProductsByName(name: String): Flow<Resource<List<Product>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = firestore
            .collection("products")
            .whereEqualTo("name", name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by price — cheapest first
                trySend(Resource.Success(products.sortedBy { it.price }))
            }

        awaitClose { listener.remove() }
    }
}