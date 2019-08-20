package com.bebediary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bebediary.database.converter.DateTypeConverter
import com.bebediary.database.converter.FileTypeConverter
import com.bebediary.database.converter.SexTypeConverter
import com.bebediary.database.dao.*
import com.bebediary.database.entity.*
import com.bebediary.database.model.DiaryAttachmentModel

@Database(
        entities = [Baby::class, Attachment::class, Note::class, Diary::class, DiaryAttachment::class],
        views = [DiaryAttachmentModel::class],
        version = 7
)
@TypeConverters(SexTypeConverter::class, DateTypeConverter::class, FileTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryAttachmentDao(): DiaryAttachmentDao
}
