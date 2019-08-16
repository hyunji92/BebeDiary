package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Baby
import io.reactivex.Flowable

@Dao
interface BabyDao : BaseDao<Baby> {

    @Query("SELECT * FROM baby")
    fun getAll(): Flowable<Baby>


}