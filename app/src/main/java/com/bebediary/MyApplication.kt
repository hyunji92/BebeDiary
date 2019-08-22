package com.bebediary

import android.app.Application
import androidx.room.Room
import com.bebediary.database.AppDatabase
import com.bebediary.database.callback.CheckListInitializer
import com.facebook.stetho.Stetho

class MyApplication : Application() {

    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "bebe-diary.db"
        )
            .fallbackToDestructiveMigration() // 새로운 디비 생성해버림
            .addCallback(CheckListInitializer(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
    }
}