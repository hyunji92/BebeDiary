package com.bebediary.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bebediary.database.entity.Baby
import com.bebediary.database.entity.Photo

data class PhotoModel(
    @Embedded val photo: Photo,
    @Relation(parentColumn = "babyId", entityColumn = "id") val babies: List<Baby>,
    @Relation(
        parentColumn = "id",
        entityColumn = "photoId"
    ) val photoAttachments: List<PhotoAttachmentModel>
)