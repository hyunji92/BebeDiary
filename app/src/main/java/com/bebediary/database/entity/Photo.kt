package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val babyId: Long,
    @ColumnInfo val createdAt: Date = Date()
)