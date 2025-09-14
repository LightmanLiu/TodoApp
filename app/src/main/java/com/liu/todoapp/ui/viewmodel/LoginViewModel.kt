package com.liu.todoapp.ui.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liu.todoapp.ui.MyApp
import com.liu.todoapp.ui.model.User
import com.liu.todoapp.ui.repository.LoginRepository
import com.liu.todoapp.ui.repository.UserDBRepository
import com.liu.todoapp.ui.util.ToDoDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(val isLogin: Boolean, val isRegister: Boolean, var curUser: User? = null, var loginDialog: LoginDialog? = null)
data class LoginDialog(val showDialog: Boolean, val dialogMessage: String)
data class LoginDBState(val insertResult: Long, val queryResult: User?, val deleteResult: Int, val updateResult: Int)

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val TAG: String = "LoginViewModel"

    private val repository = UserDBRepository(MyApp.db.loginDao())

    private val state = MutableStateFlow<LoginState>(LoginState(false,false))
    val _state: StateFlow<LoginState> = state

    private val loginDBState = MutableStateFlow<LoginDBState>(LoginDBState(0,null,0,0))
    val _loginDBState: StateFlow<LoginDBState> = loginDBState

    private val navigationEvent = MutableSharedFlow<ToDoDestination>()
    val _navigationEvent: SharedFlow<ToDoDestination> = navigationEvent

    private val toastEvent = MutableSharedFlow<String>()
    val _toastEvent: SharedFlow<String> = toastEvent

    fun toLogin(account: String, password: String) {
        viewModelScope.launch {
            if (_state.value.isLogin || _state.value.isRegister) {
                toastEvent.emit("You've already clicked and in-progress, please wait!")
            } else if (account.isEmpty()) {
                toastEvent.emit("Account cannot be empty!")
            } else if (password.isEmpty()) {
                toastEvent.emit("password cannot be empty!")
            } else {
                state.update { state ->
                    state.copy(
                        isLogin = true
                    )
                }

                try {
                    val result = loginRepository.login(account,password)
                    if (result.code == 200) {
                        state.update { state ->
                            state.copy(
                                curUser = result.data
                            )
                        }
                        navigationEvent.emit(ToDoDestination.ToDoSC)
                    } else {
                        //different results: account not exists, wrong password, code should be different as well
                        //in here us 'account not exists' as example
                        toastEvent.emit("account not exists")
                    }
                } catch (e: Exception) {
                    Log.e(TAG,e.toString())
                } finally {
                    state.update { state ->
                        state.copy(
                            isLogin = false
                        )
                    }
                }
            }
        }
    }

    fun toRegister(account: String, password: String, verifyPwd: String) {
        viewModelScope.launch {
            if (!_state.value.isRegister) {
                if (account.isEmpty()) {
                    toastEvent.emit("Account cannot be empty!")
                } else if (password.isEmpty()) {
                    toastEvent.emit("password cannot be empty!")
                } else if (password != verifyPwd) {
                    toastEvent.emit("Two passwords are not the same, please check!")
                } else {
                    state.update { state ->
                        state.copy(
                            isRegister = true
                        )
                    }

                    try {
                        val result = loginRepository.register(account,password,verifyPwd)
                        if (result.code == 200) {
                            state.update { state ->
                                state.copy(
                                    curUser = result.data
                                )
                            }
                            navigationEvent.emit(ToDoDestination.ToDoSC)
                        } else {
                            //show the corresponding toast messages
                            toastEvent.emit("register error!")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,e.toString())
                    } finally {
                        state.update { state ->
                            state.copy(
                                isRegister = false
                            )
                        }
                    }
                }
            } else {
                toastEvent.emit("too many times tried to register in a short-time!")
            }
        }
    }

    fun  resetStateValue() {
        state.update { state ->
            state.copy(isLogin = false,
                isRegister = false,
                loginDialog = state.loginDialog?.copy(
                    showDialog = false,
                    dialogMessage = ""
                ))
        }
    }


    fun resetDBValue() {
        loginDBState.update { state ->
            state.copy(insertResult = 0,
                queryResult = null,
                deleteResult = 0,
                updateResult = 0)
        }
    }


//    fun initUserDetect(account: String) {
//        viewModelScope.launch {
//            repository.observeCurUserInfo(account).collect { user ->
//                loginDBState.update { it.copy(queryResult = user) }
//            }
//        }
//    }

    fun loginOfflineMode(account: String, pwd: String) {
        viewModelScope.launch {
            val res: User? = repository.observeCurUserInfo(account).firstOrNull()

            if (res != null) {
                if (res.pwd == pwd) {
                    state.update { state ->
                        state.copy(isLogin = true)
                    }
                    navigationEvent.emit(ToDoDestination.ToDoSC)
                } else {
                    toastEvent.emit("Your password is not correct!")
                }
            } else {
                state.update { state ->
                    state.copy(isRegister = true)
                }
                toastEvent.emit("The account is not exist, please register!")
            }
        }
    }

    fun updateUserInfo(user: User) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            if (result > 0) {
                loginDBState.update { state ->
                    state.copy(
                        updateResult = result
                    )
                }
            }
        }
    }

    fun deleteUserInfo(user: User) {
        viewModelScope.launch {
            val result = repository.deleteUser(user)
            if (result > 0) {
                loginDBState.update { state ->
                    state.copy(deleteResult = result)
                }
            }
        }
    }

    fun cleanAllStateForDeleting() {
        loginDBState.update { state ->
            state.copy(0,null,0,0)
        }
        state.update { state ->
            state.copy(false,false,null,null)
        }
    }

    fun insertUserInfo(user: User,forceCheckDB: Boolean = false) {
        viewModelScope.launch {
            val existingUser = if (forceCheckDB) {
                repository.observeCurUserInfo(user.account).firstOrNull()
            } else {
                _loginDBState.value.queryResult
            }

            if (existingUser != null) {
                toastEvent.emit("The account already exists!")
            } else {
                val result = repository.insertUser(user)
                loginDBState.update {
                    it.copy(insertResult = result)
                }
                state.update { state ->
                    state.copy(isRegister = false, isLogin = true)
                }
                navigationEvent.emit(ToDoDestination.ToDoSC)
                resetStateValue()
            }
        }
    }

    fun dismissDialog() {
        state.update { state ->
            state.copy(
                loginDialog = state.loginDialog?.copy(
                    showDialog = false
                )
            )
        }
    }

    fun updateDialogMessage(message: String) {
        state.update { state ->
            state.copy(
                loginDialog = state.loginDialog?.copy(
                    showDialog = true,
                    dialogMessage = message
                )
            )
        }
    }
}