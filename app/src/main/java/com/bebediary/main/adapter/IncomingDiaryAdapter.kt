package com.bebediary.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import com.bebediary.database.model.DiaryModel
import kotlinx.android.synthetic.main.item_main_incoming_diary.view.*
import java.text.SimpleDateFormat
import java.util.*


class IncomingDiaryAdapter(
        private val onItemChangeListener: OnItemChangeListener
) : RecyclerView.Adapter<IncomingDiaryAdapter.ViewHolder>() {

    var items = arrayListOf<DiaryModel>()

    // 날짜 포맷
    private val dateFormat by lazy { SimpleDateFormat("YYYY.MM.dd", Locale.getDefault()) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main_incoming_diary, parent, false))
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var item: DiaryModel? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        init {
            itemView.itemMainIncomingDiaryCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                val item = item ?: return@setOnCheckedChangeListener
                onItemChangeListener.onChangeDiaryComplete(item, isChecked)
            }
        }

        private fun bind(diaryModel: DiaryModel) {
            itemView.itemMainIncomingDiaryCompleteLine.isVisible = diaryModel.diary.isComplete

            itemView.itemMainIncomingDiaryCheckbox.isChecked = diaryModel.diary.isComplete
            itemView.itemMainIncomingDiaryContent.text = diaryModel.diary.content
            itemView.itemMainIncomingDiaryDate.text = dateFormat.format(diaryModel.diary.date)
        }
    }

    interface OnItemChangeListener {
        fun onChangeDiaryComplete(diaryModel: DiaryModel, isComplete: Boolean)
    }
}