package com.bebediary.gallery.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bebediary.GlideApp
import com.bebediary.R
import kotlinx.android.synthetic.main.activity_gallery_detail.*

class GalleryDetailActivity : AppCompatActivity() {

    // 아이 정보
    private val path: String?
        get() = intent.getStringExtra("path")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_detail)

        // 경로가 넘어오지 않았을때
        if (path == null) {
            finish()
            return
        }

        // 이미지 로딩
        GlideApp.with(this)
            .load(path)
            .into(galleryDetailImageView)
    }
}