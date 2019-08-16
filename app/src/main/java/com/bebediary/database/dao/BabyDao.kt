package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Baby
import com.bebediary.database.model.BabyModel
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface BabyDao : BaseDao<Baby> {

    @Query("SELECT * FROM baby")
    fun getAll(): Flowable<List<BabyModel>>

    @Query("SELECT * FROM baby LIMIT 1")
    fun getCurrent(): Observable<BabyModel>
}