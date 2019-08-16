package com.bebediary.memo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import com.bebediary.database.entity.Note

class NotesAdapter(context: Context) : RecyclerView.Adapter<NotesAdapter.BeanHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var list = arrayListOf<Note>()

    private val onNoteItemClick: OnNoteItemClick

    init {
        this.onNoteItemClick = context as OnNoteItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeanHolder {
        return BeanHolder(layoutInflater.inflate(R.layout.note_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: BeanHolder, position: Int) {
        holder.textViewTitle.text = list[position].title
        holder.textViewContent.text = list[position].content

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class BeanHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        internal var textViewContent: TextView
        internal var textViewTitle: TextView

        init {
            itemView.setOnClickListener(this)
            textViewContent = itemView.findViewById(R.id.item_text)
            textViewTitle = itemView.findViewById(R.id.tv_title)
        }

        override fun onClick(view: View) {
            onNoteItemClick.onNoteClick(adapterPosition)
        }
    }

    interface OnNoteItemClick {
        fun onNoteClick(pos: Int)
    }
}
