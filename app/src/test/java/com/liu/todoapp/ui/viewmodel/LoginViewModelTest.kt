package com.liu.todoapp.ui.viewmodel

import com.liu.todoapp.ui.model.User
import com.liu.todoapp.ui.network.ApiResult
import com.liu.todoapp.ui.repository.LoginRepository
import com.liu.todoapp.ui.repository.UserDBRepository
import com.liu.todoapp.ui.util.ToDoDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @Mock
    private lateinit var mockLoginRepository: LoginRepository
    @Mock
    private lateinit var mockUserDBRepository: UserDBRepository

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        // Create a minimal ViewModel that avoids Android dependencies for testing
        viewModel = LoginViewModel(mockLoginRepository,mockUserDBRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have correct default values`() = runTest {
        val state = viewModel._state.value
        assertFalse(state.isLogin)
        assertFalse(state.isRegister)
        assertNull(state.curUser)
        assertNull(state.loginDialog)
    }

    @Test
    fun `toLogin should emit toast when account is empty`() = runTest {
        viewModel.toLogin("", "password")
        advanceUntilIdle()

        val toastMessage = viewModel._toastEvent.first()
        assertEquals("Account cannot be empty!", toastMessage)
    }

    @Test
    fun `toLogin should emit toast when password is empty`() = runTest {
//        viewModel.toLogin("account", "")
        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        //Currently, only offline mode can be used, so use the below one to instead of
        viewModel.loginOfflineMode("account","")
        advanceUntilIdle()

        job.cancel()
        assertEquals("password cannot be empty!", toastMessages.first())
    }

    @Test
    fun `toLogin should emit toast when already in progress`() = runTest {
        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.setLoginInProgress(true)
        viewModel.toLogin("account", "password")
        advanceUntilIdle()

        job.cancel()
        assertEquals("You've already clicked and in-progress, please wait!", toastMessages.first())
    }

    @Test
    fun `toLogin should navigate to ToDoScreen on successful login`() = runTest {
        val user = User(1, "account", "password", "username")
        val successResult = ApiResult<User>(200, "Success", user)
        whenever(mockLoginRepository.login("account", "password")).thenReturn(successResult)

        val dsCollection = mutableListOf<ToDoDestination>()
        val job = launch {
            viewModel._navigationEvent.collect { it ->
                dsCollection.add(it)
            }
        }

        viewModel.toLogin("account", "password")
        advanceUntilIdle()

        val state = viewModel._state.value
        assertEquals(user, state.curUser)
        assertFalse(state.isLogin)

        job.cancel()
        assertEquals(ToDoDestination.ToDoSC, dsCollection.first())
    }

    @Test
    fun `toLogin should emit toast on API failure`() = runTest {
        val failureResult = ApiResult<User>(404, "Account not found", null)
        whenever(mockLoginRepository.login("account", "password")).thenReturn(failureResult)

        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.toLogin("account", "password")
        advanceUntilIdle()

        job.cancel()
        assertEquals("account not exists", toastMessages.first())

        val state = viewModel._state.value
        assertFalse(state.isLogin)
    }

    @Test
    fun `toLogin should handle exceptions gracefully`() = runTest {
        whenever(mockLoginRepository.login("account", "password")).thenThrow(RuntimeException("Network error"))

        viewModel.toLogin("account", "password")
        advanceUntilIdle()

        val state = viewModel._state.value
        assertFalse(state.isLogin)
    }

    @Test
    fun `toRegister should emit toast when account is empty`() = runTest {
        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.toRegister("", "password", "password")
        advanceUntilIdle()

        job.cancel()
        assertEquals("Account cannot be empty!", toastMessages.first())
    }

    @Test
    fun `toRegister should emit toast when password is empty`() = runTest {
        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.toRegister("account", "", "")
        advanceUntilIdle()

        job.cancel()
        assertEquals("password cannot be empty!", toastMessages.first())
    }

    @Test
    fun `toRegister should emit toast when passwords don't match`() = runTest {
        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.toRegister("account", "password1", "password2")
        advanceUntilIdle()

        job.cancel()
        assertEquals("Two passwords are not the same, please check!", toastMessages.first())
    }

    @Test
    fun `toRegister should navigate to ToDoScreen on successful registration`() = runTest {
        val user = User(1, "account", "password", "username")
        val successResult = ApiResult<User>(200, "Success", user)
        whenever(mockLoginRepository.register("account", "password", "password")).thenReturn(successResult)

        val dsCollection = mutableListOf<ToDoDestination>()
        val job = launch {
            viewModel._navigationEvent.collect { it ->
                dsCollection.add(it)
            }
        }

        viewModel.toRegister("account", "password", "password")
        advanceUntilIdle()

        val state = viewModel._state.value
        assertEquals(user, state.curUser)
        assertFalse(state.isRegister)

        job.cancel()
        assertEquals(ToDoDestination.ToDoSC, dsCollection.first())
    }

    @Test
    fun `toRegister should emit toast on API failure`() = runTest {
        val failureResult = ApiResult<User>(400, "Registration failed", null)
        whenever(mockLoginRepository.register("account", "password", "password")).thenReturn(failureResult)

        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        viewModel.toRegister("account", "password", "password")
        advanceUntilIdle()

        job.cancel()
        assertEquals("register error!", toastMessages.first())

        val state = viewModel._state.value
        assertFalse(state.isRegister)
    }

    @Test
    fun `toRegister should emit toast when already registering`() = runTest {
        val user = User(1, "account", "password", "username")
        val successResult = ApiResult<User>(200, "Success", user)
        whenever(mockLoginRepository.register("account", "password", "password")).thenReturn(successResult)

        val toastMessages = mutableListOf<String>()
        val job = launch {
            viewModel._toastEvent.collect { message ->
                toastMessages.add(message)
            }
        }

        //the same as Login(), use offline mode to instead of
        viewModel.setRegisterInProgress(true)
        viewModel.insertUserInfo(user)
//        // First call to set register in progress
//        viewModel.toRegister("account", "password", "password")
        advanceUntilIdle()

        job.cancel()
        assertEquals("too many times tried to register in a short-time!", toastMessages.first())
    }

    @Test
    fun `resetStateValue should reset state values`() = runTest {
        viewModel.resetStateValue()

        val state = viewModel._state.value
        assertFalse(state.isLogin)
        assertFalse(state.isRegister)
    }

    @Test
    fun `resetDBValue should reset DB state values`() = runTest {
        viewModel.resetDBValue()

        val dbState = viewModel._loginDBState.value
        assertEquals(0, dbState.insertResult)
        assertNull(dbState.queryResult)
        assertEquals(0, dbState.deleteResult)
        assertEquals(0, dbState.updateResult)
    }

    @Test
    fun `cleanAllStateForDeleting should reset all states`() = runTest {
        viewModel.cleanAllStateForDeleting()

        val state = viewModel._state.value
        assertFalse(state.isLogin)
        assertFalse(state.isRegister)
        assertNull(state.curUser)
        assertNull(state.loginDialog)

        val dbState = viewModel._loginDBState.value
        assertEquals(0, dbState.insertResult)
        assertNull(dbState.queryResult)
        assertEquals(0, dbState.deleteResult)
        assertEquals(0, dbState.updateResult)
    }

    @Test
    fun `dismissDialog should set showDialog to false`() = runTest {
        viewModel.updateDialogMessage("Test message")
        viewModel.dismissDialog()

        val state = viewModel._state.value
        assertEquals(false, state.loginDialog?.showDialog)
    }

    @Test
    fun `updateDialogMessage should update dialog message and show dialog`() = runTest {
        val message = "Test dialog message"
        viewModel.updateDialogMessage(message)

        val state = viewModel._state.value
        assertEquals(true, state.loginDialog?.showDialog)
        assertEquals(message, state.loginDialog?.dialogMessage)
    }
}