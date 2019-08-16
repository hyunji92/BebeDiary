package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val file: File
)