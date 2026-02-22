package com.smartshop.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.data.model.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Helper to get current user's cart reference
    private fun cartRef() = auth.currentUser?.uid?.let { uid ->
        firestore
            .collection("users")
            .document(uid)
            .collection("cart")
    }

    // Real-time cart stream
    fun getCartItems(): Flow<Resource<List<CartItem>>> = callbackFlow {
        val ref = cartRef() ?: run {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Cart error"))
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)
            } ?: emptyList()

            trySend(Resource.Success(items))
        }

        awaitClose { listener.remove() }
    }

    suspend fun addToCart(item: CartItem): Resource<Unit> {
        return try {
            // Use productId as document ID
            // If product already in cart, this overwrites it
            cartRef()
                ?.document(item.productId)
                ?.set(item)
                ?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add to cart")
        }
    }

    suspend fun updateQuantity(
        productId: String,
        quantity: Int
    ): Resource<Unit> {
        return try {
            if (quantity <= 0) {
                // Remove item if quantity reaches 0
                removeFromCart(productId)
            } else {
                cartRef()
                    ?.document(productId)
                    ?.update("quantity", quantity)
                    ?.await()
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }

    suspend fun removeFromCart(productId: String): Resource<Unit> {
        return try {
            cartRef()
                ?.document(productId)
                ?.delete()
                ?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Remove failed")
        }
    }

    suspend fun clearCart(): Resource<Unit> {
        return try {
            val docs = cartRef()?.get()?.await()?.documents
                ?: return Resource.Success(Unit)
            docs.forEach { it.reference.delete().await() }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Clear cart failed")
        }
    }
}