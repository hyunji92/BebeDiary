package com.bebediary.baby.change

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.baby.change.adapter.BabyChangeAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_baby_change.*

class BabyChangeActivity : AppCompatActivity(), LifecycleObserver {

    // 아기 변경 어뎁터
    private val babyChangeAdapter by lazy { BabyChangeAdapter() }

    // Database
    private val db by lazy { (application as MyApplication).db }

    // Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baby_change)

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeRecyclerView() {
        babyChangeRecyclerView.adapter = babyChangeAdapter
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchBabies() {
        db.babyDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            babyChangeAdapter.items.clear()
                            babyChangeAdapter.items.addAll(it)
                            babyChangeAdapter.notifyDataSetChanged()
                        },
                        {
                            it.printStackTrace()
                        }
                )
                .apply { compositeDisposable.add(this) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() {
        compositeDisposable.dispose()
    }
}