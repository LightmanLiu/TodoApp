package com.liu.todoapp.ui.repository

import com.liu.todoapp.ui.database.ToDoDao
import com.liu.todoapp.ui.model.Todo
import javax.inject.Inject

class TodoDBRepository @Inject constructor(private val todoDao: ToDoDao) {
    fun getAllTodos() = todoDao.getAllTodos()
    suspend fun insert(todo: Todo): Long = todoDao.insert(todo)
    suspend fun update(todo: Todo): Int = todoDao.update(todo)
    suspend fun delete(todo: Todo): Int = todoDao.delete(todo)
    suspend fun clearAll() = todoDao.clearAll()
}