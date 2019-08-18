package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Diary::class,
            parentColumns = ["id"],
            childColumns = ["diaryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Attachment::class,
            parentColumns = ["id"],
            childColumns = ["attachmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DiaryAttachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val diaryId: Long,
    @ColumnInfo val attachmentId: Long,
    @ColumnInfo val createdAt: Date = Date()
)