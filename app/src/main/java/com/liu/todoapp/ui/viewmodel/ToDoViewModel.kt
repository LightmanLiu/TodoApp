package com.liu.todoapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liu.todoapp.ui.MyApp
import com.liu.todoapp.ui.model.Todo
import com.liu.todoapp.ui.repository.ToDoRepository
import com.liu.todoapp.ui.repository.TodoDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ToDoState(val isRefresh: Boolean, val isLoadingMore: Boolean, val lists: List<Todo> = emptyList<Todo>())

@HiltViewModel
class ToDoViewModel @Inject constructor(private val todoRepo: ToDoRepository): ViewModel() {

    private val TAG: String = "ToDoViewModel"

    private val repository = TodoDBRepository(MyApp.db.todoDao())

    val fakeTodos = listOf(
        Todo(id = 1, title = "Buy groceries", details = "Milk, Eggs, Bread, Fruits", isFavorite = false),
        Todo(id = 2, title = "Read a book", details = "Finish reading 'Clean Code'", isFavorite = true),
        Todo(id = 3, title = "Workout", details = "30 minutes cardio + strength training", isFavorite = false),
        Todo(id = 4, title = "Call mom", details = "Check how she's doing", isFavorite = true),
        Todo(id = 5, title = "Learn Kotlin", details = "Complete Compose basics tutorial", isFavorite = false),
        Todo(id = 6, title = "Plan weekend trip", details = "Decide destination and book hotel", isFavorite = false)
    )

    private val todoState = MutableStateFlow<ToDoState>(ToDoState(false,false))
    val _todoState: StateFlow<ToDoState> = todoState.asStateFlow()

    private val toastMsg = MutableSharedFlow<String>()
    val _toastMsg: SharedFlow<String> = toastMsg.asSharedFlow()

    init {
        observeTodoLists()
        insertOfflineData()
    }

    fun getList(page: Int, count: Int, refreshOrLoading: Boolean) {
        viewModelScope.launch {
            if (refreshOrLoading) {
                todoState.update { state ->
                    state.copy(isRefresh = true)
                }
            } else {
                todoState.update { state ->
                    state.copy(isLoadingMore = true)
                }
            }

            try {
                val result = todoRepo.getLists(page,count)
                if (result.code == 200) {

                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG,e.toString())
            } finally {
                todoState.update { state ->
                    state.copy(isRefresh = false,isLoadingMore = false)
                }
            }
        }
    }

    fun addItemToList(itemJson: Todo) {
        viewModelScope.launch {
            try {
                if (!todoState.value.lists.contains(itemJson)) {
                    val result = todoRepo.addItemToList(itemJson)
                    if (result.code == 200) {

                    } else {

                    }
                } else {
                    //show toast message
                    toastMsg.emit("the item already been added!")
                }
            } catch (e: Exception) {
                Log.e(TAG,e.toString())
            }
        }
    }

    fun changeValueFavorite(item: Todo) {
        viewModelScope.launch {

            try {
                val sbmToDo = item.copy(isFavorite = !item.isFavorite)
                val result = todoRepo.changeItemFavorite(sbmToDo)
                if (result.code == 200) {

                } else {

                }
            } catch (e: Exception) {
                Log.d(TAG,e.toString())
            }
        }
    }

    fun observeTodoLists() {
        viewModelScope.launch {
            repository.getAllTodos()
                .distinctUntilChanged{ old, new -> old == new}
                .collect { lists ->
                todoState.update { it.copy(lists = lists) }
            }
        }
    }

    fun getTodosOffline() {
        viewModelScope.launch {
            todoState.update {it.copy(isRefresh = true) }

            repository.clearAll()

            fakeTodos.forEach { item ->
                repository.insert(item)
            }
            todoState.update { it.copy(isRefresh = false) }
        }
    }

    fun addTodoOffline(todo: Todo) {
        viewModelScope.launch {
            if (todo.title.isEmpty()) {
                toastMsg.emit("title cannot be empty")
            } else {
                val addItem = todo.copy(id = (fakeTodos.maxOfOrNull { it.id } ?: 0) + 1)
                val result = repository.insert(addItem)
                if (result > 0) {
                    toastMsg.emit("Add ${addItem.title} succeed!")
                }
            }
        }
    }

    fun deleteTodoOffline(todo: Todo) {
        viewModelScope.launch {
            val result = repository.delete(todo)
            if (result > 0) {
                toastMsg.emit("delete item ${todo.title} succeed!")
            }
        }
    }

    fun changeCurrentTodoLikeOffline(todo: Todo) {
        viewModelScope.launch {
            val data = todo.copy(isFavorite = !todo.isFavorite)
            val res = repository.update(data)
            if (res > 0) {
                toastMsg.emit("update item ${todo.title} succeed!")
            }
        }
    }

    fun loadMoreOffline() {
        viewModelScope.launch {
            todoState.update { it ->
                it.copy(isLoadingMore = true)
            }
            //currently only loading more once because fakeTodos's size keep stay in 6, after the first time always update the lists
            fakeTodos.forEach { item ->
                val newItem = item.copy(id = fakeTodos.size + item.id)
                repository.insert(newItem)
            }
            todoState.update { it ->
                it.copy(isLoadingMore = false)
            }
        }
    }

    fun updateTodoOffline(todo: Todo) {
        viewModelScope.launch {
            val res = repository.update(todo)
            if (res > 0) {
                toastMsg.emit("update item ${todo.title} succeed!")
            }
        }
    }

    fun insertOfflineData() {
        viewModelScope.launch {
            fakeTodos.forEach { item ->
                repository.insert(item)
            }
        }
    }
}