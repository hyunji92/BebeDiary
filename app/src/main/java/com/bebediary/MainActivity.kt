package com.bebediary

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.baby.change.BabyChangeActivity
import com.bebediary.calendar.CalendarFragment
import com.bebediary.database.model.BabyModel
import com.bebediary.memo.NoteListActivity
import com.bebediary.register.BabyRegisterActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contents_main.*
import kotlinx.android.synthetic.main.header_navigatioin.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LifecycleObserver {

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (application as MyApplication).db }

    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    private val fragmentManager = supportFragmentManager

    private val calendarFragment = CalendarFragment()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = fragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.navigation_camera -> {
                Log.d("test", "test camera")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_calendar -> {
                Log.d("test", "test calendar")
                main_all_Scrollview.visibility = View.GONE
                frame_layout.visibility = View.VISIBLE
                transaction.replace(R.id.frame_layout, calendarFragment).commitAllowingStateLoss();
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_checklist -> {
                Log.d("test", "test checklist")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_information -> {
                Log.d("test", "test information")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_memo -> {
                Log.d("test", "test memo")
                val intent = Intent(this@MainActivity, NoteListActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.apply {
            isItemHorizontalTranslationEnabled = false
            setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        }

        // 메인 네비게이션 클릭 리스너
        nv_main_navigation_root.setNavigationItemSelectedListener(this)

        prefs = getSharedPreferences("baby_info", Context.MODE_PRIVATE)
        editor = prefs.edit()

        babyInfoSetting()

        // Lifecycle Observer
        lifecycle.addObserver(this)
    }

    /**
     * 현재 선택된 아이의 정보를 가져와서
     * 드로어 헤더 업데이트
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchCurrentBaby() {
        db.babyDao().getCurrent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            // Drawer에 아이 정보 업데이트
                            invalidateNavigationHeader(it)

                            // 아이 뷰 업데이트
                            invalidateBabyView(it)

                            // Logging
                            Log.d("Main", "현재 선택된 아이 : $it")
                        },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    /**
     * 뷰 기본 동작 설정
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {
        main_image_off_button.setOnClickListener {
            val isOpen = it.tag as? Boolean == true
            main_image_off_button.setBackgroundResource(if (isOpen) R.drawable.main_image_on else R.drawable.main_image_off)
            no_register_baby_image_layout.isVisible = isOpen
            real_baby_image.isVisible = isOpen

            // 태그 업데이트
            it.tag = isOpen.not()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = compositeDisposable.dispose()

    /**
     * 아이 뷰 업데이트
     */
    private fun invalidateBabyView(babyModel: BabyModel) {

        // 아이 추가 뷰 숨김
        first_add_baby_layout.isVisible = false

        // 이미지 숨김 버튼 보여줌
        main_image_off_button.isVisible = true

        // 상단 레이아웃 보여짐
        main_top_layout.isVisible = true

        // 곧 다가오는 일정 보여짐
        comming_schedule_layout.isVisible = true

        // 사진 설정
        real_baby_image.isVisible = true
        GlideApp.with(real_baby_image)
                .load(babyModel.photos.first().file)
                .centerCrop()
                .into(real_baby_image)
    }

    /**
     * Navigation Header 업데이트
     */
    private fun invalidateNavigationHeader(babyModel: BabyModel) {
        GlideApp.with(navigationHeaderImage)
                .load(babyModel.photos.first().file)
                .centerCrop()
                .circleCrop()
                .into(navigationHeaderImage)

        // 이름 설정
        navigationHeaderName.text = babyModel.baby.name
    }

    private fun babyInfoSetting() {
        var name = prefs.getString("baby_name", "")
        if (name == "" || name == null) {
            // 아무 정보 없을 때
            first_add_baby_layout.visibility = View.VISIBLE
            main_image_off_button.visibility = View.GONE

            add_baby.apply {
                setOnClickListener {
                    val intent = Intent(this@MainActivity, BabyRegisterActivity::class.java)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        } else {
            // 아기 정보 생겼을 때
            haveBabyInfoLayoutSet()
        }
    }

    fun haveBabyInfoLayoutSet() {
        first_add_baby_layout.visibility = View.GONE
        main_image_off_button.visibility = View.VISIBLE
        // 사진 등록 , 사진 Uri 있음
        var profile = prefs.getString("image", null)
        if (profile == null) {
            no_register_baby_image_layout.visibility = View.VISIBLE
        } else {
            real_baby_image.visibility = View.VISIBLE
            real_baby_image.setImageURI(Uri.parse(profile))
        }
        main_top_layout.visibility = View.VISIBLE
        comming_schedule_layout.visibility = View.VISIBLE

        var imageOn = false
        main_image_off_button.setOnClickListener {
            if (imageOn) {
                Log.d("test", "test Image On$imageOn")
                main_image_off_button.setBackgroundResource(R.drawable.main_image_off)
                no_register_baby_image_layout.visibility = View.GONE
                real_baby_image.visibility = View.GONE
                imageOn = false
            } else {
                Log.d("test", "test Image Off$imageOn")
                main_image_off_button.setBackgroundResource(R.drawable.main_image_on)
                no_register_baby_image_layout.visibility = View.VISIBLE
                real_baby_image.visibility = View.VISIBLE
                imageOn = true
            }
        }
    }

    /**
     * Navigation Item 선택시 불리는 리스너
     */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.more_baby -> {
                startActivity(Intent(this, BabyChangeActivity::class.java))
            }
        }

        return false
    }

/*
중요 사이트
https://promobile.tistory.com/193
https://dpdpwl.tistory.com/3
https://github.com/dolsanta/Sample_Calendar
https://dpdpwl.tistory.com/3
* */
    /*참고 사이트

    * https://webnautes.tistory.com/1216

https://medium.com/@Patel_Prashant_/android-custom-calendar-with-events-fa48dfca8257

https://dpdpwl.tistory.com/3

https://www.google.com/search?q=android+calendarview+예제&rlz=1C5CHFA_enKR851KR851&oq=android+calendarview+예제&aqs=chrome..69i57.10103j0j4&sourceid=chrome&ie=UTF-8

https://androi.tistory.com/136


https://zeph1e.tistory.com/42

https://drcarter.tistory.com/152

https://www.dev2qa.com/android-one-time-repeat-alarm-example/

https://gogorchg.tistory.com/entry/Android-AlarmManager%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-Schedule-%EA%B4%80%EB%A6%AC

https://jamesdreaming.tistory.com/102

https://namget.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%BB%A4%EC%8A%A4%ED%85%80-%EB%8B%AC%EB%A0%A5-%EC%98%88%EC%A0%9C-Android-Custom-CalendarView-Example

https://github.com/kuluna/CalendarViewPager

https://hatti.tistory.com/entry/android-calendar

https://github.com/hnhariat/calendar

https://woochan-dev.tistory.com/27

https://www.youtube.com/watch?v=xs5406vApTo
    * */
}
