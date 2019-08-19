package com.bebediary.calendar.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import kotlinx.android.synthetic.main.item_calendar_list_header.view.*
import java.util.*

class CalendarListHeaderAdapter(
        private val year: Int,
        private val month: Int
) : RecyclerView.Adapter<CalendarListHeaderAdapter.ViewHolder>() {

    // 달력 최초 시작 포지션
    val startPosition
        get() = Int.MAX_VALUE / 2

    // 달력 최초 시작 날짜 설정
    private val startCalendar
        get() = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_list_header, parent, false))
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // position - startPosition > 0 월 증가 else 월 감소
        holder.bind(calculateCalendar(position))
    }

    fun calculateCalendar(position: Int): Calendar {
        val calendar = startCalendar
        calendar.add(Calendar.MONTH, position - startPosition)
        return calendar

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(calendar: Calendar) {
            itemView.itemCalendarListHeaderMonth.text = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            itemView.itemCalendarListHeaderYear.text = String.format("%d", calendar.get(Calendar.YEAR))
        }

    }
}