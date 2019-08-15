package com.bebediary.calendar

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import com.bebediary.R
import kotlinx.android.synthetic.main.activity_add_calendar.*
import kotlinx.android.synthetic.main.activity_add_calendar.baby_birthday
import kotlinx.android.synthetic.main.activity_add_calendar.date_picker_button
import java.text.SimpleDateFormat
import java.util.*

class AddCalendarActivity : AppCompatActivity() {

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
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_calendar)

        back_button.setOnClickListener {
            this.finish()
        }

        date_picker_button.setOnClickListener {
            datePicker()
        }

        add_calendar_button.setOnClickListener {
            this.finish()
        }

        delete_calendar_button.setOnClickListener {
            Toast.makeText(this, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()
            this.finish()
        }

        var isNoti = false
        noti_icon.setOnClickListener {
            if(isNoti){
                noti_icon.setBackgroundResource(R.drawable.noti_icon)
                isNoti = false
            } else {
                noti_icon.setBackgroundResource(R.drawable.noti_icon_off)
                isNoti = true
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
}
