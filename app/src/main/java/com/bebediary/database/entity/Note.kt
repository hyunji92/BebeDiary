package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.SET_NULL
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Baby::class,
            parentColumns = ["id"],
            childColumns = ["babyId"],
            onDelete = SET_NULL
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val babyId: Long,
    @ColumnInfo val title: String,
    @ColumnInfo val content: String,
    @ColumnInfo val createdAt: Date
)