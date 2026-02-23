package com.smartshop.app.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartshop.app.data.model.ShoppingList
import com.smartshop.app.data.model.ShoppingListItem
import com.smartshop.app.data.repository.ShoppingListRepository
import com.smartshop.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _lists = MutableStateFlow<Resource<List<ShoppingList>>>(Resource.Loading)
    val lists: StateFlow<Resource<List<ShoppingList>>> = _lists

    private val _createState = MutableStateFlow<Resource<String>?>(null)
    val createState: StateFlow<Resource<String>?> = _createState

    private val _deleteState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteState: StateFlow<Resource<Unit>?> = _deleteState

    init {
        loadLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            repository.getShoppingLists().collect { _lists.value = it }
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            _createState.value = repository.createList(name)
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            _deleteState.value = repository.deleteList(listId)
        }
    }

    fun resetCreateState() { _createState.value = null }
}