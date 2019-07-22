package com.bebediary.register

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.Window
import com.bebediary.R
import kotlinx.android.synthetic.main.activity_baby_register.*
import java.text.SimpleDateFormat
import java.util.*



class BabyRegisterActivity : Activity() {

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
        var month :String = ""
        var day :String = ""

        if(monthOfYear < 10){
            month = "0$monthOfYear"
        }
        if(dayOfMonth < 10) {
            day = "0$dayOfMonth"
        }
        editor.putString("baby_birthday" , year.toString() + month + day)
        editor.apply()

        Log.d("test", "test Date birthday:" + prefs.getString("baby_birthday", formatDate))
    }

    lateinit var prefs :SharedPreferences
    lateinit var editor :SharedPreferences.Editor

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
        //사진 등록 ( 카메라, 갤러리 )
        //https://black-jin0427.tistory.com/120
        //등록한 사진 저장
        //https://m.blog.naver.com/PostView.nhn?blogId=kkh32490&logNo=220287551618&proxyReferer=https%3A%2F%2Fwww.google.com%2F
        //http://www.masterqna.com/android/16166/%EC%84%A0%ED%83%9D%EB%90%9C-bipmap%EC%9D%84sharedpreferences%EC%97%90-%EC%A0%80%EC%9E%A5%ED%95%98%EB%8A%94%EB%B2%95-%E3%85%9C%E3%85%9C
        //http://android-steps.blogspot.com/2015/08/profile-page-save-image-data-in.html
        //이걸로 하면 될듯 : https://github.com/martinsing/Image-Save-And-Retrieve-App


    }

    // 이름, 성별, 몇일
    fun saveBabyInfo(){
        if (baby_name.text.toString() != "" || baby_name.text.toString() != null){
            editor.apply {
                putString("baby_name" , baby_name.text.toString())
                apply()
            }
        }
        Log.d("test" ,"EditText nane : " + baby_name.text.toString())

        if(baby_gender.text.toString() != "" || baby_gender.text.toString() != null) {
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
            if (!check){
                //임신중
                pregnant_on_off.setBackgroundResource(R.drawable.pregnant_on)
                editor.apply {
                    putBoolean("pregnant_status", true)
                    apply()
                    Log.d("test" ,"Pregnant2 : " + prefs.getBoolean("pregnant_status", false))
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
                    Log.d("test" ,"Pregnant1 : " + prefs.getBoolean("pregnant_status", false))
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
