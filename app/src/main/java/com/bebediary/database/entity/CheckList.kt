package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CheckList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val babyId: Long,
    @ColumnInfo val categoryId: Long,
    @ColumnInfo val content: String,
    @ColumnInfo var isComplete: Boolean = false
)