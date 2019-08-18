package com.bebediary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bebediary.database.converter.DateTypeConverter
import com.bebediary.database.converter.FileTypeConverter
import com.bebediary.database.converter.SexTypeConverter
import com.bebediary.database.dao.AttachmentDao
import com.bebediary.database.dao.BabyDao
import com.bebediary.database.dao.NoteDao
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Baby
import com.bebediary.database.entity.Note

@Database(
    entities = [Baby::class, Attachment::class, Note::class],
    version = 3
)
@TypeConverters(SexTypeConverter::class, DateTypeConverter::class, FileTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
}
