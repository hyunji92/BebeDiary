package com.bebediary.baby.change.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.model.BabyModel
import kotlinx.android.synthetic.main.item_baby_change.view.*

class BabyChangeAdapter(
        private val babyChangeInterface: BabyChangeInterface
) : RecyclerView.Adapter<BabyChangeAdapter.ViewHolder>() {

    var items = arrayListOf<BabyModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_baby_change, parent, false))
    }

    override fun getItemCount(): Int = items.count() + 1


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items.getOrNull(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: BabyModel? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                } else {
                    bindAddBaby()
                }
            }

        init {
            itemView.setOnClickListener {
                val baby = item
                if (baby == null) {
                    babyChangeInterface.addBaby()
                } else {
                    babyChangeInterface.changeBaby(baby.baby)
                }
            }
        }

        private fun bindAddBaby() {
            GlideApp.with(itemView)
                    .load(R.drawable.add_baby_image)
                    .circleCrop()
                    .into(itemView.itemBabyChangeAvatar)
            itemView.itemBabyChangeName.text = "추가하기"
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