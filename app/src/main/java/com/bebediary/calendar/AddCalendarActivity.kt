package com.bebediary.calendar

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.calendar.adapter.AddCalendarAttachmentAdapter
import com.bebediary.camera.CameraWrapperActivity
import com.bebediary.database.entity.Attachment
import com.bebediary.database.entity.Diary
import com.bebediary.database.entity.DiaryAttachment
import com.bebediary.database.model.DiaryModel
import com.bebediary.util.Constants
import com.bebediary.util.extension.toAttachment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_calendar.*
import java.io.File
import java.util.*

/**
 * 일정 추가하는 액티비티
 */
class AddCalendarActivity : AppCompatActivity(), LifecycleObserver, AddCalendarAttachmentAdapter.OnItemChangeListener {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // 일정을 추가할 날짜 데이터
    // 날짜 데이터는 연 월 일이 무조건 동시에 바뀐다는 가정하에
    // dayOfMonth가 변경될때만 캘린더 뷰를 업데이트 하고 다이어리 정보를 요청한다
    private var year = -1
    private var month = -1
    private var day = -1
        set(value) {
            field = value
            invalidateAddCalendarDateView()
            fetchDiary()
        }

    // 일정 추가할 날짜 Calendar
    private val date
        get() = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

    // 알림 상태 여부 데이터
    private var isNotification = false
        set(value) {
            field = value
            invalidateNotificationView()
        }

    // RecyclerView Adapter
    private val addCalendarAttachmentAdapter by lazy { AddCalendarAttachmentAdapter(this) }

    // Composite Disposable
    private val compositeDisposable = CompositeDisposable()

    // Database
    private val db by lazy { (application as MyApplication).db }

    // Fetch Diary Disposable
    private var fetchDiaryDisposable: Disposable? = null

    // 이미 데이터가 들어있는 경우 저장
    private var preloadDiary: DiaryModel? = null
        set(value) {
            field = value
            invalidatePreloadDiary(value)
        }

