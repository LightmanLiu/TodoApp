package com.liu.todoapp.ui.repository

import com.google.gson.Gson
import com.liu.todoapp.ui.model.Todo
import com.liu.todoapp.ui.network.ApiResult
import com.liu.todoapp.ui.network.ToDoApiService
import com.liu.todoapp.ui.util.parseApiResult
import javax.inject.Inject

class ToDoRepositoryImpl @Inject constructor(private val todoApi: ToDoApiService): ToDoRepository {
    override suspend fun getLists(page: Int, count: Int): ApiResult<List<Todo>> {
        try {
            val result = todoApi.getList(page,count)
            val json = result.string()
            return parseApiResult<List<Todo>>(json);
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addItemToList(itemJson: Todo): ApiResult<Unit> {
        try {
            val result = todoApi.addItemToList(Gson().toJson(itemJson))
            val json = result.string()
            return parseApiResult<Unit>(json)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun changeItemFavorite(itemJson: Todo): ApiResult<Unit> {
        try {
            val result = todoApi.changeItemValue(Gson().toJson(itemJson))
            val json = result.string()
            return parseApiResult<Unit>(json)
        } catch (e: Exception) {
            throw e
        }
    }
}