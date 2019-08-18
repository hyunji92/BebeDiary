package com.bebediary.calendar.decorators;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.text.style.ForegroundColorSpan;
import com.bebediary.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

/**
 * Created by samsung on 2017-04-12.
 */

public class SundayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();
    private Context context;

    public SundayDecorator(FragmentActivity activity) {
        context = activity;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.calendarRed)));
    }
}