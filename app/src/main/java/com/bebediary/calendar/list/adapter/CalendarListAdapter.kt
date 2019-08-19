package com.bebediary.calendar.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.model.DiaryModel
import kotlinx.android.synthetic.main.item_calendar_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarListAdapter : RecyclerView.Adapter<CalendarListAdapter.ViewHolder>() {

    var items: Map<Date, DiaryModel?>? = null

    private val dateFormat by lazy { SimpleDateFormat("YYYY. MM. dd EEE", Locale.ENGLISH) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_list, parent, false))
    }

    override fun getItemCount() = items?.count() ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keys = items?.keys?.toList() ?: return
        val key = keys[position]

        // 홀더 업데이트
        holder.date = key
        holder.diaryModel = items?.get(key)
        holder.invalidate()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var date: Date? = null
        var diaryModel: DiaryModel? = null

        // Date Colors
        private val dateDefaultColor = 0xFF5A5A5A.toInt()
        private val dateSaturdayColor = 0xFF6BC4C8.toInt()
        private val dateSundayColor = 0xFFFF8484.toInt()

        fun invalidate() {
            val date = date ?: return
            val hasDiary = diaryModel != null

            // 캘린더 날짜
            val calendar = Calendar.getInstance().apply {
                time = date
            }

            // 캘린더 리스트 날짜 설정
            itemView.calendarListDateView.text = dateFormat.format(date).toUpperCase()
            itemView.calendarListDateView.setTextColor(
                    when {
                        calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY -> dateSaturdayColor
                        calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> dateSundayColor
                        else -> dateDefaultColor
                    }
            )

            // Visibility 설정
            itemView.calendarListCheckbox.isVisible = hasDiary && false
            itemView.calendarListContentView.isVisible = hasDiary
            itemView.calendarListDayView.isVisible = hasDiary
            itemView.calendarListImageView.isVisible = hasDiary && diaryModel?.diaryAttachments?.count() ?: 0 > 0

            // 텍스트 설정
            itemView.calendarListContentView.text = diaryModel?.diary?.content

            // 이미지 있을 경우 설정
            val diaryAttachment = diaryModel?.diaryAttachments?.firstOrNull()
            if (diaryAttachment != null) {
                GlideApp.with(itemView)
                        .load(diaryAttachment.attachments.first().file)
                        .centerCrop()
                        .into(itemView.calendarListImageView)
            }
        }
    }
}