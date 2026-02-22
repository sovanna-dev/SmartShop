package com.smartshop.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.smartshop.app.data.model.Address
import com.smartshop.app.data.model.CartItem
import com.smartshop.app.data.model.Order
import com.smartshop.app.data.model.OrderStatus
import com.smartshop.app.data.model.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun placeOrder(
        items: List<CartItem>,
        totalPrice: Double,
        shippingAddress: Address
    ): Resource<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Resource.Error("User not logged in")

            // Create new order document
            val orderRef = firestore.collection("orders").document()

            val order = Order(
                id = orderRef.id,
                userId = userId,
                items = items,
                totalPrice = totalPrice,
                shippingAddress = shippingAddress,
                status = OrderStatus.PENDING,
                timestamp = Timestamp.now()
            )

            orderRef.set(order).await()

            Resource.Success(orderRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to place order")
        }
    }

    // Get all orders for current user — real time
    fun getUserOrders(): Flow<Resource<List<Order>>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading)

        val listener = firestore
            .collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error loading orders"))
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(Resource.Success(orders))
            }

        awaitClose { listener.remove() }
    }
}