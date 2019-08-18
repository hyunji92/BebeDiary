package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Diary
import com.bebediary.database.model.DiaryModel
import io.reactivex.Flowable

@Dao
interface DiaryDao : BaseDao<Diary> {

    @Query("SELECT * FROM diary WHERE babyId = :babyId")
    fun getBabyDiaries(babyId: Long): Flowable<List<DiaryModel>>
}