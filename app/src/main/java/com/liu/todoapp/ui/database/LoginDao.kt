package com.liu.todoapp.ui.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liu.todoapp.ui.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User): Int

    @Delete
    suspend fun deleteUser(user: User): Int

    @Query("SELECT * FROM user_table WHERE account = :name LIMIT 1")
    fun observeCurUserInfo(name: String): Flow<User?>
}