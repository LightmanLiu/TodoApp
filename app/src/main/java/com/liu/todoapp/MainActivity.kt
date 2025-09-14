package com.liu.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.liu.todoapp.ui.screens.LoginScreen
import com.liu.todoapp.ui.screens.ToDoScreen
import com.liu.todoapp.ui.theme.TodoAppTheme
import com.liu.todoapp.ui.util.NavControllerManager
import com.liu.todoapp.ui.util.ToDoDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    navigationLeadInfo(innerPadding)
                }
            }
        }
    }
}

@Composable
fun navigationLeadInfo(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        NavControllerManager.init(navController)
    }
    NavHost(navController = navController,
        startDestination = ToDoDestination.LoginSC.route) {
        ToDoDestination.entries.forEach { destination ->
            composable(destination.route) {
                when(destination.route) {
                    ToDoDestination.LoginSC.route -> LoginScreen(paddingValues)
                    ToDoDestination.ToDoSC.route -> ToDoScreen(paddingValues)
                }
            }
        }
    }
}