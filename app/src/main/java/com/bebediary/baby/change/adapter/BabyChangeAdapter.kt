package com.bebediary.baby.change.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.model.BabyModel
import kotlinx.android.synthetic.main.item_baby_change.view.*

class BabyChangeAdapter : RecyclerView.Adapter<BabyChangeAdapter.ViewHolder>() {

    var items = arrayListOf<BabyModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_baby_change, parent, false))
    }

    override fun getItemCount(): Int = items.count()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: BabyModel? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        private fun bind(baby: BabyModel) {
            GlideApp.with(itemView)
                    .load(baby.photos.first().file)
                    .circleCrop()
                    .into(itemView.itemBabyChangeAvatar)
            itemView.itemBabyChangeName.text = baby.baby.name
        }
    }

}