package com.liu.todoapp.ui.repository

import com.liu.todoapp.ui.model.User
import com.liu.todoapp.ui.network.ApiResult
import com.liu.todoapp.ui.network.LoginApiService
import com.liu.todoapp.ui.util.parseApiResult
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(private val loginApiService: LoginApiService): LoginRepository {
    override suspend fun login(
        account: String,
        password: String
    ): ApiResult<User> {
        try {
            val result = loginApiService.login(account,password)
            val json = result.string()
            return parseApiResult<User>(json)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun register(
        account: String,
        password: String,
        verifyPwd: String
    ): ApiResult<User> {
        try {
            val result = loginApiService.register(account, password, verifyPwd)
            val json = result.string()
            return parseApiResult<User>(json)
        } catch (e: Exception) {
            throw e
        }
        TODO("Not yet implemented")
    }
}