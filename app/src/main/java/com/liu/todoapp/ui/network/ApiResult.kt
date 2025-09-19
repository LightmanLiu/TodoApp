package com.liu.todoapp.ui.network

data class ApiResult<T> (
    val code: Int,
    val message: String? = null,
    val data: T? = null
)