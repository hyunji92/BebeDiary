package com.bebediary.camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.util.extension.toAttachment
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.util.*

class CameraWrapperActivity : AppCompatActivity(), LifecycleObserver {

    // 이미지 정보
    private val imageFile by lazy { File(cacheDir, "${UUID.randomUUID()}.jpg") }
    private val imageUri by lazy { FileProvider.getUriForFile(this, "$packageName.provider", imageFile) }

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun openCameraActivity() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 사진 촬영 실패 했을 경우
        if (resultCode != Activity.RESULT_OK) {
            setResult(resultCode)
            finish()
            return
        }

        // 이미지 파일 어테치먼트로 변경
        imageFile.toAttachment(this)
                .subscribe(
                        {
                            // 원본 데이터 제거
                            try {
                                imageFile.delete()
                            } catch (e: Exception) {

                            }

                            // 완료 데이터 설정
                            setResult(Activity.RESULT_OK, Intent().putExtra("imagePath", it.file.path))

                            // 액티비티 종료
                            finish()
                        },
                        {
                            it.printStackTrace()
                            finish()
                        }
                )
                .apply { compositeDisposable.add(this) }
    }
}