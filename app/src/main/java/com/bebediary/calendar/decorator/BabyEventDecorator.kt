package com.bebediary.calendar.decorator

import android.content.Context
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.bebediary.R
import com.bebediary.calendar.span.TextSpan
import com.bebediary.database.model.BabyModel
import com.bebediary.util.extension.eventDateToCalendarText
import com.bebediary.util.extension.format
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class BabyEventDecorator(
    private val context: Context,
    private val babyModel: BabyModel,
    private val from: Calendar
) :
    DayViewDecorator {

    private val textColor by lazy {
        ResourcesCompat.getColor(
            context.resources,
            R.color.calendarRed,
            context.theme
        )
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val diaryDate = Calendar.getInstance().apply {
            time = from.time
        }

        return diaryDate.get(Calendar.YEAR) == day.year &&
                diaryDate.get(Calendar.MONTH) == day.month &&
                diaryDate.get(Calendar.DAY_OF_MONTH) == day.day
    }

    override fun decorate(view: DayViewFacade) {
        Log.d(
            "Decorator",
            "${from.time.format("YYYY.MM.dd")}, ${babyModel.baby.eventDateToCalendarText(from)}"
        )

        view.addSpan(
            TextSpan(
                babyModel.baby.eventDateToCalendarText(from),
                color = textColor
            )
        )
    }
}
