package com.bebediary.calendar

import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bebediary.R
import com.bebediary.calendar.decorators.EventDecorator
import com.bebediary.calendar.decorators.OneDayDecorator
import com.bebediary.calendar.decorators.SaturdayDecorator
import com.bebediary.calendar.decorators.SundayDecorator
import com.bebediary.register.BabyRegisterActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.*
import java.util.concurrent.Executors

class CalendarFragment : Fragment() {

    /*internal var time: String? = null
    internal var kcal: String? = null
    internal var menu: String? = null*/

    private val oneDayDecorator = OneDayDecorator(activity)
    internal var cursor: Cursor? = null
    internal lateinit var materialCalendarView: MaterialCalendarView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_calendar, container, false)
        materialCalendarView = view.findViewById(R.id.calendarView) as MaterialCalendarView

        materialCalendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
            .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        materialCalendarView.addDecorators(
            SundayDecorator(activity),
            SaturdayDecorator(activity),
            oneDayDecorator
        )

        val result = arrayOf("2019,07,18", "2019,07,08", "2019,05,28", "2017,06,18")

        ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor())

        materialCalendarView.setOnDateChangedListener { widget, date, selected ->
            val Year = date.year
            val Month = date.month + 1
            val Day = date.day

            Log.i("Year test", Year.toString() + "")
            Log.i("Month test", Month.toString() + "")
            Log.i("Day test", Day.toString() + "")

            val shot_Day = "$Year,$Month,$Day"

            Log.i("shot_Day test", shot_Day + "")
            materialCalendarView.clearSelection()

            val intent = Intent(activity, AddCalendarActivity::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity?.startActivity(intent)

            Toast.makeText(activity, shot_Day, Toast.LENGTH_SHORT).show()
        }
        return view
    }


    private inner class ApiSimulator internal constructor(internal var Time_Result: Array<String>) :
        AsyncTask<Void, Void, List<CalendarDay>>() {

        override fun doInBackground(vararg voids: Void): List<CalendarDay> {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            val calendar = Calendar.getInstance()
            val dates = ArrayList<CalendarDay>()

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for (i in Time_Result.indices) {
                val day = CalendarDay.from(calendar)
                val time = Time_Result[i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val year = Integer.parseInt(time[0])
                val month = Integer.parseInt(time[1])
                val dayy = Integer.parseInt(time[2])

                dates.add(day)
                calendar.set(year, month - 1, dayy)
            }



            return dates
        }

        override fun onPostExecute(calendarDays: List<CalendarDay>) {
            super.onPostExecute(calendarDays)

            if (activity!!.isFinishing) {
                return
            }

            materialCalendarView.addDecorator(
                EventDecorator(
                    activity!!.resources.getColor(R.color.calendarRed),
                    calendarDays,
                    activity
                )
            )
            //materialCalendarView.addDecorator(EventDecorator(activity!!.resources.getString(R.string.title_home),calendarDays, activity))
        }
    }

}
