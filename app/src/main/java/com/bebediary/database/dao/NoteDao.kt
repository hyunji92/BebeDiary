package com.bebediary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.bebediary.database.entity.Note
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface NoteDao : BaseDao<Note> {

    @Query("SELECT * FROM note WHERE note.babyId = :babyId")
    fun getNotes(babyId: Long): Flowable<List<Note>>

    @Query("SELECT * FROM note WHERE note.id = :id")
    fun getNoteById(id: Long): Single<Note>
}