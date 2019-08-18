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
import com.bebediary.calendar.decorator.DiaryDecorator
import com.bebediary.calendar.decorator.OneDayDecorator
import com.bebediary.calendar.decorator.SaturdayDecorator
import com.bebediary.calendar.decorator.SundayDecorator
import com.bebediary.database.model.BabyModel
import com.bebediary.database.model.DiaryModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import java.util.*

class CalendarFragment : Fragment(), LifecycleObserver, OnDateSelectedListener {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Lifecycle Observer 추가
        viewLifecycleOwner.lifecycle.addObserver(this)

        return view
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        val view = view ?: return
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
    }

    /**
     * 유저가 캘린더에서 날짜를 선택했을 경우
     */
    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        // 캘린더 상태일때
        if (selected) {
            widget.clearSelection()

            // 일정 추가 액티비티 실행
            val activity = activity ?: return
            val babyModel = currentBabyModel ?: return

            // 인텐트 생성
            val intent = Intent(activity, AddCalendarActivity::class.java)
                    .putExtra("babyId", babyModel.baby.id)
                    .putExtra("year", date.year)
                    .putExtra("month", date.month)
                    .putExtra("day", date.day)
            activity.startActivity(intent)
        }
    }
}
