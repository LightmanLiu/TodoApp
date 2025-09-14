package com.liu.todoapp.ui.network

import com.google.gson.annotations.SerializedName

data class ApiResult<T> (
    val code: Int,
    val message: String? = null,
    val data: T? = null
)