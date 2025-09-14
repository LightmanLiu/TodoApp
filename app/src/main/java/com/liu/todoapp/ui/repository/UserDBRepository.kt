package com.liu.todoapp.ui.repository

import com.liu.todoapp.ui.database.LoginDao
import com.liu.todoapp.ui.model.User

class UserDBRepository(private val loginDao: LoginDao) {
    suspend fun insertUser(user: User): Long =  loginDao.insertUser(user)
    suspend fun updateUser(user: User): Int = loginDao.updateUser(user)
    suspend fun deleteUser(user: User): Int = loginDao.deleteUser(user)
    fun observeCurUserInfo(account: String) = loginDao.observeCurUserInfo(account)
}