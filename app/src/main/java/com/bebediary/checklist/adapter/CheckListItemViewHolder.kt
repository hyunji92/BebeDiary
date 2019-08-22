package com.bebediary.checklist.adapter

import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.database.entity.CheckList
import kotlinx.android.synthetic.main.check_list_item.view.*

class CheckListItemViewHolder(
    itemView: View,
    private val onItemChangeListener: CheckListSection.OnItemChangeListener? = null
) : RecyclerView.ViewHolder(itemView) {

    var checkList: CheckList? = null

    init {
        itemView.checkListDoneCheckBox.setOnClickListener {
            val checkList = checkList ?: return@setOnClickListener
            val view = it as? CompoundButton ?: return@setOnClickListener
            onItemChangeListener?.onChangeCheckListComplete(checkList, view.isChecked)
        }
    }

    fun bind(checkList: CheckList) {
        this.checkList = checkList

        itemView.checkListDoneCheckBox.isChecked = checkList.isComplete
        itemView.checkListItemContent.text = checkList.content
    }

}