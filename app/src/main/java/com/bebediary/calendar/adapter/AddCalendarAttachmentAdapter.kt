package com.bebediary.calendar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.entity.Attachment
import kotlinx.android.synthetic.main.item_add_calendar_attachment.view.*

class AddCalendarAttachmentAdapter(
        private val itemChangeListener: OnItemChangeListener
) : RecyclerView.Adapter<AddCalendarAttachmentAdapter.ViewHolder>() {

    var items = arrayListOf<Attachment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_add_calendar_attachment, parent, false))
    }

    override fun getItemCount() = items.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: Attachment? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        init {

            // 삭제 버튼 클릭시
            itemView.itemAddCalendarAttachmentRemove.setOnClickListener {
                val item = item ?: return@setOnClickListener
                itemChangeListener.onRemoveAttachment(item)
            }
        }

        private fun bind(attachment: Attachment) {

            // 이미지 설정
            GlideApp.with(itemView)
                    .load(attachment.file)
                    .centerCrop()
                    .into(itemView.itemAddCalendarAttachmentImageView)
        }
    }

    interface OnItemChangeListener {
        fun onRemoveAttachment(attachment: Attachment)
    }

}