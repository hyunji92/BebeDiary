package com.bebediary

import android.app.Application
import androidx.room.Room
import com.bebediary.database.AppDatabase
import com.facebook.stetho.Stetho

class MyApplication : Application() {

    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "db-bebe-diary"
        )
            .fallbackToDestructiveMigration() // 새로운 디비 생성해버림
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
    }
}