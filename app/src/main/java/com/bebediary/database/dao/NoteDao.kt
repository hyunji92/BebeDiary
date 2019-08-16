package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Baby
import com.bebediary.database.entity.Note
import io.reactivex.Flowable

@Dao
interface NoteDao : BaseDao<Note> {

}