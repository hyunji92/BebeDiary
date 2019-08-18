package com.bebediary.database.dao

import androidx.room.Insert
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single

interface BaseDao<T> {

    @Insert
    fun insert(data: T): Single<Long>

    @Update
    fun update(data: T): Completable
}