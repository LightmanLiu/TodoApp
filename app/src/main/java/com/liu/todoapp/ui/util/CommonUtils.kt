package com.liu.todoapp.ui.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liu.todoapp.ui.network.ApiResult

inline fun <reified T> parseApiResult(json: String): ApiResult<T>{
    val type = object : TypeToken<ApiResult<T>>() {}.type
    return Gson().fromJson(json,type)
}