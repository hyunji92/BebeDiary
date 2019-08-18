package com.bebediary.calendar.decorator

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.res.ResourcesCompat
import com.bebediary.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

/**
 * 토요일 표시 해주는 데코레이터
 */
class SaturdayDecorator(private val context: Context) : DayViewDecorator {

    private val calendar = Calendar.getInstance()
    private val textColor by lazy { ResourcesCompat.getColor(context.resources, R.color.mainColor, context.theme) }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(textColor))
    }
}
