package com.liu.todoapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liu.todoapp.R
import com.liu.todoapp.ui.model.Todo
import com.liu.todoapp.ui.network.ToDoApiService
import com.liu.todoapp.ui.repository.ToDoRepositoryImpl
import com.liu.todoapp.ui.util.ToDoViewModelFactory
import com.liu.todoapp.ui.viewmodel.ToDoViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current

    val api = remember {
        Retrofit.Builder()
            .baseUrl("http://xxxx.xxxx.xx/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ToDoApiService::class.java)
    }

    val todoRepository = remember { ToDoRepositoryImpl(api) }

    val toDoViewModel: ToDoViewModel = viewModel(
        factory = ToDoViewModelFactory(todoRepository)
    )

    val todoState by toDoViewModel._todoState.collectAsStateWithLifecycle()

    val addItem = rememberSaveable { mutableStateOf("") }

    /**
     * loading more data rely on whether lastVisible is the last one of current lists, yes trigger loading more data
     */
//    val listReachBottom = rememberLazyListState()
//
//    LaunchedEffect(listReachBottom) {
//        snapshotFlow { listReachBottom.layoutInfo }
//            .collect { layoutInfo ->
//                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index
//                val totalItems = layoutInfo.totalItemsCount
//                if (lastVisible == totalItems - 1 && !todoState.isLoadingMore) {
//                    toDoViewModel.loadMoreOffline()
//                }
//            }
//    }

//    var page = remember { 1 }
//    val count = remember { 8 }

    val favoriteCount by remember { derivedStateOf { todoState.lists.count { it.isFavorite }} }

    LaunchedEffect(Unit) {
        supervisorScope {
            launch {
                toDoViewModel._toastMsg.collect { toastMsg ->
                    Toast.makeText(context,toastMsg,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally){
        Row (modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)){
            TextField(
                value = addItem.value,
                onValueChange = { addItem.value = it},
                singleLine = true,
                label = { Text(text = "input the item you want to add")},
                modifier = Modifier
                    .weight(weight = 2f)
                    .height(80.dp)
            )
            Button(onClick = {
                val newTD = Todo(0,addItem.value,addItem.value,false)
                toDoViewModel.addTodoOffline(newTD)
            }, modifier = Modifier
                .weight(weight = 1f)
                .height(80.dp)
                .padding(all = 20.dp)) {
                Text(text = "Add")
            }
        }

        Text("Already Favorite:  $favoriteCount / ${todoState.lists.size}")

        Spacer(modifier = Modifier.height(5.dp))

        PullToRefreshAndLoadingMore(
            isRefreshing = todoState.isRefresh,
            onRefreshData = { toDoViewModel.getTodosOffline() },
            isLoadingMore = todoState.isLoadingMore,
            onLoadingMore = { toDoViewModel.loadMoreOffline() }
        ) { listState ->
            LazyColumn (
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)){
                items(todoState.lists,
                    key = { it.id }) { toDoItem ->
                    ItemLayout(toDoItem,toDoViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshAndLoadingMore(
    isRefreshing: Boolean,
    onRefreshData: () -> Unit,
    isLoadingMore: Boolean,
    onLoadingMore: () -> Unit,
    content: @Composable (LazyListState) -> Unit) {
//    val coroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var loadTriggered by remember { mutableStateOf(false) }
//    val loadingState = remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing =  isRefreshing,
        onRefresh = onRefreshData,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (available.y < 0 &&
                        lastVisible?.index == totalItems - 1 &&
                        !loadTriggered && !isLoadingMore) {
                        loadTriggered = true
                        onLoadingMore()
                    }
                    return Offset.Zero
                }
            })
    ) {
        content(listState)

        if (isLoadingMore) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
        }
    }

    LaunchedEffect(isLoadingMore) {
        if (!isLoadingMore) loadTriggered = false
    }
}

@Composable
fun ItemLayout(todoItem: Todo, toDoViewModel: ToDoViewModel) {
    Row (modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .padding(top = 8.dp, bottom = 8.dp, start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically){
        Column (modifier = Modifier.weight(3f)){
            Text(text = todoItem.title, modifier = Modifier.weight(1f))
            Text(text = todoItem.details, modifier = Modifier.weight(2f))
        }
        Spacer(modifier = Modifier.width(5.dp))
        Button(onClick = {
            toDoViewModel.changeCurrentTodoLikeOffline(todoItem)
//            toDoViewModel.changeValueFavorite(todoItem)
        }, modifier = Modifier
            .height(100.dp)
            .weight(1.5f)) {
            Icon(
                imageVector = if (todoItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(id = R.string.favourite)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "Like",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ))
        }
    }
}