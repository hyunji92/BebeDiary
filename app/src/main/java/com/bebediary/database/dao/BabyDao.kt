package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Baby
import com.bebediary.database.model.BabyModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface BabyDao : BaseDao<Baby> {

    @Query("SELECT * FROM baby")
    fun getAll(): Flowable<List<BabyModel>>

    @Query("SELECT * FROM baby  WHERE isSelected = 1 LIMIT 1")
    fun getSelected(): Observable<BabyModel>

    @Query("UPDATE baby SET isSelected = id == :babyId")
    fun select(babyId: Long): Completable
}