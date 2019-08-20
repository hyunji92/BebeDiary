package com.bebediary.calendar.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.model.DiaryAttachmentModel
import kotlinx.android.synthetic.main.item_calendar_detail_attachment.view.*

class CalendarDetailAttachmentAdapter : RecyclerView.Adapter<CalendarDetailAttachmentAdapter.ViewHolder>() {

    var items = listOf<DiaryAttachmentModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_calendar_detail_attachment,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: DiaryAttachmentModel? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        private fun bind(item: DiaryAttachmentModel) {
            GlideApp.with(itemView)
                .load(item.attachments.first().file)
                .centerCrop()
                .into(itemView.itemCalendarDetailAttachmentImageView)
        }
    }
}