package com.liu.todoapp.ui.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liu.todoapp.ui.model.Todo
import com.liu.todoapp.ui.model.User

@Database(entities = [Todo::class, User::class], version = 1, exportSchema = false)
abstract class RoomDB: RoomDatabase() {
    abstract fun todoDao(): ToDoDao
    abstract fun loginDao(): LoginDao
}