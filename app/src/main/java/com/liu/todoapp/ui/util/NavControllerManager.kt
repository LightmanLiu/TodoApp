package com.liu.todoapp.ui.util

import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

object NavControllerManager {

    private lateinit var navController: NavHostController

    fun init(navController: NavHostController) {
        this.navController = navController
    }

    fun  navigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        try {
            navController.navigate(route, builder)
        } catch (e: IllegalArgumentException) {
            Log.e("NavControllerManager","Navigation failed :" + e.message)
        }
    }
}