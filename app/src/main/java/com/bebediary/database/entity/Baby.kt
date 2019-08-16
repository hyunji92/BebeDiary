package com.bebediary.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Attachment::class,
            parentColumns = ["id"],
            childColumns = ["photoId"]
        )
    ]
)
data class Baby(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sex") val sex: Sex,
    @ColumnInfo(name = "photoId") val photoId: Long? = null,
    @ColumnInfo(name = "createdAt") val createdAt: Date = Date()
)