    // 수정 모드
    private val isEdit: Boolean
        get() = preloadDiary != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_calendar)

        // 아이 정보가 넘어오지 않았을때 화면 종료
        if (this.babyId == -1L) {
            finish()
            return
        }

        // 옵저버 추가
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // 일정을 입력할 날짜 저장
        year = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR))
        month = intent.getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH))
        day = intent.getIntExtra("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH))

        // 첨부파일 추가
        addCalendarAddAttachment.setOnClickListener { showPhotoSelectDialog() }

        // 날짜 선택
        date_picker_button.setOnClickListener { showDatePicker() }

        // 알림 상태 변경
        addCalendarNotificationView.setOnClickListener { isNotification = isNotification.not() }

        // 첨부파일 리사이클러 뷰 업데이트
        addCalendarAttachmentRecyclerView.adapter = addCalendarAttachmentAdapter

        // 저장 혹은 수정 작업
        addCalendarSave.setOnClickListener { editOrSave() }

        // 삭제
        addCalendarDelete.setOnClickListener { delete() }

        // 종료
        addCalendarBackButton.setOnClickListener { finish() }
    }

    private fun fetchDiary() {

        // 기존 실행중인 작업이 있다면 종료
        fetchDiaryDisposable?.dispose()

        // 다음날 정보
        val nextDate = Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
        }

        // 데이터 기본으로 없애버림
        preloadDiary = null

        // 해당 날짜 및 다음 날짜 다이어리 정보
        fetchDiaryDisposable = db.diaryDao().getDiaryByDate(babyId, date.timeInMillis, nextDate.timeInMillis)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            // 다이어리 정보 업데이트
                            preloadDiary = it
                        },
                        { it.printStackTrace() }
                )
    }

    /**
     * 날짜 변경 다이얼로그 띄움
     */
    private fun showDatePicker() {

        // API 24 이상일 경우 시스템 기본 테마 사용
        val context =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) this
                else ContextThemeWrapper(this, R.style.MyDatePickerSpinnerStyle)

        // Show Date Picker Dialog
        DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    this.year = year
                    this.month = month
                    this.day = dayOfMonth
                },
                year, month, day
        ).show()
    }

    /**
     * 날짜 데이터가 업데이트 되었을때 텍스트뷰 텍스트 변경
     */
    private fun invalidateAddCalendarDateView() {
        addCalendarDateView.text = String.format("%02d.%02d.%02d", year, month + 1, day)
    }

    /**
     * 알림 뷰 업데이트
     */
    private fun invalidateNotificationView() {
        addCalendarNotificationView.setImageResource(if (isNotification) R.drawable.noti_icon else R.drawable.noti_icon_off)
    }

    /**
     * 이미 작성된 다이어리 모델 업데이트
     */
    private fun invalidatePreloadDiary(diaryModel: DiaryModel?) {

        // 저장, 수정 버튼 텍스트 변경
        addCalendarSave.text = if (isEdit) "수정" else "저장"

        // 수정일때는 날짜 변경 불가하게 설정
        date_picker_button.isEnabled = !isEdit

        // 삭제 버튼 숨김
        addCalendarDeleteGroup.isVisible = isEdit

        // 내용 업데이트
        addCalendarContent.setText(diaryModel?.diary?.content ?: "")

        // 알림 상태 업데이트
        isNotification = diaryModel?.diary?.isEnableNotification ?: false

        // 첨부파일 업데이트
        addCalendarAttachmentAdapter.items.clear()
        if (diaryModel?.diaryAttachments?.isNotEmpty() == true) {
            val attachmentModels = diaryModel.diaryAttachments
            addCalendarAttachmentAdapter.items.addAll(attachmentModels.map { it.attachments.first() })
        }
        addCalendarAttachmentAdapter.notifyDataSetChanged()
    }

    /**
     * 사진 선택 다이얼로그
     */
    private fun showPhotoSelectDialog() {
        AlertDialog.Builder(this)
                .setTitle("사진등록")
                .setItems(arrayOf("카메라", "갤러리")) { _, id ->
                    if (id == 0) takePhoto() else goToAlbum()
                }
                .show()
    }

    /**
     * 사진 촬영 액티비티 실행
     */
    private fun takePhoto() {
        val intent = Intent(this, CameraWrapperActivity::class.java)
        startActivityForResult(intent, Constants.requestCameraCode)
    }

    /**
     * 사진 선택 액티비티 실행
     */
    private fun goToAlbum() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, Constants.requestAlbumCode)
    }

    /**
     * 어뎁터에 사진 추가
     */
    private fun addAttachment(attachment: Attachment) {
        addCalendarAttachmentAdapter.items.add(attachment)
        addCalendarAttachmentAdapter.notifyDataSetChanged()
    }

    /**
     * 어뎁터에 사진 제거
     */
    private fun removeAttachment(attachment: Attachment) {
        val index = addCalendarAttachmentAdapter.items.indexOf(attachment)
        if (index >= 0) {
            addCalendarAttachmentAdapter.items.removeAt(index)
        }
        addCalendarAttachmentAdapter.notifyDataSetChanged()
    }

    /**
     * 첨부파일 Adapter에서 삭제 버튼 선택시
     */
    override fun onRemoveAttachment(attachment: Attachment) = removeAttachment(attachment)

    /**
     * 다이어리 내용 저장 혹은 삭제 작업
     */
    private fun editOrSave() {
        if (addCalendarContent.text.isNullOrBlank()) {
            Toast.makeText(this, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEdit) edit() else save()
    }

    /**
     * 기존 다이어리 첨부파일 전부 제거
     */
    private fun edit() {
        val preloadDiary = preloadDiary ?: return

        // 업데이트할 다이어리 정보
        val diary = preloadDiary.diary
        diary.content = addCalendarContent.text.toString()
        diary.isEnableNotification = isNotification

        // 기존 첨부파일 제거 및 데이터 입력
        db.diaryAttachmentDao()
                .deleteAll(preloadDiary.diaryAttachments.map { it.diaryAttachment })
                .flatMap { db.diaryDao().update(diary) }
                .flatMap { _ ->
                    val diaryAttachments = addCalendarAttachmentAdapter.items.map {
                        DiaryAttachment(diaryId = diary.id, attachmentId = it.id)
                    }
                    db.diaryAttachmentDao().insertAll(diaryAttachments)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { finish() },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    /**
     * 저장 요청
     */
    private fun save() {

        // 저장할 다이어리 객체
        val diary = Diary(
                babyId = babyId,
                content = addCalendarContent.text.toString(),
                date = date.time,
                isEnableNotification = isNotification
        )

        // 데이터 베이스에 입력
        db.diaryDao().insert(diary)
                .flatMap { diaryId ->
                    val diaryAttachments = addCalendarAttachmentAdapter.items.map {
                        DiaryAttachment(diaryId = diaryId, attachmentId = it.id)
                    }
                    db.diaryAttachmentDao().insertAll(diaryAttachments)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            finish()
                        },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    /**
     * 다이어리 삭제
     */
    private fun delete() {
        val preloadDiary = preloadDiary ?: return

        // 데이터 베이스에 입력
        db.diaryDao().deleteAll(listOf(preloadDiary.diary))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { finish() },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 실패한 작업은 처리 하지 않는다
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        // 사진 첨부파일 관련 처리
        if (requestCode == Constants.requestCameraCode || requestCode == Constants.requestAlbumCode) {

            // 넘어온 인텐트 데이터가 없으면 작업 하지 않음
            data ?: return

            // 이미지 선택 URI
            val attachment = when (requestCode) {
                Constants.requestAlbumCode ->
                    data.data?.toAttachment(this) // 앨범에서 이미지를 선택했을경우
                Constants.requestCameraCode -> {
                    File(data.getStringExtra("imagePath")).toAttachment(this) // 카메라 촬영으로 이미지를 가져왔을 경우

                }
                else -> null
            } ?: return

            // 이미지 데이터 베이스에 추가 및 뷰에 추가
            val attachmentDao = db.attachmentDao()
            attachment
                    .flatMap { attachmentDao.insert(it) }
                    .flatMap { id -> db.attachmentDao().getById(id) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { addAttachment(it) },
                            { it.printStackTrace() }
                    )
                    .apply { compositeDisposable.add(this) }
        }
    }
}
