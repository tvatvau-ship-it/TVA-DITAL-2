package com.example

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.data.AppDatabase

class TvaApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("TvaApplication", "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "tva-database"
        ).fallbackToDestructiveMigration().build()
    }
}
