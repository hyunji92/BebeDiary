package com.bebediary.checklist

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.entity.CheckList
import com.bebediary.database.entity.CheckListCategory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_checklist_add.*

class CheckListAddActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, LifecycleObserver {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // 사용자 지정 카테고리
    private var isCustomCategory = false

    // Database
    private val db by lazy { (application as MyApplication).db }

    // Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // 체크리스트 카테고리 스피너어뎁터
    private val checkListCategoryAdapter by lazy {
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
    }

    // 체크 리스트 카테고리 정보들
    private var checkListCategories: List<CheckListCategory>? = null

    // 선택된 체크 리스트 카테고리
    private var selectedCheckListCategory: CheckListCategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist_add)

        // 아이 정보 검증
        if (babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    /**
     * 뷰들 초기화 작업
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // 스피너 설정
        checkListAddCategorySpinner.onItemSelectedListener = this
        checkListAddCategorySpinner.adapter = checkListCategoryAdapter

        // 저장 버튼 설정
        checklistAddSave.setOnClickListener { save() }

        // 취소 버튼 설정
        checklistAddClose.setOnClickListener { finish() }
    }

    /**
     * 체크리스트 카테고리 정보 구독
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun subscribeCheckListCategories() {
        db.checkListCategoryDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateCheckListCategories(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }

    }

    /**
     * 레이아웃 사이즈 업데이트
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun updateLayout() {
        val point = Point()
        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        display.getSize(point)

        window.attributes.width = (point.x * 0.9).toInt() //Display 사이즈의 90%
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = compositeDisposable.dispose()

    /**
     * 저장 처리
     */
    private fun save() {
        // 카테고리 입력
        if (isCustomCategory && checkListAddCustomCategoryName.text?.isBlank() == true) {
            Toast.makeText(this, "카테고리 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 사용자 지정 카테고리가 아닐때 선택된 카테고리가 없는 경우
        if (!isCustomCategory && selectedCheckListCategory == null) {
            Toast.makeText(this, "카테고리를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 내용 입력
        if (checkListAddContent.text?.isBlank() == true) {
            Toast.makeText(this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (isCustomCategory) saveCheckListWithCategory() else saveCheckList()
    }

    /**
     * 카테고리와 체크리스트 둘다 저장
     */
    private fun saveCheckListWithCategory() {
        val checkListCategory = CheckListCategory(name = checkListAddCustomCategoryName.text.toString())

        // 체크리스트 저장
        db.checkListCategoryDao()
            .insert(checkListCategory)
            .flatMap {
                db.checkListDao().insert(
                    CheckList(
                        categoryId = it,
                        babyId = babyId,
                        content = checkListAddContent.text.toString()
                    )
                )
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
     * 단순 체크리스트 저장
     */
    private fun saveCheckList() {
        val categoryId = selectedCheckListCategory?.id ?: return

        // 체크리스트 저장
        db.checkListDao().insert(
            CheckList(
                categoryId = categoryId,
                babyId = babyId,
                content = checkListAddContent.text.toString()
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { finish() },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 체크 리스트 카테고리 스피너 업데이트
     */
    private fun invalidateCheckListCategories(checkListCategories: List<CheckListCategory>) {
        this.checkListCategories = checkListCategories

        // 현재 선택된 카테고리 초기 데이터 설정
        this.selectedCheckListCategory = checkListCategories[0]

        checkListCategoryAdapter.addAll(*checkListCategories.map { it.name }.toTypedArray(), "사용자 지정")
        checkListCategoryAdapter.notifyDataSetChanged()
    }

    /**
     * 체크리스트 카테고리 선택 리스너
     *
     * 아무것도 선택하지 않았을 경우
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    /**
     * 체크리스트 카테고리 선택 리스너
     *
     * 아이템을 선택했을 경우
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        isCustomCategory = position + 1 == checkListCategoryAdapter.count
        checkListAddCustomCategoryNameGroup.isVisible = isCustomCategory

        // Custom Category 의 경우 Null 아닌경우 현재 선택한 카테고리
        selectedCheckListCategory = if (isCustomCategory) null else checkListCategories?.get(position)
    }
}