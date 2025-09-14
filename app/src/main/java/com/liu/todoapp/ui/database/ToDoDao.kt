package com.liu.todoapp.ui.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liu.todoapp.ui.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: Todo): Long

    @Update
    suspend fun update(todo: Todo): Int

    @Delete
    suspend fun delete(todo: Todo): Int

    @Query("DELETE FROM todo_table")
    suspend fun clearAll()

    @Query("SELECT * FROM todo_table ORDER BY id DESC")
    fun getAllTodos(): Flow<List<Todo>>
}