package com.bebediary

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bebediary.memo.MemoActivity
import com.bebediary.register.BabyRegisterActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //등록한 아기가 업다면
        main_top_layout.visibility= View.GONE
        comming_schedule_layout.visibility = View.GONE
        main_image_off_button.visibility = View.GONE
        first_add_baby_layout.visibility = View.VISIBLE
        add_baby.apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, BabyRegisterActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

        //MemoActiviy
        /*memo_icon.apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, MemoActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        memo_bottom_button.apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, MemoActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }*/

        //CalendarActivity

    }


/*
중요 사이트
https://promobile.tistory.com/193
https://dpdpwl.tistory.com/3
https://github.com/dolsanta/Sample_Calendar
https://dpdpwl.tistory.com/3
* */
    /*참고 사이트

    * https://webnautes.tistory.com/1216

https://medium.com/@Patel_Prashant_/android-custom-calendar-with-events-fa48dfca8257

https://dpdpwl.tistory.com/3

https://www.google.com/search?q=android+calendarview+예제&rlz=1C5CHFA_enKR851KR851&oq=android+calendarview+예제&aqs=chrome..69i57.10103j0j4&sourceid=chrome&ie=UTF-8

https://androi.tistory.com/136


https://zeph1e.tistory.com/42

https://drcarter.tistory.com/152

https://www.dev2qa.com/android-one-time-repeat-alarm-example/

https://gogorchg.tistory.com/entry/Android-AlarmManager%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-Schedule-%EA%B4%80%EB%A6%AC

https://jamesdreaming.tistory.com/102

https://namget.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%BB%A4%EC%8A%A4%ED%85%80-%EB%8B%AC%EB%A0%A5-%EC%98%88%EC%A0%9C-Android-Custom-CalendarView-Example

https://github.com/kuluna/CalendarViewPager

https://hatti.tistory.com/entry/android-calendar

https://github.com/hnhariat/calendar

https://woochan-dev.tistory.com/27

https://www.youtube.com/watch?v=xs5406vApTo
    * */
}
