package com.bebediary.checklist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.checklist.adapter.CheckListSection
import com.bebediary.database.entity.CheckList
import com.bebediary.database.entity.CheckListCategory
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_checklist.*

class CheckListActivity : AppCompatActivity(), LifecycleObserver, CheckListSection.OnItemChangeListener {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // 체크리스트 어뎁터
    private val checkListAdapter: SectionedRecyclerViewAdapter by lazy { SectionedRecyclerViewAdapter() }

    // 체크리스트 카테고리 스피너어뎁터
    private val checkListCategoryAdapter by lazy {
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
    }

    // Composite Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // Database
    private val db by lazy { (application as MyApplication).db }

    // 아이템들
    private var categories: List<CheckListCategory> = listOf()
    private var items: List<CheckList> = listOf()

    // 선택된 카테고리 필터
    private var categoryFilter: CheckListCategory? = null
        set(value) {
            field = value

            // 체크 리스트 업데이트
            invalidateCheckLists(items)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        // 아이 정보 검증
        if (babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // RecyclerView 어뎁터
        check_recycler_view.adapter = checkListAdapter

        // 체크리스트 추가버튼
        checkListAdd.setOnClickListener { addCheckList() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeCheckListCategory() {

        // 어뎁터 생성
        checkListCategorySpinner.adapter = checkListCategoryAdapter
        checkListCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 카테고리 필터 설정
                categoryFilter = if (position == 0) null else categories[position - 1]
            }
        }

        // 카테고리 요청후 설정
        db.checkListCategoryDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // 체크리스트 스피너 업데이트
                    checkListCategoryAdapter.clear()
                    checkListCategoryAdapter.add("전체")
                    checkListCategoryAdapter.addAll(it.map { category -> category.name })

                    // 카테고리 업데이트
                    this.categories = it

                    // 체크 리스트 구독 요청
                    subscribeCheckLists()
                },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 체크리스트 아이템 구독
     */
    private fun subscribeCheckLists() {
        db.checkListDao()
            .getCheckLists(babyId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { invalidateCheckLists(it) },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }

    /**
     * 체크리스트 추가 화면 실행
     */
    private fun addCheckList() {
        val intent = Intent(this, CheckListAddActivity::class.java).putExtra("babyId", babyId)
        startActivity(intent)
    }

    /**
     * 체크 리스트 업데이트
     */
    private fun invalidateCheckLists(items: List<CheckList>) {

        // 아이템 업데이트
        this.items = items

        // 모든 섹션 제거
        checkListAdapter.removeAllSections()

        // 데이터 설정
        categories.filter { categoryFilter == null || it.id == categoryFilter?.id }.forEach {
            val checkLists = items.filter { checkListCategory -> checkListCategory.categoryId == it.id }
            if (checkLists.count() > 0) {
                checkListAdapter.addSection(CheckListSection(it.name, checkLists, this))
            }
        }

        checkListAdapter.notifyDataSetChanged()
    }

    /**
     * 체크리스트 체크박스 상태 변경 리스너
     */
    override fun onChangeCheckListComplete(checkList: CheckList, isComplete: Boolean) {
        checkList.isComplete = isComplete
        db.checkListDao().update(checkList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d("CheckListactivity", "Change Complete") },
                { it.printStackTrace() }
            )
            .apply { compositeDisposable.add(this) }
    }
}
