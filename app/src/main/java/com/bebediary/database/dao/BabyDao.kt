package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Baby
import com.bebediary.database.model.BabyModel
import io.reactivex.Flowable

@Dao
interface BabyDao : BaseDao<Baby> {

    @Query("SELECT baby.id as baby_id, baby.name as baby_name, baby.sex as baby_sex, baby.createdAt as baby_createdAt, attachment.id as photo_id, attachment.file as photo_file FROM baby INNER JOIN attachment ON baby.photoId = attachment.id")
    fun getAll(): Flowable<List<BabyModel>>


}