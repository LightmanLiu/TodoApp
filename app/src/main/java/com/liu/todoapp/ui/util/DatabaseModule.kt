package com.liu.todoapp.ui.util

import android.content.Context
import androidx.room.Room
import com.liu.todoapp.ui.database.LoginDao
import com.liu.todoapp.ui.database.RoomDB
import com.liu.todoapp.ui.database.ToDoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context) : RoomDB {
        return Room.databaseBuilder(
            context,
            RoomDB::class.java,
            "my_database"
        ).build()
    }

    @Provides
    fun provideToDoDao(database: RoomDB): ToDoDao = database.todoDao()

    @Provides
    fun provideLoginDao(database: RoomDB): LoginDao = database.loginDao()
}