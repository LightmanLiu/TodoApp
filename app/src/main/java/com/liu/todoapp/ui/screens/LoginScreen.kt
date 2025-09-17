package com.liu.todoapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liu.todoapp.ui.model.User
import com.liu.todoapp.ui.network.LoginApiService
import com.liu.todoapp.ui.repository.LoginRepositoryImpl
import com.liu.todoapp.ui.util.LoginViewModelFactory
import com.liu.todoapp.ui.util.NavControllerManager
import com.liu.todoapp.ui.viewmodel.LoginDialog
import com.liu.todoapp.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun LoginScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current

    val api = remember {
        Retrofit.Builder()
            .baseUrl("http://xxxx.xxxx.xxxx/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginApiService::class.java)
    }

    val repository = remember { LoginRepositoryImpl(api) }

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(repository)
    )

//    val loginState by loginViewModel._state.collectAsState()
    val isRegister by loginViewModel._state.map { it.isRegister }.collectAsStateWithLifecycle(false)
    val dialogState by loginViewModel._state.map { it.loginDialog }.collectAsStateWithLifecycle(null)

    var account by rememberSaveable { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var verifyPwd by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        //need to guarantee the crash from any coroutine function won't influence the others, so use 'supervisorScope'
        //or val scope = CoroutineScope(SupervisorJob() + coroutineContext)
        supervisorScope {
            launch {
                loginViewModel._navigationEvent.collect { route ->
                    NavControllerManager.navigate(route.route)
                    loginViewModel.resetStateValue()
                    loginViewModel.resetDBValue()
                }
            }

            launch {
                loginViewModel._toastEvent.collect { toastMsg ->
                    Toast.makeText(context,toastMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        TextField(
            value = account,
            onValueChange = { account = it },
            singleLine = true,
            label = { Text(text = "Account") },
            modifier = Modifier.height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        PasswordView(pwd,"Password",{pwd = it})
        Spacer(modifier = Modifier.height(10.dp))

        AnimatedVisibility(
            visible = isRegister,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                PasswordView(verifyPwd, "Verify Password") { verifyPwd = it }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        LoginRegisterButton(
            isRegister = isRegister,
            onLogin = { loginViewModel.loginOfflineMode(account, pwd) },
            onRegister = {
                val user = User(0, account, pwd, account)
                loginViewModel.insertUserInfo(user)
            }
        )
    }

    ShowDialog(dialogState,loginViewModel)
}

@Composable
fun LoginRegisterButton(
    isRegister: Boolean,
    onLogin: () -> Unit,
    onRegister: () -> Unit) {
    AnimatedContent(targetState = isRegister) { isRegister ->
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .height(60.dp)
                .padding(start = 10.dp, end = 10.dp)
        ) {
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .weight( 1f)
                    .fillMaxHeight()
            ) {
                Text(text = "Login",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ))
            }
            if (isRegister) {
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(text = "Register",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ))
                }
            }
        }
    }
}

@Composable
fun PasswordView(pwd: String, label: String,
                 onPasswordChange: (String) -> Unit) {

    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = pwd,
        onValueChange = onPasswordChange,
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            val image = if (passwordVisible) {
                Icons.Default.Check //cannot find visibility icon, use check and close to replace
            } else {
                Icons.Default.Close
            }

            IconButton(onClick = {passwordVisible = !passwordVisible}) {
                Icon(imageVector = image, contentDescription = null)
            }
        },
        label = { Text(text = label)},
        modifier = Modifier.height(50.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Composable
fun ShowDialog(loginDialog: LoginDialog?, loginViewModel: LoginViewModel) {
    if (loginDialog?.showDialog == true) {
        AlertDialog(
            onDismissRequest = {loginViewModel.dismissDialog()},
            confirmButton = {
                TextButton(
                    onClick = {loginViewModel.dismissDialog()}
                ) {
                    Text("Confirm")
                }
            },
            title = { Text("Attention")},
            text = {Text(loginDialog.dialogMessage)}
        )
    }
}