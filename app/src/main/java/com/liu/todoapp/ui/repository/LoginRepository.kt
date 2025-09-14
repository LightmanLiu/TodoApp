package com.liu.todoapp.ui.repository

import com.liu.todoapp.ui.model.User
import com.liu.todoapp.ui.network.ApiResult
import com.liu.todoapp.ui.network.LoginApiService

interface LoginRepository{
    suspend fun login(account: String, password: String): ApiResult<User>
    suspend fun register(account: String, password: String, verifyPwd: String): ApiResult<User>
}