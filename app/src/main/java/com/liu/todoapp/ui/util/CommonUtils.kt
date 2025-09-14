package com.liu.todoapp.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liu.todoapp.ui.network.ApiResult
import com.liu.todoapp.ui.repository.LoginRepository
import com.liu.todoapp.ui.repository.ToDoRepository
import com.liu.todoapp.ui.viewmodel.LoginViewModel
import com.liu.todoapp.ui.viewmodel.ToDoViewModel

inline fun <reified T> parseApiResult(json: String): ApiResult<T>{
    val type = object : TypeToken<ApiResult<T>>() {}.type
    return Gson().fromJson(json,type)
}

class LoginViewModelFactory(
    private val repository: LoginRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ToDoViewModelFactory(
    private val repository: ToDoRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}