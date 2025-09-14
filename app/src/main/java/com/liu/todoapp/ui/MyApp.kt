package com.liu.todoapp.ui

import android.app.Application
import androidx.room.Room
import com.liu.todoapp.ui.database.RoomDB

class MyApp: Application() {

    companion object{
        lateinit var db: RoomDB
            private set
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            RoomDB::class.java,
            "my_database"
        ).build()
    }
}