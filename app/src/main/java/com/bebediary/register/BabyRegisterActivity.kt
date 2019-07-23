package com.bebediary.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.Window
import android.widget.Toast
import com.bebediary.R
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_baby_register.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BabyRegisterActivity : Activity() {


    companion object {
        private val PICK_FROM_ALBUM = 1
        private val PICK_FROM_CAMERA = 2

        var tempFile: File? = null
    }

    lateinit var imgUri: Uri
    lateinit var photoURI: Uri
    lateinit var albumURI: Uri


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
        var month: String = ""
        var day: String = ""

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_baby_register)

        register_exit.setOnClickListener {
            this.finish()
        }
        date_picker_button.setOnClickListener { datePicker() }
        prefs = getSharedPreferences("baby_info", Context.MODE_PRIVATE)
        editor = prefs.edit()

        setPregnantStatus()

        register_baby.setOnClickListener {
            saveBabyInfo()
        }

        //사진 접근 권한
        tedPermission();

        empty_baby_image_register.setOnClickListener {
            // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
            //if(isPermission) goToAlbum();
            //else Toast.makeText(it.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            showDialog()
        }
        //https://black-jin0427.tistory.com/121
        //https://g-y-e-o-m.tistory.com/48
        //http://dailyddubby.blogspot.com/2018/04/107-tedpermission.html

        // url 이미지 폴더에 저장하고 이미지 열기
        // https://heum-story.tistory.com/13

        //사진 등록 ( 카메라, 갤러리 )
        //https://black-jin0427.tistory.com/120

        //등록한 사진 저장
        //이걸로 하면 될듯 : https://github.com/martinsing/Image-Save-And-Retrieve-App
        //https://m.blog.naver.com/PostView.nhn?blogId=kkh32490&logNo=220287551618&proxyReferer=https%3A%2F%2Fwww.google.com%2F
        //http://www.masterqna.com/android/16166/%EC%84%A0%ED%83%9D%EB%90%9C-bipmap%EC%9D%84sharedpreferences%EC%97%90-%EC%A0%80%EC%9E%A5%ED%95%98%EB%8A%94%EB%B2%95-%E3%85%9C%E3%85%9C
        //http://android-steps.blogspot.com/2015/08/profile-page-save-image-data-in.html


    }

    fun showDialog() {
        val value = arrayOf<String>("카메라", "갤러리")
        val alertdialogbuilder = AlertDialog.Builder(this)

        alertdialogbuilder.setTitle("사진등록")
        alertdialogbuilder.setItems(
            value
        ) { dialog, id ->
            //val selectedText = listOf(value)[id]
            if (id == 0) {
                takePhoto()
            } else {
                goToAlbum()
            }
        }
        val dialog = alertdialogbuilder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (resultCode !== Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show()
            tempFile?.run {
                if (tempFile!!.exists()) {
                    if (tempFile!!.delete()) {
                        //Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공")
                        tempFile = null
                    }
                }
            }
            return
        }

        if (requestCode == PICK_FROM_ALBUM) {

            /*val photoUri = data.data
            Log.d("tesxt", "PICK_FROM_ALBUM photoUri : " + photoUri);
            var cursor: Cursor? = null

            try {

                *//*
             *  Uri 스키마를
             *  content:/// 에서 file:/// 로  변경한다.
             *//*
                val proj = arrayOf(MediaStore.Images.Media.DATA)

                assert(photoUri != null)
                cursor = contentResolver.query(photoUri!!, proj, null, null, null)

                assert(cursor != null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                cursor.moveToFirst()

                tempFile = File(cursor.getString(column_index))
                Log.d("test :", "tempFile Uri : " + Uri.fromFile(tempFile))

            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }

            setImage()*/

            if (data.getData() != null) {
                try {
                    var albumFile: File? = null
                    albumFile = createImageFile()
                    photoURI = data.getData()
                    albumURI = Uri.fromFile(albumFile)
                    galleryAddPic()
                    empty_baby_image_register.setImageURI(photoURI)
                    //cropImage();
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.v("알림", "앨범에서 가져오기 에러")
                }
            }

        } else if (requestCode == PICK_FROM_CAMERA) {

            /*setImage()*/
            try {
                Log.v("알림", "FROM_CAMERA 처리")
                galleryAddPic()
                empty_baby_image_register.setImageURI(imgUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun setImage() {

        val options = BitmapFactory.Options()
        val originalBm = BitmapFactory.decodeFile(tempFile?.getAbsolutePath(), options)

        baby_image.setImageBitmap(originalBm)

    }

    fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.setData(contentUri)
        sendBroadcast(mediaScanIntent)
        Toast.makeText(this, "사진이 저장되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun goToAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    private fun takePhoto() {

        /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            tempFile = createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            finish()
            e.printStackTrace()
        }

        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "{package name}.provider", tempFile!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)

            } else {
                val photoUri = Uri.fromFile(tempFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }*/

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(getPackageManager()) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (photoFile != null) {

                    val providerURI = FileProvider.getUriForFile(this, getPackageName().plus(".provider"), photoFile)
                    imgUri = providerURI
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, providerURI)
                    startActivityForResult(intent, PICK_FROM_CAMERA)
                }
            }
        } else {
            Log.v("알림", "저장공간에 접근 불가능")
            return
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 이미지 파일 이름 ( blackJin_{시간}_ )
        /*val timeStamp = SimpleDateFormat("HHmmss").format(Date())
        val imageFileName = "blackJin_" + timeStamp + "_"
        // 이미지가 저장될 파일 주소 ( blackJin )
        val storageDir = File(Environment.getExternalStorageDirectory() + "/blackJin/")
        if (!storageDir.exists()) storageDir.mkdirs()
        // 빈 파일 생성
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        Log.d("test", "createImageFile : " + image.getAbsolutePath())
        return image*/
        val imgFileName = System.currentTimeMillis().toString() + ".jpg"
        var imageFile: File? = null
        val storageDir = File(Environment.getExternalStorageDirectory().toString() + "/Bebe")
        if (!storageDir.exists()) {
            Log.v("알림", "storageDir 존재 x " + storageDir.toString())
            storageDir.mkdirs()
        }
        Log.v("알림", "storageDir 존재함 " + storageDir.toString())
        imageFile = File(storageDir, imgFileName)
        mCurrentPhotoPath = imageFile.getAbsolutePath()
        return imageFile
    }

    private fun tedPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                // 권한 요청 성공

            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                // 권한 요청 실패
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
        pregnant_on_off.setOnClickListener {
            isPregnant = prefs.getBoolean("pregnant_status", false)
            if (!check) {
                //임신중
                pregnant_on_off.setBackgroundResource(R.drawable.pregnant_on)
                editor.apply {
                    putBoolean("pregnant_status", true)
                    apply()
                    Log.d("test", "Pregnant2 : " + prefs.getBoolean("pregnant_status", false))
                }
                birthday_text.text = "생일"
                birthday_line.setBackgroundResource(R.drawable.popup_line)
                check = true
            } else {
                //임신중 아님
                pregnant_on_off.setBackgroundResource(R.drawable.pregnant_off)
                editor.apply {
                    putBoolean("pregnant_status", false)
                    apply()
                    Log.d("test", "Pregnant1 : " + prefs.getBoolean("pregnant_status", false))
                }
                birthday_text.text = "출산 예정일"
                birthday_line.setBackgroundResource(R.drawable.gender_popup_line)
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
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24 이상일 경우 시스템 기본 테마 사용
            context = this
        }*/
        val datePickerDialog = DatePickerDialog(context, dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    fun showDatePicker(v: View) {
//        val newFragment = MyDatePickerFragment()
//        newFragment.show(getSupportFragmentManager(), "date picker")
        //https://www.zoftino.com/android-datepicker-example
    }
}
