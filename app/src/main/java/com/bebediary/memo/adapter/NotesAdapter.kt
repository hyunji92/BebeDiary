package com.bebediary.memo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import com.bebediary.database.entity.Note
import kotlinx.android.synthetic.main.note_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(context: Context) : RecyclerView.Adapter<NotesAdapter.BeanHolder>() {

    // 날짜 포멧
    private val dateFormat by lazy { SimpleDateFormat("YYYY.MM.dd", Locale.getDefault()) }

    // 아이템
    var items = arrayListOf<Note>()

    private val onNoteItemClick: OnNoteItemClick

    init {
        this.onNoteItemClick = context as OnNoteItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeanHolder {
        return BeanHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: BeanHolder, position: Int) {
        val item = items[position]
        holder.note = item
    }

    override fun getItemCount() = items.count()

    inner class BeanHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 메모 데이터
        var note: Note? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        init {
            itemView.setOnClickListener {
                val item = note
                if (item != null) {
                    onNoteItemClick.onNoteClick(item)
                }
            }
        }

        private fun bind(note: Note) {
            itemView.tv_title.text = note.title
            itemView.item_text.text = note.content
            itemView.tv_date.text = dateFormat.format(note.createdAt)
        }
    }

    interface OnNoteItemClick {
        fun onNoteClick(note: Note)
    }
}
