package com.liu.todoapp.ui.util


sealed class ToDoDestination(val route: String, val label: String) {

    object LoginSC: ToDoDestination(
        route = "LoginSC",
        label = "LoginSC")

    object ToDoSC: ToDoDestination(
        route = "ToDoSC",
        label = "ToDoSC")

    companion object {
        val entries: List<ToDoDestination> by lazy {
            listOf(LoginSC, ToDoSC)
        }
    }
}