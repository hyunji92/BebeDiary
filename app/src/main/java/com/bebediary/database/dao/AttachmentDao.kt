package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Attachment
import io.reactivex.Single

@Dao
interface AttachmentDao : BaseDao<Attachment> {

    @Query("SELECT * FROM attachment WHERE id = :id")
    fun getById(id: Long): Single<Attachment>

}