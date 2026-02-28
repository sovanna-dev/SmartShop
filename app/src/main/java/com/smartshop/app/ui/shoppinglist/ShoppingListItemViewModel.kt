package com.smartshop.app.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.data.model.Resource
import com.smartshop.app.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListItemViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _items = MutableStateFlow<Resource<List<ShoppingListItem>>>(Resource.Loading)
    val items: StateFlow<Resource<List<ShoppingListItem>>> = _items

    private val _addState = MutableStateFlow<Resource<Unit>?>(null)
    val addState: StateFlow<Resource<Unit>?> = _addState

    private val _actionState = MutableStateFlow<Resource<Unit>?>(null)
    val actionState: StateFlow<Resource<Unit>?> = _actionState

    private var currentListId: String = ""

    fun loadItems(listId: String) {
        currentListId = listId
        viewModelScope.launch {
            repository.getListItems(listId).collect { _items.value = it }
        }
    }

    fun addItem(item: ShoppingListItem) {
        viewModelScope.launch {
            _addState.value = Resource.Loading
            _addState.value = repository.addItemToList(currentListId, item)
        }
    }

    fun toggleItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleItemChecked(currentListId, itemId, isChecked)
        }
    }

    fun removeItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            _actionState.value = repository.removeItem(currentListId, itemId, isChecked)
        }
    }

    fun resetAddState() { _addState.value = null }

    fun resetActionState() { _actionState.value = null }
}