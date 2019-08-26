package com.bebediary.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.calendar.decorator.*
import com.bebediary.calendar.detail.CalendarDetailActivity
import com.bebediary.calendar.list.CalendarListActivity
import com.bebediary.database.model.BabyModel
import com.bebediary.database.model.DiaryModel
import com.bebediary.util.extension.isSaturday
import com.prolificinteractive.materialcalendarview.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import java.util.*


class CalendarFragment : Fragment(), LifecycleObserver, OnDateSelectedListener,
    OnMonthChangedListener {

    // Calendar Decorators
    private val saturdayDecorator by lazy { SaturdayDecorator(requireContext()) }
    private val sundayDecorator by lazy { SundayDecorator(requireContext()) }
    private val oneDayDecorator by lazy { OneDayDecorator(requireContext()) }

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (requireContext().applicationContext as MyApplication).db }

    // 현재 선택된 아이 정보
    private var currentBabyModel: BabyModel? = null

    // 다이어리 모델 리스트 저장
    private var diaryModels: List<DiaryModel> = listOf()

    // 아이 이벤트 데코레이터
    private var babyEventDecorators = arrayListOf<BabyEventDecorator>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Lifecycle Observer 추가
        viewLifecycleOwner.lifecycle.addObserver(this)

        return view
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        val view = view ?: return

        // 캘린더 리스트로 이동하
        view.calendarMoveToList.setOnClickListener {
            val babyModel = currentBabyModel ?: return@setOnClickListener
            val intent = Intent(requireContext(), CalendarListActivity::class.java)
                .putExtra("babyId", babyModel.baby.id)
            startActivity(intent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeCalendarView() {
        val view = view ?: return

        // 캘린더 뷰 설정
        view.calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
            .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        // 기본 데코레이터 설정
        view.calendarView.addDecorators(saturdayDecorator, sundayDecorator, oneDayDecorator)

        // 캘린더 날짜를 선택했을때
        view.calendarView.setOnDateChangedListener(this)

        // 월 변경 리스너
        view.calendarView.setOnMonthChangedListener(this)
    }

    /**
     * 현재 선택 되어있는 아이 정보를 가져온다
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchCurrentBaby() {
        db.babyDao().getSelected()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // 현재 아이 멤버 데이터로 저장
                    currentBabyModel = it

                    // 현재 아이의 다이어리 정보 업데이트
                    fetchBabyDiaries()
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = compositeDisposable.dispose()

    /**
     * 아이의 다이어리 리스트 요청
     */
    private fun fetchBabyDiaries() {
        val baby = currentBabyModel?.baby ?: return
        db.diaryDao().getBabyDiaries(baby.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // 다이어리 모델 리스트 저장
                    diaryModels = it

                    invalidateDiaryDecorators(it)
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 다이어리 이벤트 데코레이터 업데이트
     */
    private fun invalidateDiaryDecorators(diaryModels: List<DiaryModel>) {
        val view = view ?: return

        // 모든 데코레이터 제거
        view.calendarView.removeDecorators()

        // 다이어리 모델 정보로 데코레이터 생성 및 설정
        // 앞서 모든 데코레이터를 제거했기 때문에 기본 데코레이터 추가한다
        view.calendarView.addDecorators(
            *diaryModels.map { DiaryDecorator(it) }.toTypedArray(),
            sundayDecorator,
            saturdayDecorator,
            oneDayDecorator
        )

        // 달 변경 이벤트 생성
        onMonthChanged(view.calendarView, view.calendarView.currentDate)
    }

    /**
     * 캘린더 월이 변경 되었을 경우
     */
    override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) {

        // 데코레이터 제거
        babyEventDecorators.forEach { widget.removeDecorator(it) }
        babyEventDecorators.clear()

        val currentBabyModel = currentBabyModel ?: return

        // 데이터 업데이트 비동기처리
        Observable.fromCallable {
            // 이번달의 시작 날짜부터 끝 날짜 까지 반복해서
            val startOfMonth = Calendar.getInstance().apply {
                set(date.year, date.month, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 기본 날짜 설정
            val searchDate = Calendar.getInstance().apply {
                timeInMillis = startOfMonth.timeInMillis
            }

            for (dayOfMonth in 1..startOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {

                // 날짜 설정
                searchDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // 토요일 아니면 Continue
                if (!searchDate.time.isSaturday()) continue

                // 해당 토요일에 아무 일정이 없을 경우 아기 이벤트 정보 추가
                val hasAlreadyEvent = diaryModels
                    .filter { it.diary.date.time >= searchDate.timeInMillis }
                    .findLast {
                        val diaryDate =
                            Calendar.getInstance().apply { timeInMillis = it.diary.date.time }
                        diaryDate.get(Calendar.YEAR) == searchDate.get(Calendar.YEAR) &&
                                diaryDate.get(Calendar.MONTH) == searchDate.get(Calendar.MONTH) &&
                                diaryDate.get(Calendar.DAY_OF_MONTH) == searchDate.get(Calendar.DAY_OF_MONTH)
                    }
                if (hasAlreadyEvent != null) continue

                // Decorator 추가
                babyEventDecorators.add(
                    BabyEventDecorator(
                        requireContext(),
                        babyModel = currentBabyModel,
                        from = Calendar.getInstance().apply {
                            set(
                                searchDate.get(Calendar.YEAR),
                                searchDate.get(Calendar.MONTH),
                                searchDate.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                    )
                )
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { widget.addDecorators(babyEventDecorators) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 유저가 캘린더에서 날짜를 선택했을 경우
     */
    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        // 캘린더 상태일때
        if (selected) {
            widget.clearSelection()

            // 일정 추가 액티비티 실행
            val activity = activity ?: return
            val babyModel = currentBabyModel ?: return

            // 기존에 등록 되어있는 일정이 있으면 상세보기 화면으로 이동
            val hasDiary = diaryModels.find {
                val diaryDate = Calendar.getInstance().apply { time = it.diary.date }
                diaryDate.get(Calendar.YEAR) == date.year &&
                        diaryDate.get(Calendar.MONTH) == date.month &&
                        diaryDate.get(Calendar.DAY_OF_MONTH) == date.day
            }

            // 다이어리가 있는 경우 상세 화면으로, 없는 경우 추가 화면으로 이동
            val intent = if (hasDiary != null) {
                Intent(activity, CalendarDetailActivity::class.java)
                    .putExtra("diaryId", hasDiary.diary.id)
                    .putExtra("babyId", babyModel.baby.id)
            } else {
                // 인텐트 생성
                Intent(activity, AddCalendarActivity::class.java)
                    .putExtra("babyId", babyModel.baby.id)
                    .putExtra("year", date.year)
                    .putExtra("month", date.month)
                    .putExtra("day", date.day)
            }
            startActivity(intent)
        }
    }
}
