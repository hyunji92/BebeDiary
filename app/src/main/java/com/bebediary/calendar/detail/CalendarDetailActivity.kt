package com.bebediary.calendar.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.model.DiaryModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

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

    }
}
