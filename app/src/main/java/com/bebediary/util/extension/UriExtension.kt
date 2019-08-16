package com.bebediary.util.extension

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import com.bebediary.database.entity.Attachment
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.lang.IllegalStateException
import java.util.*

/**
 * Uri를 유저의 애플리케이션 데이터 폴더에 파일을 복사 시키고
 * 해당 파일 경로를 Attachment 를 반환한다
 */
fun Uri.toAttachment(context: Context): Single<Attachment> {
    return Observable.fromCallable {

        // Uri를 가지고 파일을 데이터 폴더에 이동
        val sourceFile = try {
            this.toFile()
        } catch (e: Exception) {
            Log.d("UriExtension", "TO File : ${this}")
            val path = this.path
            if (path?.startsWith("/storage") == true) {
                File(path)
            } else {
                context.contentResolver.query(this, null, null, null, null, null)?.run {
                    moveToFirst()
                    val file = File(getString(getColumnIndex(MediaStore.Images.ImageColumns.DATA)))
                    close()
                    file
                } ?: throw IllegalStateException("URI($this)를 파일로 변환할 수 없습니다")
            }
        }

        // Target File
        val targetFile = File(context.filesDir, "attachments/${UUID.randomUUID()}.${sourceFile.extension}")

        // 파일 폴더로 파일 이동
        sourceFile.copyTo(targetFile)

        // Attachment
        Attachment(file = targetFile)
    }.singleOrError()
}
