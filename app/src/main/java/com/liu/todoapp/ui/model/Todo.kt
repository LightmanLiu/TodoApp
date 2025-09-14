package com.liu.todoapp.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(@PrimaryKey(autoGenerate = true) val id: Int,
                val title: String,
                val details: String,
                val isFavorite: Boolean = false)
