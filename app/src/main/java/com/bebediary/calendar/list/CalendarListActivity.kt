package com.bebediary.calendar.list

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.calendar.detail.CalendarDetailActivity
import com.bebediary.calendar.list.adapter.CalendarListAdapter
import com.bebediary.calendar.list.adapter.CalendarListHeaderAdapter
import com.bebediary.database.model.DiaryModel
import com.bebediary.util.recyclerview.SnapPagerScrollListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_calendar_list.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * 캘린더 정보를 리스트로 볼 수 있는 액티비티
 */
class CalendarListActivity : AppCompatActivity(), LifecycleObserver, CalendarListAdapter.OnItemClickListener {

    // Year, Month
    // 무조건 동시에 업데이트 되므로 월 변경시 날짜 변경 작업 실행
    private var year = 0
    private var month = 0
        set(value) {
            field = value
            invalidateToolbarTitle()
        }

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // RecyclerView Header Adapter
    private val calendarListHeaderAdapter by lazy { CalendarListHeaderAdapter(year, month) }
    private val calendarListHeaderPagerSnapHelper by lazy { PagerSnapHelper() }

    // RecyclerView Adapter
    private val calendarListAdapter by lazy { CalendarListAdapter(this) }

    // 페이지 변경 Subject
    private val dateChangeSubject by lazy { PublishSubject.create<Calendar>() }

    // Composite Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }
    private var diariesDisposable: Disposable? = null

    // Database
    private val db by lazy { (application as MyApplication).db }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_list)

        // 아이 정보가 넘어오지 않았을때 화면 종료
        if (this.babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        setSupportActionBar(toolbar)

        // 최초에 들어왔을때 연도 월 데이터 입력
        year = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR))
        month = intent.getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH))

        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT)
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeHeader() {

        // 어뎁터 설정
        calendarListHeaderView.adapter = calendarListHeaderAdapter

        // RecyclerView 포지션 변경
        // 최초 년,월로 변경
        calendarListHeaderView.scrollToPosition(calendarListHeaderAdapter.startPosition)

        // 페이징 추가
        calendarListHeaderPagerSnapHelper.attachToRecyclerView(calendarListHeaderView)

        // 헤더 페이지 변경시 날짜 변경
        calendarListHeaderView.addOnScrollListener(SnapPagerScrollListener(
            calendarListHeaderPagerSnapHelper,
            SnapPagerScrollListener.ON_SETTLED,
            true,
            object : SnapPagerScrollListener.OnChangeListener {
                override fun onSnapped(position: Int) {
                    val calendar = calendarListHeaderAdapter.calculateCalendar(position)
                    year = calendar.get(Calendar.YEAR)
                    month = calendar.get(Calendar.MONTH)

                    // 데이트 변경 체인지 데이터 넘김
                    dateChangeSubject.onNext(calendar)
                }
            }
        ))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeRecyclerView() {

        // 어뎁터 설정
        calendarListRecyclerView.adapter = calendarListAdapter

        // Divider
        calendarListRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    /**
     * 날짜 변경 Observable 구독
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun subscribeDateChange() {
        dateChangeSubject
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { subscribeDiaries(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }

    }

    /**
     * 특정 월의 다이어리 리스트 요청해서 데이터 업데이트
     */
    private fun subscribeDiaries(calendar: Calendar) {
        diariesDisposable?.dispose()

        // Date Format
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        // 다이어리 시작 날짜
        val startedAt = Calendar.getInstance()
            .apply {
                timeInMillis = calendar.timeInMillis
                set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0
                )
                set(Calendar.MILLISECOND, 0)
            }

        // 다이어리 끝 날짜
        val endedAt = Calendar.getInstance()
            .apply {
                timeInMillis = startedAt.timeInMillis
                add(Calendar.MONTH, 1)
            }

        // 새로운 날짜로 다이어리 구독
        diariesDisposable = db.diaryDao()
            .getDiaryByDateRange(babyId, startedAt.timeInMillis, endedAt.timeInMillis)
            .map {
                it.map { model -> dateFormat.format(model.diary.date) to model }.toMap()
            }
            .map {
                val items = sortedMapOf<Date, DiaryModel?>()

                // 시작 날짜 달의 모든 날짜의 데이터 확인해서 데이터 생성
                val date = Calendar.getInstance().apply { timeInMillis = startedAt.timeInMillis }
                for (dayOfMonth in 1..startedAt.getActualMaximum(Calendar.DAY_OF_MONTH)) {

                    // 날짜 설정
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // 해당 월 일에 다이어리 데이터가 있는지 확인해서 있으면 추가 없으면 제외 시킨다
                    val diaryModel = it[dateFormat.format(date.time)]
                    items[date.time] = diaryModel
                }

                return@map items
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateDiaries(it) },
                { it.printStackTrace() }
            )
    }

    /**
     * Date - DiaryModel Map 형식의 아이템을 어뎁터에 설정
     *
     */
    private fun invalidateDiaries(items: Map<Date, DiaryModel?>) {
        calendarListAdapter.items = items
        calendarListAdapter.notifyDataSetChanged()
    }

    /**
     * 툴바 제목 변경
     */
    private fun invalidateToolbarTitle() {
        collapsingToolbarLayout.title = String.format("%d-%02d", year, month + 1)
    }

    /**
     * 캘린더 리스트에서 아이템 클릭
     */
    override fun onClick(date: Date, diaryModel: DiaryModel?) {
        val model = diaryModel ?: return

        val intent = Intent(this, CalendarDetailActivity::class.java)
            .putExtra("diaryId", model.diary.id)
            .putExtra("babyId", babyId)
        startActivity(intent)
    }
}