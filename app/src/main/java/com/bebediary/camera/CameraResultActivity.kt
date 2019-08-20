package com.bebediary.camera

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.GlideApp
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.entity.Sex
import com.bebediary.database.model.BabyModel
import com.bebediary.util.extension.eventDateToText
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera_result.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 아이 사진 찍은 후 워터마크 데이터를 포함한 이미지 결과로 보여주는 화면
 */
class CameraResultActivity : AppCompatActivity(), LifecycleObserver {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // 이미지 경로
    private val imagePath: String?
        get() = intent.getStringExtra("imagePath")

    // Database
    private val db by lazy { (application as MyApplication).db }

    // Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_result)

        // 데이터 1차 검토
        if (babyId == -1L || imagePath == null) {
            finish()
            return
        }

        // Add Observer
        lifecycle.addObserver(this)
    }

    /**
     * 뷰 초기화
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // 이미지 로딩
        GlideApp.with(this)
            .load(imagePath)
            .centerCrop()
            .into(cameraResultImageView)

        // 이미지 선택시 파일 저장 및 종료
        cameraResultImageView.setOnClickListener { saveAndExit() }
    }

    /**
     * 아이 정보 요청
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchBaby() {
        db.babyDao().getById(babyId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateBabyWatermark(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 아이 정보를 가져와서 하단 워터마크 업데이트
     */
    private fun invalidateBabyWatermark(babyModel: BabyModel) {

        // 아이 이름 설정
        cameraResultWatermarkBabyNameView.text = babyModel.baby.name

        // 성별 설정
        cameraResultWatermarkBabyGenderView.setImageResource(
            when (babyModel.baby.sex) {
                Sex.Female -> R.drawable.noti_icon
                Sex.Male -> R.drawable.noti_icon_off
            }
        )

        // 생일 혹은 출산 예정일
        val eventDate = (if (babyModel.baby.isPregnant) babyModel.baby.babyDueDate else babyModel.baby.birthday)
            ?: return

        // 생일 뷰 설정
        val dateFormat = SimpleDateFormat("YYYY.MM.dd", Locale.getDefault())
        cameraResultWatermarkBabyBirthView.text = dateFormat.format(eventDate)

        // 아이 설명 뷰 설정
        cameraResultWatermarkBabyDescriptionView.text = babyModel.baby.eventDateToText()
    }

    /**
     * 이미지 파일 저장 후 화면 종료
     */
    private fun saveAndExit() {
        val currentTimeString = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val storageDirectory = File(Environment.getExternalStorageDirectory().toString(), "/Bebe")
        if (storageDirectory.exists().not()) {
            storageDirectory.mkdirs()
        }

        // 저장될 이미지 파일
        val imageFile = File(storageDirectory, "${cameraResultWatermarkBabyNameView.text}-$currentTimeString.jpg")

        // 저장 작업 실행
        Observable.fromCallable {
            val bitmap = cameraResultRootView.drawToBitmap()
            val output = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
            output.close()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // 갤러리 추가 할 수 있도록 브로드캐스팅
                    sendBroadcast(imageFile)

                    // 액티비티 종료
                    finish()
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 이미지 스캔 인텐트 요청해서
     * 갤러리에 보일 수 있도록 요청
     */
    private fun sendBroadcast(file: File) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = Uri.fromFile(file)
            sendBroadcast(mediaScanIntent)
        }
    }
}