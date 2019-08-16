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
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bebediary.GlideApp
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.entity.Attachment
import com.bebediary.util.extension.toAttachment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hyundeee.app.usersearch.YameTest
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

    private val pickFromAlbum = 1
    private val pickFromCamera = 2

    lateinit var imgUri: Uri
    lateinit var photoURI: Uri


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

            var test = prefs.getString("album_image", null)
            Log.d("test", "test camera image : " + test)
            if (test == null || test == "") {
                YameTest.testSubject?.onNext(imgUri)
            } else {
                Log.d("test", "test album image : " + test)
                YameTest.testSubject?.onNext(photoURI)
            }
            this.finish()
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
        val imageUri = when (requestCode) {
            pickFromAlbum -> data.data // 앨범에서 이미지를 선택했을경우
            pickFromCamera -> imgUri // 카메라 촬영으로 이미지를 가져왔을 경우
            else -> null
        } ?: return

        // 이미지 저장 및 설정
        val attachmentDao = db.attachmentDao()
        imageUri.toAttachment(this)
            .flatMap { attachment -> attachmentDao.insert(attachment) }
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
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (photoFile != null) {
                    val providerURI = FileProvider.getUriForFile(this, packageName.plus(".provider"), photoFile)
                    imgUri = providerURI
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI)
                    startActivityForResult(intent, pickFromCamera)
                }
            }
        } else {
            Log.v("알림", "저장공간에 접근 불가능")
            return
        }
    }

    /**
     * Attachment 데이터를 넘겨받아 유저의 이미지뷰를 업데이트한다
     */
    private fun invalidatePhotoView(attachment: Attachment) {
        empty_baby_image_register.isVisible = false
        baby_image.isVisible = true
        baby_image_layout.isVisible = true

        // 이미지 로딩
        GlideApp.with(this)
            .load(attachment.file)
            .centerCrop()
            .circleCrop()
            .into(baby_image)
        baby_image.invalidate()
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

    private fun tedPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                // 권한 요청 실패
                isPermission = false
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage(resources.getString(R.string.permission_2))
            .setDeniedMessage(resources.getString(R.string.permission_1))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }

    // 이름, 성별, 몇일
    fun saveBabyInfo() {
        if (baby_name.text.toString() != "" || baby_name.text.toString() != null) {
            editor.apply {
                putString("baby_name", baby_name.text.toString())
                apply()
            }
        }
        Log.d("test", "EditText nane : " + baby_name.text.toString())

        if (baby_gender.text.toString() != "" || baby_gender.text.toString() != null) {
            editor.apply {
                putString("baby_gender", baby_gender.text.toString())
                apply()
            }
        }

        //알람 몇일 전 ? 저장 ( 라이디오 버튼 값 )

    }

    fun setPregnantStatus() {
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

    fun datePicker() {
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
