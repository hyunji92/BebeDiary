package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Diary(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo val babyId: Long,
        @ColumnInfo val content: String,
        @ColumnInfo val date: Date,
        @ColumnInfo var isComplete: Boolean = false,
        @ColumnInfo val isEnableNotification: Boolean,
        @ColumnInfo val createdAt: Date = Date()
)