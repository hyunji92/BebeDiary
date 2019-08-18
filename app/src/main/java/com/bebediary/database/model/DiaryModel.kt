package com.bebediary.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bebediary.database.entity.Diary

data class DiaryModel(
        @Embedded val diary: Diary,
        @Relation(parentColumn = "id", entityColumn = "diaryId") val diaryAttachments: List<DiaryAttachmentModel>
)