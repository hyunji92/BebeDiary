package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.CheckListCategory
import io.reactivex.Flowable

@Dao
interface CheckListCategoryDao : BaseDao<CheckListCategory> {

    @Query("SELECT * FROM checklistcategory")
    fun getAll(): Flowable<List<CheckListCategory>>
}