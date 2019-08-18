package com.bebediary.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.Window
import android.widget.Toast
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.camera.CameraWrapperActivity
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Baby
import com.bebediary.database.entity.Sex
import com.bebediary.util.Constants
import com.bebediary.util.extension.toAttachment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_baby_register.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BabyRegisterActivity : Activity() {

    companion object {
        var tempFile: File? = null
    }

    // Activity Result RequestCode
    private val pickFromAlbum = 1

    lateinit var imgUri: Uri

    lateinit var mCurrentPhotoPath: String

    private var isPermission = true

    val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        Log.d("test", "test Date year: $year monthOfYear: $monthOfYear  dayOfMonth: $dayOfMonth")
        baby_birthday.text = "$year.$monthOfYear.$dayOfMonth"

        // 현재시간을 msec 으로 구한다.
        val now = System.currentTimeMillis()
        // 현재시간을 date 변수에 저장한다.
        val date = Date(now)

        val commandDateFormat by lazy { SimpleDateFormat("yyyyHHmm", Locale.getDefault()) }
        val formatDate = commandDateFormat.format(date)

        Log.d("test", "test Date Today: $formatDate")
        var month = ""
        var day = ""

        if (monthOfYear < 10) {
            month = "0$monthOfYear"
        }
        if (dayOfMonth < 10) {
            day = "0$dayOfMonth"
        }
        editor.putString("baby_birthday", year.toString() + month + day)
        editor.apply()

        Log.d("test", "test Date birthday:" + prefs.getString("baby_birthday", formatDate))
    }

    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    var isPregnant: Boolean = false

    // Baby 정보
    private var photo: Attachment? = null

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (application as MyApplication).db }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_baby_register)

        //사진 접근 권한
        tedPermission()

        prefs = getSharedPreferences("baby_info", Context.MODE_PRIVATE)
        editor = prefs.edit()

        register_exit.setOnClickListener {
            this.finish()
        }
        register_baby.setOnClickListener {
            saveBabyInfo()
            editor.apply()
        }

        date_picker_button.setOnClickListener { datePicker() }
        setPregnantStatus()

        val mImageUri = prefs.getString("image", null)
        if (mImageUri != null) {
            empty_baby_image_register.visibility = View.GONE
            baby_image.visibility = View.VISIBLE
            baby_image_layout.visibility = View.VISIBLE
            baby_image.setImageURI(Uri.parse(mImageUri))
        } else {
            baby_image.setImageResource(R.drawable.empty_image_register)
        }

        empty_baby_image_register.setOnClickListener {
            // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
            if (isPermission) {
                showPickPhotoDialog()
            } else {
                Toast.makeText(it.context, resources.getString(R.string.permission_2), Toast.LENGTH_LONG)
                    .show()
            }
        }

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
            if (tempFile?.exists() == true && tempFile?.delete() == true) {
                tempFile = null
            }
            return
        }

        // 이미지 선택 URI
        val attachment = when (requestCode) {
            pickFromAlbum ->
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
        startActivityForResult(intent, pickFromAlbum)
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val imgFileName = "bebe_$timeStamp.jpg"
        var imageFile: File? = null
        val storageDir = File(Environment.getExternalStorageDirectory().toString() + "/Bebe")
        if (!storageDir.exists()) {
            Log.v("알림", "storageDir 존재 x $storageDir")
            storageDir.mkdirs()
        }
        Log.v("알림", "storageDir 존재함 $storageDir")
        imageFile = File(storageDir, imgFileName)
        mCurrentPhotoPath = imageFile.absolutePath
        return imageFile
    }

    /**
     * 스토리지 권한 설정
     */
    private fun tedPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    // 권한 요청 성공
                    isPermission = true
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                    // 권한 요청 실패
                    isPermission = false
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

        // 아이 정보 생성
        val baby = Baby(
            name = baby_name.text.toString(),
            sex = if (babyRegisterMale.isChecked) Sex.Male else Sex.Female,
            photoId = photo?.id ?: throw IllegalStateException("사진 아이디가 존재하지 않습니다")
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

    private fun setPregnantStatus() {
        var check = false
        isPregnant = prefs.getBoolean("pregnant_status", false)
        if (isPregnant) {
            //임신중
            pregnant_on_off.setBackgroundResource(R.drawable.pregnant_on)
            birthday_text.text = "출산 예정일"
            birthday_line.setBackgroundResource(R.drawable.gender_popup_line)
        } else {
            //임신중 아님
            pregnant_on_off.setBackgroundResource(R.drawable.pregnant_off)
            birthday_text.text = "생일"
            birthday_line.setBackgroundResource(R.drawable.popup_line)
        }
        pregnant_on_off.setOnClickListener {
            if (!check) {
                //임신중
                pregnant_on_off.setBackgroundResource(R.drawable.pregnant_on)
                editor.apply {
                    putBoolean("pregnant_status", true)
                    apply()
                    Log.d("test", "Pregnant2 : " + prefs.getBoolean("pregnant_status", false))
                }
                birthday_text.text = "출산 예정일"
                birthday_line.setBackgroundResource(R.drawable.gender_popup_line)

                check = true
            } else {
                //임신중 아님
                pregnant_on_off.setBackgroundResource(R.drawable.pregnant_off)
                editor.apply {
                    putBoolean("pregnant_status", false)
                    apply()
                    Log.d("test", "Pregnant1 : " + prefs.getBoolean("pregnant_status", false))
                }
                birthday_text.text = "생일"
                birthday_line.setBackgroundResource(R.drawable.popup_line)
                check = false
            }
        }
    }

    private fun datePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        var context: Context = ContextThemeWrapper(this, R.style.MyDatePickerSpinnerStyle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24 이상일 경우 시스템 기본 테마 사용
            context = this
        }
        val datePickerDialog = DatePickerDialog(context, dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    fun showDatePicker(v: View) {
//        val newFragment = MyDatePickerFragment()
//        newFragment.show(getSupportFragmentManager(), "date picker")
        //https://www.zoftino.com/android-datepicker-example
    }
}
