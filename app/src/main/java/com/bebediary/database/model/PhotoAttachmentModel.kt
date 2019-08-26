package com.bebediary.database.model

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.PhotoAttachment

@DatabaseView("SELECT * FROM photoattachment")

data class PhotoAttachmentModel(
    @Embedded val photoAttachment: PhotoAttachment,
    @Relation(parentColumn = "attachmentId", entityColumn = "id") val attachments: List<Attachment>
)