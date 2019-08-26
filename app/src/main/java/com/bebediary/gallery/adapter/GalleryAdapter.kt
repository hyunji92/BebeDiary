package com.bebediary.gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bebediary.GlideApp
import com.bebediary.R
import com.bebediary.database.model.PhotoModel
import com.bebediary.util.extension.eventDateToCalendarText
import kotlinx.android.synthetic.main.item_gallery.view.*
import java.util.*

class GalleryAdapter(private val onClickListener: OnClickListener) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var photos: List<PhotoModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_gallery,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return photos.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = photos[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item: PhotoModel? = null
            set(value) {
                field = value
                if (value != null) {
                    bind(value)
                }
            }

        init {
            itemView.setOnClickListener {
                val photoModel = this.item ?: return@setOnClickListener
                onClickListener.onClick(photoModel = photoModel)
            }
        }

        private fun bind(item: PhotoModel) {
            val baby = item.babies.first()

            itemView.itemGalleryBabyName.text = baby.name
            itemView.itemGalleryBabyEventText.text =
                baby.eventDateToCalendarText(Calendar.getInstance().apply {
                    time = item.photo.createdAt
                })

            GlideApp.with(itemView)
                .load(item.photoAttachments.first().attachments.first().file)
                .centerCrop()
                .into(itemView.itemGalleryImageView)
        }
    }

    interface OnClickListener {
        fun onClick(photoModel: PhotoModel)
    }
}