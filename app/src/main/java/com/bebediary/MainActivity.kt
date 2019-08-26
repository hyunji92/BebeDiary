package com.bebediary

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.api.AirQualityApi
import com.bebediary.baby.change.BabyChangeActivity
import com.bebediary.calendar.CalendarFragment
import com.bebediary.calendar.alarm.CalendarAlarmReceiver
import com.bebediary.camera.CameraResultActivity
import com.bebediary.camera.CameraWrapperActivity
import com.bebediary.checklist.CheckListActivity
import com.bebediary.data.AirQuality
import com.bebediary.database.entity.Sex
import com.bebediary.database.model.BabyModel
import com.bebediary.database.model.DiaryModel
import com.bebediary.gallery.GalleryActivity
import com.bebediary.info.InfoFragment
import com.bebediary.main.adapter.IncomingDiaryAdapter
import com.bebediary.memo.NoteListActivity
import com.bebediary.register.BabyRegisterActivity
import com.bebediary.util.Constants
import com.bebediary.util.extension.eventDateToText
import com.bebediary.util.extension.format
import com.bebediary.whitenoise.WhiteNoiseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contents_main.*
import kotlinx.android.synthetic.main.header_navigatioin.view.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    LifecycleObserver,
    IncomingDiaryAdapter.OnItemChangeListener {

    // API
    private val airQualityApi by lazy { AirQualityApi(this) }
    private var airQualityItems = arrayListOf<AirQuality>()
    private val airQualityStorage by lazy {
        getSharedPreferences(
            "air_quality",
            Context.MODE_PRIVATE
        )
    }

    // 대기질 선택해놓은 지역 정보
    private var airQualitySido: String?
        get() = airQualityStorage.getString("sido", null)
        set(value) = airQualityStorage.edit().putString("sido", value).apply()
    private var airQualityCity: String?
        get() = airQualityStorage.getString("city", null)
        set(value) = airQualityStorage.edit().putString("city", value).apply()

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (application as MyApplication).db }

    // 현재 선택된 아이 정보
    private var currentBabyModel: BabyModel? = null

    // 다이어리 Disposable
    private var fetchDiaryDisposable: Disposable? = null

    // Incoming Diary Adapter
    private val incomingDiaryAdapter by lazy { IncomingDiaryAdapter(this) }

    // Fragments
    private val calendarFragment = CalendarFragment()
    private val infoFragment = InfoFragment()

    // Drawer Toggle
    private val drawerToggle by lazy {
        ActionBarDrawerToggle(
            this,
            dl_main_drawer_root,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
    }

    // 하단 네비게이션 리스너
    private val bottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->

            // 아이가 선택되어 있어야만 아래의 모든 작업을 할 수 있으므로 아이가 선택되어있지 않으면 리턴
            currentBabyModel?.baby?.id ?: return@OnNavigationItemSelectedListener false

            // 아이디에 해당하는 액션 실행
            when (item.itemId) {
                R.id.navigation_camera -> openBabyCamera()
                R.id.navigation_calendar -> replaceToCalendar()
                R.id.navigation_checklist -> openCheckList()
                R.id.navigation_information -> replaceToInformation()
                R.id.navigation_memo -> openNoteList()
            }

            true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lifecycle Observer
        lifecycle.addObserver(this)
    }

    /**
     * 뷰 기본 동작 설정
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // 기본 뷰 설정 ( 아이 추가 뷰 설정, 이미지 숨기기 버튼 숨김 )
        first_add_baby_layout.isVisible = true
        main_image_off_button.isVisible = false

        // 아이 추가 뷰 리스너
        add_baby.setOnClickListener {
            startActivity(Intent(this, BabyRegisterActivity::class.java))
        }

        // Navigation View 설정
        nav_view.isItemHorizontalTranslationEnabled = false
        nav_view.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)

        // Drawer Layout 설정
        dl_main_drawer_root.addDrawerListener(drawerToggle)

        // 메인 네비게이션 클릭 리스너
        nv_main_navigation_root.setNavigationItemSelectedListener(this)

        // 메인 이미지 숨기는 뷰
        main_image_off_button.setOnClickListener {
            val isOpen = it.tag as? Boolean == true
            main_image_off_button.setBackgroundResource(if (isOpen) R.drawable.main_image_on else R.drawable.main_image_off)
            no_register_baby_image_layout.isVisible = isOpen
            real_baby_image.isVisible = isOpen

            // 태그 업데이트
            it.tag = isOpen.not()
        }

        // 홈 버튼 눌렀을때 홈화면으로 이동
        go_to_home.setOnClickListener {
            main_all_Scrollview.isVisible = true
            frame_layout.isVisible = false
        }

        // 백색 소음 화면으로 이동
        white_nois.setOnClickListener {
            val intent = Intent(this, WhiteNoiseActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        // 대기질 지역 설정 다이얼로그 보여줌
        mainAirQualityGroupView.setOnClickListener { openSelectLocationDialog() }

        // RecyclerView Adapter
        mainIncomingDiaryRecyclerView.adapter = incomingDiaryAdapter
    }

    /**
     * 현재 선택된 아이의 정보를 가져와서
     * 드로어 헤더 업데이트
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchCurrentBaby() {
        db.babyDao().getSelected()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // Drawer에 아이 정보 업데이트
                    invalidateNavigationHeader(it)

                    // 아이 뷰 업데이트
                    invalidateBabyView(it)

                    // 다이어리 업데이트
                    fetchDiaries(it)

                    // 멤버 변수로 저장
                    currentBabyModel = it

                    // Logging
                    Log.d("Main", "현재 선택된 아이 : $it")
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 서버에서 대기질 정보를 가져오는 API 호출 후
     * List<AirQuality> 형태의 데이터로 받아온다
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchAirQuality() {
        airQualityApi.getAirQuality()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    airQualityItems.clear()
                    airQualityItems.addAll(it)

                    invalidateAirQualityView()
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * Calendar Notification Register 생성
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun registerCalendarNotification() {
        // Alarm Manager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 당일 날짜
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 모든 다이어리 정보 가져온다
        db.diaryDao().getAllFuture(start.timeInMillis)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // 현재 시간 캘린더 정보
                    val now = Calendar.getInstance()

                    // 알람 등록
                    it
                        .filter { diary -> diary.diary.date.time >= now.timeInMillis }
                        .filter { diary -> !diary.diary.isComplete && diary.diary.isEnableNotification }
                        .forEach { diary ->
                            // 전날 알림
                            val before = Calendar.getInstance().apply {
                                timeInMillis = diary.diary.date.time
                                set(Calendar.HOUR, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                add(Calendar.DAY_OF_MONTH, -1)
                            }

                            // 전날 알림 인텐트
                            val beforeIntent = Intent(this, CalendarAlarmReceiver::class.java)
                            beforeIntent.putExtra("content", diary.diary.content)
                            beforeIntent.putExtra("isBefore", true)

                            // 전날 알림 Pending Intent
                            val beforePendingIntent = PendingIntent.getBroadcast(
                                this,
                                Constants.notificationCalendarRequestCode,
                                beforeIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            // 당일 알림
                            val dDay = Calendar.getInstance().apply {
                                timeInMillis = diary.diary.date.time
                                set(Calendar.HOUR, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }

                            // 당일 알림 인텐트
                            val dDayIntent = Intent(this, CalendarAlarmReceiver::class.java)
                            dDayIntent.putExtra("content", diary.diary.content)
                            dDayIntent.putExtra("isBefore", false)

                            // 당일 알림 Pending Intent
                            val dDayPendingIntent = PendingIntent.getBroadcast(
                                this,
                                Constants.notificationCalendarRequestCode,
                                dDayIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            // 알림 등록
                            alarmManager.apply {
                                set(AlarmManager.RTC_WAKEUP, dDay.timeInMillis, dDayPendingIntent)
                                set(
                                    AlarmManager.RTC_WAKEUP,
                                    before.timeInMillis,
                                    beforePendingIntent
                                )
                            }
                        }
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.notificationChannelIdCalendar,
                Constants.notificationChannelNameCalendar,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = Constants.notificationChannelDescriptionCalendar
            notificationManager.createNotificationChannel(notificationChannel)
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

        // 아이 이름 설정
        BabyNameView.text = babyModel.baby.name

        // 성별 설정
        BabyGenderView.setImageResource(
            when (babyModel.baby.sex) {
                Sex.Female -> R.drawable.noti_icon
                Sex.Male -> R.drawable.noti_icon_off
            }
        )

        // 생일 혹은 출산 예정일
        val eventDate =
            (if (babyModel.baby.isPregnant) babyModel.baby.babyDueDate else babyModel.baby.birthday)
                ?: return

        // 생일 뷰 설정
        BabyBirthView.text = eventDate.format("YYYY.MM.dd")

        // 아이 설명 뷰 설정
        BabyDescriptionView.text = babyModel.baby.eventDateToText()
    }

    /**
     * Navigation Header 업데이트
     */
    private fun invalidateNavigationHeader(babyModel: BabyModel) {
        val headerView = nv_main_navigation_root.getHeaderView(0)
        val navigationHeaderImage = headerView.navigationHeaderImage
        val navigationHeaderName = headerView.navigationHeaderName

        // 이미지 설정
        GlideApp.with(navigationHeaderImage)
            .load(babyModel.photos.first().file)
            .centerCrop()
            .circleCrop()
            .into(navigationHeaderImage)

        // 이름 설정
        navigationHeaderName.text = babyModel.baby.name
    }

    /**
     * 대기질 레이아웃 설정
     */
    @SuppressLint("SetTextI18n")
    private fun invalidateAirQualityView() {

        // 대기질 정보가 없을때 뷰를 업데이트 하지 않음
        if (airQualityItems.count() == 0) {
            return
        }

        // 필터된 아이템
        val filterItems =
            airQualityItems.filter { it.cityName == airQualityCity && it.sidoName == airQualitySido }

        // 사용할 아이템 정보
        val item = if (filterItems.count() == 0) airQualityItems.first() else filterItems.first()
        mainDustLocation.text = "${item.sidoName} ${item.cityName}"
        mainDustValue.text = "PM ${item.pm10}"
    }

    /**
     * 다이어리 리스트 재 요청
     */
    private fun fetchDiaries(babyModel: BabyModel) {
        // Dispose Diary Disposable
        fetchDiaryDisposable?.dispose()

        // 오늘 부터 10일 이후까지
        val startedAt = Calendar.getInstance().apply {
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endedAt = Calendar.getInstance().apply {
            time = startedAt.time
            add(Calendar.DAY_OF_MONTH, 10)
        }

        // 최근 일정 다이어리 리스트 요청
        fetchDiaryDisposable = db.diaryDao()
            .getDiaryByDateRange(babyModel.baby.id, startedAt.timeInMillis, endedAt.timeInMillis)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateFetchDiaries(it) },
                { it.printStackTrace() }
            )
    }

    /**
     * 최근 일정 업데이트
     */
    private fun invalidateFetchDiaries(diaryModels: List<DiaryModel>) {
        incomingDiaryAdapter.items.clear()
        incomingDiaryAdapter.items.addAll(diaryModels)
        incomingDiaryAdapter.notifyDataSetChanged()

        // Visibility 업데이트
        mainIncomingDiaryEmptyView.isVisible = diaryModels.isEmpty()
        mainIncomingDiaryRecyclerView.isVisible = diaryModels.isNotEmpty()
    }

    /**
     * 다이어리 아이템 완료 상태 변경
     */
    override fun onChangeDiaryComplete(diaryModel: DiaryModel, isComplete: Boolean) {
        diaryModel.diary.isComplete = isComplete

        // 업데이트
        db.diaryDao().update(diaryModel.diary)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d("MC", "Update Diary Model ${diaryModel.diary}") },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 캘린더 화면으로 변경
     */
    private fun replaceToCalendar() {
        main_all_Scrollview.visibility = View.GONE
        frame_layout.visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, calendarFragment)
            .commitAllowingStateLoss()
    }

    /**
     * 정보 보여주는 화면으로 변경
     */
    private fun replaceToInformation() {
        main_all_Scrollview.visibility = View.GONE
        frame_layout.visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, infoFragment)
            .commitAllowingStateLoss()
    }

    /**
     * 메모 화면 열기
     */
    private fun openNoteList() {
        val babyId = currentBabyModel?.baby?.id ?: return
        val intent = Intent(this@MainActivity, NoteListActivity::class.java)
        intent.putExtra("babyId", babyId)
        startActivity(intent)
    }

    /**
     * 사진 찍는 화면 열기
     */
    private fun openBabyCamera() {
        val intent = Intent(this, CameraWrapperActivity::class.java)
        startActivityForResult(intent, Constants.requestCameraCode)
    }

    /**
     * 체크 리스트 화면 열기
     */
    private fun openCheckList() {
        val babyId = currentBabyModel?.baby?.id ?: return
        val intent = Intent(this@MainActivity, CheckListActivity::class.java)
        intent.putExtra("babyId", babyId)
        startActivity(intent)
    }

    /**
     * 아이 사진 찍은 결과 화면 보여주는 액티비티 실행
     */
    private fun openBabyCameraResult(imagePath: String) {
        val babyId = currentBabyModel?.baby?.id ?: return

        val intent = Intent(this, CameraResultActivity::class.java)
        intent.putExtra("babyId", babyId)
        intent.putExtra("imagePath", imagePath)
        startActivity(intent)
    }

    /**
     * 아이 사진 모아보는 갤러리 실행
     */
    private fun openGallery() {
        val babyId = currentBabyModel?.baby?.id ?: return
        val intent = Intent(this@MainActivity, GalleryActivity::class.java)
        intent.putExtra("babyId", babyId)
        startActivity(intent)
    }

    /**
     * 지역 선택 다이얼로그 보여줌
     */
    private fun openSelectLocationDialog() {
        if (airQualityItems.count() == 0) return

        // 아이템 리스트
        val items = airQualityItems.map { "${it.sidoName} - ${it.cityName}" }
            .sorted()
            .toTypedArray()

        // Dialog 보여줌
        AlertDialog.Builder(this)
            .setTitle("지역을 선택해주세요")
            .setItems(items) { _, position ->
                val sidoCityName = items[position].split(" - ")
                this.airQualitySido = sidoCityName[0]
                this.airQualityCity = sidoCityName[1]
                invalidateAirQualityView()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 사진 촬영 화면 누른 후 다시 돌아왔을때 이미지 경로가 있으면 사진 결과 화면 실행
        if (requestCode == Constants.requestCameraCode && resultCode == Activity.RESULT_OK) {
            val imagePath = data?.getStringExtra("imagePath")
            if (imagePath != null) {
                openBabyCameraResult(imagePath)
            }
        }
    }

    /**
     * Navigation Item 선택시 불리는 리스너
     */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        dl_main_drawer_root.closeDrawer(GravityCompat.START)

        when (menuItem.itemId) {
            R.id.more_baby -> {
                startActivity(Intent(this, BabyChangeActivity::class.java))
            }
            R.id.navigation_gallery -> openGallery()
            R.id.guide -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("http://www.bebediary.co.kr/app/guide/"))
                startActivity(intent)
            }
            R.id.pregnant_guide -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("http://www.bebediary.co.kr/app/baby_info/"))
                startActivity(intent)
            }
            R.id.review -> {

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.example.android"
                    )
                    setPackage("com.android.vending")
                }
                startActivity(intent)
//                val i = Intent(Intent.ACTION_VIEW)
//                val appPackageName = packageName // getPackageName() from Context or Activity object
//                try {
//                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
//                } catch (anfe: android.content.ActivityNotFoundException) {
//                    startActivity(
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
//                        )
//                    )
//                }
//
//                startActivity(i)
            }
            R.id.together -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("http://www.bebediary.co.kr/app/partner/"))
                startActivity(intent)
            }
        }

        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (dl_main_drawer_root.isDrawerOpen(GravityCompat.START)) {
            dl_main_drawer_root.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()


    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
