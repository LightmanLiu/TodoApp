package com.liu.todoapp.ui.network

import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApiService {
    @POST("api/login")
    suspend fun login(
        @Query("account")account: String = "",
        @Query("password")password: String = ""
    ): ResponseBody

    @POST("api/register")
    suspend fun register(
        @Query("account")account: String = "",
        @Query("password")password: String = "",
        @Query("verifyPassword")verifyPwd: String = ""
    ): ResponseBody
}