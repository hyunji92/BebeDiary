package com.bebediary.calendar.decorator

import android.app.Activity
import android.graphics.drawable.Drawable
import com.bebediary.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*

/**
 * Decorate several days with a dot
 */
class EventDecorator : DayViewDecorator {

    private val drawable: Drawable
    private var color: Int = 0
    private var dates: HashSet<CalendarDay>? = null

    private var contents: String = ""

    constructor(color: Int, dates: Collection<CalendarDay>, context: Activity) {
        drawable = context.resources.getDrawable(R.drawable.more)
        this.color = color
        this.dates = HashSet(dates)
    }

    constructor(color: Int, string: String, dates: Collection<CalendarDay>, context: Activity) {
        drawable = context.resources.getDrawable(R.drawable.more)
        this.color = color
        this.contents = string
        this.dates = HashSet(dates)
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates!!.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(drawable)
        view.addSpan(DotSpan(8f, color)) // 날자밑에 점
        view.addSpan(contents)
    }
}
