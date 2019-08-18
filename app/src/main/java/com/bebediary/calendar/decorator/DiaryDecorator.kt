package com.bebediary.calendar.decorator

import com.bebediary.calendar.span.TextSpan
import com.bebediary.database.model.DiaryModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class DiaryDecorator(private val diaryModel: DiaryModel) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val diaryDate = Calendar.getInstance().apply {
            time = diaryModel.diary.date
        }

        return diaryDate.get(Calendar.YEAR) == day.year &&
                diaryDate.get(Calendar.MONTH) == day.month &&
                diaryDate.get(Calendar.DAY_OF_MONTH) == day.day
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(TextSpan(diaryModel.diary.content))
    }
}
