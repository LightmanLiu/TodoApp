package com.liu.todoapp.ui.repository

import com.liu.todoapp.ui.model.Todo
import com.liu.todoapp.ui.network.ApiResult

interface ToDoRepository {
    suspend fun getLists(page: Int, count: Int): ApiResult<List<Todo>>
    suspend fun addItemToList(itemJson: Todo): ApiResult<Unit>
    suspend fun changeItemFavorite(itemJson: Todo): ApiResult<Unit>
}