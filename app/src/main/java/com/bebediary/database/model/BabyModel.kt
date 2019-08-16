package com.bebediary.database.model

import androidx.room.Embedded
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Baby

data class BabyModel(
        @Embedded(prefix = "baby_") var baby: Baby,
        @Embedded(prefix = "photo_") var photo: Attachment
)