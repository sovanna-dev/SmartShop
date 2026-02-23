package com.smartshop.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartshop.app.data.model.ShoppingList
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun listsRef() = firestore
        .collection("users")
        .document(auth.currentUser!!.uid)
        .collection("shoppingLists")

    private fun itemsRef(listId: String) = listsRef()
        .document(listId)
        .collection("items")

    // Get all shopping lists — real-time
    fun getShoppingLists(): Flow<Resource<List<ShoppingList>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = listsRef()
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val lists = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(lists))
            }

        awaitClose { listener.remove() }
    }

    // Get items in a list — real-time
    fun getListItems(listId: String): Flow<Resource<List<ShoppingListItem>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = itemsRef(listId)
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ShoppingListItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(items))
            }

        awaitClose { listener.remove() }
    }

    // Create a new list
    suspend fun createList(name: String): Resource<String> {
        return try {
            val list = hashMapOf(
                "name" to name,
                "createdAt" to Timestamp.now(),
                "totalItems" to 0,
                "checkedItems" to 0
            )
            val doc = listsRef().add(list).await()
            Resource.Success(doc.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create list")
        }
    }

    // Add item to list
    suspend fun addItemToList(
        listId: String,
        item: ShoppingListItem
    ): Resource<Unit> {
        return try {
            val itemMap = hashMapOf(
                "productId" to item.productId,
                "name" to item.name,
                "quantity" to item.quantity,
                "price" to item.price,
                "imageUrl" to item.imageUrl,
                "isChecked" to false,
                "addedAt" to Timestamp.now()
            )
            itemsRef(listId).add(itemMap).await()
            // Update total items count
            listsRef().document(listId)
                .update("totalItems", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add item")
        }
    }

    // Toggle item checked state
    suspend fun toggleItemChecked(
        listId: String,
        itemId: String,
        isChecked: Boolean
    ): Resource<Unit> {
        return try {
            itemsRef(listId).document(itemId)
                .update("isChecked", isChecked)
                .await()
            // Update checked count
            val increment = if (isChecked) 1L else -1L
            listsRef().document(listId)
                .update(
                    "checkedItems",
                    com.google.firebase.firestore.FieldValue.increment(increment)
                )
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    // Remove item from list
    suspend fun removeItem(listId: String, itemId: String): Resource<Unit> {
        return try {
            itemsRef(listId).document(itemId).delete().await()
            listsRef().document(listId)
                .update(
                    "totalItems",
                    com.google.firebase.firestore.FieldValue.increment(-1)
                )
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove item")
        }
    }

    // Delete entire list
    suspend fun deleteList(listId: String): Resource<Unit> {
        return try {
            listsRef().document(listId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete list")
        }
    }
}