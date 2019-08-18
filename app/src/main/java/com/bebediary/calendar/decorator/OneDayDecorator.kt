package com.bebediary.calendar.decorator

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.res.ResourcesCompat
import com.bebediary.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/**
 * 오늘일때 배경 설정 및
 * 글씨 두깨 변경
 */
class OneDayDecorator(
        private val context: Context
) : DayViewDecorator {

    // Background Color
    private val backgroundDrawable by lazy {
        ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return CalendarDay.today() == day
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(StyleSpan(Typeface.BOLD))
        view.addSpan(RelativeSizeSpan(1.1f))
        view.addSpan(ForegroundColorSpan(Color.WHITE))
        view.setBackgroundDrawable(backgroundDrawable)
    }
}
