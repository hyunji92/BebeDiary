package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.CheckList
import io.reactivex.Flowable

@Dao
interface CheckListDao : BaseDao<CheckList> {

    @Query("SELECT * FROM checklist WHERE checklist.babyId = :babyId")
    fun getCheckLists(babyId: Long): Flowable<List<CheckList>>

}