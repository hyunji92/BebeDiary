package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Photo
import com.bebediary.database.model.PhotoModel
import io.reactivex.Flowable

@Dao
interface PhotoDao : BaseDao<Photo> {

    @Query("SELECT * FROM photo")
    fun getAll(): Flowable<List<PhotoModel>>

    @Query("SELECT * FROM photo WHERE babyId = :babyId")
    fun getBabyPhotos(babyId: Long): Flowable<List<PhotoModel>>
}