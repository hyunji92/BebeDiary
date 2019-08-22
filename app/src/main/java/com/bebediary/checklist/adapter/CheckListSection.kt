package com.bebediary.checklist.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.R
import com.bebediary.database.entity.CheckList
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

class CheckListSection(
    private val title: String,
    private val items: List<CheckList>,
    private val onItemChangeListener: OnItemChangeListener? = null
) : StatelessSection(
    SectionParameters.builder()
        .itemResourceId(R.layout.check_list_item)
        .headerResourceId(R.layout.check_list_header)
        .build()
) {

    override fun getContentItemsTotal() = items.count()

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? CheckListItemViewHolder)?.bind(items[position])
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        (holder as? CheckListHeaderViewHolder)?.bind(title)
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return CheckListHeaderViewHolder(view)
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return CheckListItemViewHolder(view, onItemChangeListener)
    }

    interface OnItemChangeListener {
        fun onChangeCheckListComplete(checkList: CheckList, isComplete: Boolean)
    }

}