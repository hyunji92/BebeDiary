package com.bebediary.util.extension

import android.content.Context
import com.bebediary.database.entity.Attachment
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.*

/**
 * File을 유저의 애플리케이션 데이터 폴더에 파일을 복사 시키고
 * 해당 파일 경로를 Attachment 를 반환한다
 */
fun File.toAttachment(context: Context): Single<Attachment> {
    return Observable.fromCallable {

        // Source File
        val sourceFile = this

        // Target File
        val targetFile = File(context.filesDir, "attachments/${UUID.randomUUID()}.${sourceFile.extension}")

        // 파일 폴더로 파일 이동
        sourceFile.copyTo(targetFile)

        // Attachment
        Attachment(file = targetFile)
    }.singleOrError()
}
