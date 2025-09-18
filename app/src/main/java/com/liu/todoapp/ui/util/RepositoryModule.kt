package com.liu.todoapp.ui.util

import com.liu.todoapp.ui.repository.LoginRepository
import com.liu.todoapp.ui.repository.LoginRepositoryImpl
import com.liu.todoapp.ui.repository.ToDoRepository
import com.liu.todoapp.ui.repository.ToDoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindToDoRepository(
        impl: ToDoRepositoryImpl
    ): ToDoRepository

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        impl: LoginRepositoryImpl
    ): LoginRepository
}