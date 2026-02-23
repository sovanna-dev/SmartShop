package com.smartshop.app.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.data.repository.ShoppingListRepository
import com.smartshop.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _items = MutableStateFlow<Resource<List<ShoppingListItem>>>(Resource.Loading)
    val items: StateFlow<Resource<List<ShoppingListItem>>> = _items

    private val _addState = MutableStateFlow<Resource<Unit>?>(null)
    val addState: StateFlow<Resource<Unit>?> = _addState

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

    fun toggleChecked(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleItemChecked(currentListId, itemId, isChecked)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            repository.removeItem(currentListId, itemId)
        }
    }

    fun resetAddState() { _addState.value = null }
}