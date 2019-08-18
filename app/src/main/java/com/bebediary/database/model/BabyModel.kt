package com.bebediary.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Baby

data class BabyModel(
        @Embedded val baby: Baby,
        @Relation(parentColumn = "photoId", entityColumn = "id") val photos: List<Attachment>
)