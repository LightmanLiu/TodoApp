package com.liu.todoapp.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(@PrimaryKey(autoGenerate = true) val user_id: Int,
                val account: String,
                val pwd: String,
                val username: String)
