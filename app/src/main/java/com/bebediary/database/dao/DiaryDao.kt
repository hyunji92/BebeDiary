package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Diary
import com.bebediary.database.model.DiaryModel
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface DiaryDao : BaseDao<Diary> {

    @Query("SELECT * FROM diary WHERE babyId = :babyId")
    fun getBabyDiaries(babyId: Long): Flowable<List<DiaryModel>>

    @Query("SELECT * FROM diary WHERE babyId = :babyId and date >= date('now') and date <= date('now','+10 days')")
    fun getIncomingBabyDiaries(babyId: Long): Flowable<List<DiaryModel>>

    @Query("SELECT * FROM diary WHERE babyId = :babyId and date BETWEEN :startDateTimeInMillis AND :endDateTimeInMillis")
    fun getDiaryByDate(babyId: Long, startDateTimeInMillis: Long, endDateTimeInMillis: Long): Maybe<DiaryModel>

    @Query("SELECT * FROM diary WHERE babyId = :babyId and date BETWEEN :startDateTimeInMillis AND :endDateTimeInMillis")
    fun getDiaryByDateRange(babyId: Long, startDateTimeInMillis: Long, endDateTimeInMillis: Long): Flowable<List<DiaryModel>>
}