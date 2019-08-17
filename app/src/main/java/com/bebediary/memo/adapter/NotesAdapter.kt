package com.bebediary.memo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import com.bebediary.database.entity.Note
import kotlinx.android.synthetic.main.note_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(context: Context) : RecyclerView.Adapter<NotesAdapter.BeanHolder>() {

    // 날짜 포멧
    private val dateFormat by lazy { SimpleDateFormat("YYYY.MM.dd", Locale.getDefault()) }

    var list = arrayListOf<Note>()

    private val onNoteItemClick: OnNoteItemClick

    init {
        this.onNoteItemClick = context as OnNoteItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeanHolder {
        return BeanHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: BeanHolder, position: Int) {
        val item = list[position]
        holder.textViewTitle.text = item.title
        holder.textViewContent.text = item.content
        holder.textViewDate.text = dateFormat.format(item.createdAt)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class BeanHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var textViewContent: TextView
        var textViewTitle: TextView
        var textViewDate: TextView

        init {
            itemView.setOnClickListener(this)
            textViewContent = itemView.item_text
            textViewTitle = itemView.tv_title
            textViewDate = itemView.tv_date
        }

        override fun onClick(view: View) {
            onNoteItemClick.onNoteClick(adapterPosition)
        }
    }

    interface OnNoteItemClick {
        fun onNoteClick(pos: Int)
    }
}
