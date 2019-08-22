package com.bebediary.database.callback

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bebediary.MyApplication
import com.bebediary.database.entity.CheckListCategory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CheckListInitializer(private val application: MyApplication) : RoomDatabase.Callback() {

    private val compositeDisposable by lazy { CompositeDisposable() }

    // 기본 체크 리스트 카테고리
    private val defaultCheckListCategories = listOf(
        CheckListCategory(name = "임신전"),
        CheckListCategory(name = "임신초기"),
        CheckListCategory(name = "임신중기"),
        CheckListCategory(name = "임신후기"),
        CheckListCategory(name = "출산후")
    )

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // 기본 체크리스트 카테고리 생성
        application.db.checkListCategoryDao()
            .insertAll(defaultCheckListCategories)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d("CheckListInitializer", "Initialize CheckListCategory") },
                { it.printStackTrace() }
            ).apply { compositeDisposable.add(this) }
    }

}
