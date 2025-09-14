package com.liu.todoapp.ui.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ToDoApiService {
    @GET("api/getList")
    suspend fun getList(
        @Query("page") page: Int,
        @Query("count") count: Int
    ): ResponseBody

    @POST("api/addItem")
    suspend fun addItemToList(
        @Query("item") json: String
    ): ResponseBody

    @POST("api/changeItemValue")
    //each item should has a unique id, in there keep send json for convenience
    suspend fun changeItemValue(
        @Query("item") json: String
    ): ResponseBody
}