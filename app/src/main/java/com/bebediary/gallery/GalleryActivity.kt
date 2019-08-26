package com.bebediary.gallery

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.model.PhotoModel
import com.bebediary.gallery.adapter.GalleryAdapter
import com.bebediary.gallery.detail.GalleryDetailActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity(), LifecycleObserver, GalleryAdapter.OnClickListener {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // Adapter
    private val galleryAdapter by lazy { GalleryAdapter(this) }

    // Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // Database
    private val db by lazy { (application as MyApplication).db }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        // 아이 정보 검증
        if (babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            title = "Album"
        }

        galleryRecyclerView.adapter = galleryAdapter
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchPhotos() {
        db.photoDao().getBabyPhotos(babyId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidatePhotos(it) }, { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    private fun invalidatePhotos(photos: List<PhotoModel>) {
        galleryAdapter.photos = photos
        galleryAdapter.notifyDataSetChanged()
    }

    override fun onClick(photoModel: PhotoModel) {
        val intent = Intent(this, GalleryDetailActivity::class.java)
        intent.putExtra("path", photoModel.photoAttachments.first().attachments.first().file.path)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return true
    }
}
