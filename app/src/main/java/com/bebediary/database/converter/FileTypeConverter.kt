package com.bebediary.database.converter

import androidx.room.TypeConverter
import java.io.File

class FileTypeConverter {

    @TypeConverter
    fun toFile(value: String?): File? {
        return if (value == null) null else File(value)
    }

    @TypeConverter
    fun toPath(value: File?): String? {
        return value?.path
    }

}