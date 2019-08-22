package com.bebediary.checklist.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.check_list_header.view.*

class CheckListHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(title: String) {
        itemView.checkListHeaderTitle.text = title
    }

}