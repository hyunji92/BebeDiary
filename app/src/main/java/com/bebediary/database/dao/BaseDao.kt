package com.bebediary.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import io.reactivex.Single

interface BaseDao<T> {

    @Insert
    fun insert(data: T): Single<Long>

    @Insert
    fun insertAll(data: List<T>): Single<List<Long>>

    @Delete
    fun deleteAll(data: List<T>): Single<Int>

    @Update
    fun update(data: T): Single<Int>
}