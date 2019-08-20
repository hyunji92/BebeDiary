package com.bebediary.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible
import com.bebediary.GlideApp
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.camera.CameraWrapperActivity
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Baby
import com.bebediary.database.entity.Sex
import com.bebediary.util.Constants
import com.bebediary.util.extension.format
import com.bebediary.util.extension.toAttachment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_baby_register.*
import java.io.File
import java.util.*


/**
 * 아이 등록하는 액티비티
 */
class BabyRegisterActivity : Activity() {

    // 사진 퍼미션이 여부
    private var hasPhotoPermission = false

    // 임신 여부 정보
    var isPregnant = false
        set(value) {
            field = value
            invalidatePregnant()
        }

    // 생일 혹은 출산 예정일 정보
    private var year = -1
    private var month = -1
    private var day = -1
        set(value) {
            field = value
            invalidateEventDate()
        }

    // 생일 혹은 출산일 캘린더
    private val eventCalendar
        get() = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

    // Baby 사진 정보
    private var photo: Attachment? = null

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (application as MyApplication).db }

    // 수정 모드 여부
    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_baby_register)

        // 사진 접근 권한
        tedPermission()

        // 아이 추가 종료 버튼
        register_exit.setOnClickListener { finish() }

        // 이이 정보 저장 요청
        register_baby.setOnClickListener { saveBabyInfo() }

        // 날짜 변경
        date_picker_button.setOnClickListener { datePicker() }

        // 임신 상태 업데이트
        pregnant_on_off.setOnClickListener { isPregnant = !isPregnant }

        // 이미지 등록 버튼
        empty_baby_image_register.setOnClickListener {
            // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
            if (hasPhotoPermission) {
                showPickPhotoDialog()
            } else {
                Toast.makeText(it.context, resources.getString(R.string.permission_2), Toast.LENGTH_LONG).show()
            }
        }

        updateLayout()
    }

    /**
     * 레이아웃 사이즈 업데이트
     */
    private fun updateLayout() {
        val point = Point()
        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        display.getSize(point)

        window.attributes.width = (point.x * 0.9).toInt() //Display 사이즈의 90%
    }

    /**
     * 사진 선택 다이얼로그
     */
    private fun showPickPhotoDialog() {
        AlertDialog.Builder(this)
            .setTitle("사진등록")
            .setItems(arrayOf("카메라", "갤러리")) { _, id ->
                if (id == 0) takePhoto() else goToAlbum()
            }
            .show()
    }

    /**
     * 유저가 카메라나 갤러리 이미지 선택했을때
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != RESULT_OK) {
            return
        }

        // 이미지 선택 URI
        val attachment = when (requestCode) {
            Constants.requestAlbumCode ->
                data.data?.toAttachment(this) // 앨범에서 이미지를 선택했을경우
            Constants.requestCameraCode -> {
                File(data.getStringExtra("imagePath")).toAttachment(this) // 카메라 촬영으로 이미지를 가져왔을 경우

            }
            else -> null
        } ?: return

        // 이미지 저장 및 설정
        val attachmentDao = db.attachmentDao()
        attachment
            .flatMap { attachmentDao.insert(it) }
            .flatMap { id -> db.attachmentDao().getById(id) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidatePhotoView(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 사진 앨벙 선택
     */
    private fun goToAlbum() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, Constants.requestAlbumCode)
    }

    /**
     * 사진 촬영 요청
     */
    private fun takePhoto() {
        val intent = Intent(this, CameraWrapperActivity::class.java)
        startActivityForResult(intent, Constants.requestCameraCode)
    }

    /**
     * Attachment 데이터를 넘겨받아 유저의 이미지뷰를 업데이트한다
     */
    private fun invalidatePhotoView(attachment: Attachment) {

        // 아이 정보 업데이트
        photo = attachment

        // View 업데이트
        empty_baby_image_register.isVisible = false
        baby_image.isVisible = true
        baby_image_layout.isVisible = true

        // 이미지 로딩
        GlideApp.with(this)
            .load(attachment.file)
            .centerCrop()
            .circleCrop()
            .into(baby_image)
    }

    /**
     * 임신 여부에 따른 뷰 업데이트
     * 1. 임신 여부 토글 버튼
     * 2. 생일, 출산 예정일 날짜 입력 창
     */
    private fun invalidatePregnant() {

        // 임신중 아이콘 변경
        pregnant_on_off.setBackgroundResource(if (isPregnant) R.drawable.pregnant_on else R.drawable.pregnant_off)

        // 텍스트 생일 출산 예정일 토글
        birthday_text.text = if (isPregnant) "출산 예정일" else "생일"
    }

    /**
     * 생일 혹은 출산 예정일 업데이트
     */
    private fun invalidateEventDate() {
        baby_birthday.text = eventCalendar.time.format("YYYY.MM.dd")
    }

    /**
     * 스토리지 권한 설정
     */
    private fun tedPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    hasPhotoPermission = true
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                    hasPhotoPermission = false
                }
            })
            .setRationaleMessage(resources.getString(R.string.permission_2))
            .setDeniedMessage(resources.getString(R.string.permission_1))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }

    /**
     * 아이 정보 데이터 베이스에 저장
     */
    private fun saveBabyInfo() {

        // 이름 입력 여부 확인
        if (baby_name.text.isNullOrBlank()) {
            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 성별 선택 여부 확인
        if (!babyRegisterMale.isChecked && !babyRegisterFeMale.isChecked) {
            Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 사진 선택
        if (photo == null) {
            Toast.makeText(this, "사진을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 이벤트 날짜 설정 여부
        if (year == -1 || month == -1 || day == -1) {
            val message = if (isPregnant) "출산 예정일을 선택해주세요" else "생일을 선택해주세요"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        // 아이 정보 생성
        val baby = Baby(
            name = baby_name.text.toString(),
            sex = if (babyRegisterMale.isChecked) Sex.Male else Sex.Female,
            photoId = photo?.id ?: throw IllegalStateException("사진 아이디가 존재하지 않습니다"),
            isPregnant = isPregnant,
            birthday = if (isPregnant) null else eventCalendar.time,
            babyDueDate = if (isPregnant) eventCalendar.time else null,
            isSelected = isEdit.not()
        )

        // 데이터 입력
        db.babyDao().insert(baby)
            .subscribeOn(Schedulers.io())
            .subscribe(
                { finish() },
                {
                    Toast.makeText(this, "아이 데이터 생성에 실패하였습니다", Toast.LENGTH_SHORT).show()
                    it.printStackTrace()
                }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * DatePicker
     */
    private fun datePicker() {
        val calendar = Calendar.getInstance()

        // API 24 이상일 경우 시스템 기본 테마 사용
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            ContextThemeWrapper(this, R.style.MyDatePickerSpinnerStyle)
        else this

        // 다이얼로그 보여줌
        DatePickerDialog(
            context, { _, year, month, day ->

                // 이벤트 정보 업데이트
                this.year = year
                this.month = month
                this.day = day
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
