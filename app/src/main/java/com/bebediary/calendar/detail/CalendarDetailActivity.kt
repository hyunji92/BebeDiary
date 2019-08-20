package com.bebediary.calendar.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.GlideApp
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.calendar.detail.adapter.CalendarDetailAttachmentAdapter
import com.bebediary.database.model.DiaryModel
import com.bebediary.util.extension.eventDateToText
import com.bebediary.util.extension.format
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_calendar_detail.*

class CalendarDetailActivity : AppCompatActivity(), LifecycleObserver {

    // 상세보기할 다이어리 아이디
    private val diaryId get() = intent.getLongExtra("diaryId", -1L)

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // Database
    private val db by lazy { (application as MyApplication).db }

    // Composite Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // Adapter
    private val calendarDetailAttachmentAdapter by lazy { CalendarDetailAttachmentAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_detail)

        // 다이어리 정보가 없는 경우
        if (diaryId == -1L || babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        calendarDetailImageRecyclerView.adapter = calendarDetailAttachmentAdapter
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchDiary() {
        db.diaryDao().getDiary(diaryId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateDiaryView(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 다이어리 뷰 업데이트
     */
    private fun invalidateDiaryView(diaryModel: DiaryModel) {

        // 캘리더 날짜 설정
        calendarDetailDateView.text =
            diaryModel.diary.date.format("YYYY. MM. dd EEE").toUpperCase()

        // 아이 정보
        val baby = diaryModel.babies.first()
        calendarDetailBabyEventView.text = baby.eventDateToText()

        // 캘린더 컨텐츠 설정
        calendarDetailContentView.text = diaryModel.diary.content

        // 첨부파일 갯수
        val attachmentCount = diaryModel.diaryAttachments.count()

        // 첨부파일 뷰 가시성 설정
        calendarDetailMainImage.isVisible = attachmentCount > 0
        calendarDetailImageRecyclerView.isVisible = attachmentCount > 1

        // 이미지 보여줌
        if (attachmentCount > 0) {
            GlideApp.with(this)
                .load(diaryModel.diaryAttachments[0].attachments.first().file)
                .centerCrop()
                .into(calendarDetailMainImage)
        }

        // 리사이클러뷰에 이미지 보여줌
        if (attachmentCount > 1) {
            val recyclerViewAttachments =
                diaryModel.diaryAttachments.subList(1, diaryModel.diaryAttachments.count())
            calendarDetailAttachmentAdapter.items = recyclerViewAttachments
            calendarDetailAttachmentAdapter.notifyDataSetChanged()
        }
    }
}
