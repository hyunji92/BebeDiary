package com.bebediary.database.model

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.DiaryAttachment

@DatabaseView("SELECT * FROM diaryattachment")
data class DiaryAttachmentModel(
        @Embedded val diaryAttachment: DiaryAttachment,
        @Relation(parentColumn = "attachmentId", entityColumn = "id") val attachments: List<Attachment>
)