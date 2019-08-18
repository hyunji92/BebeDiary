package com.bebediary.memo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.entity.Note
import com.bebediary.memo.adapter.NotesAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_memo.*

class NoteListActivity : AppCompatActivity(), NotesAdapter.OnNoteItemClick, LifecycleObserver {

    // Adapter
    private val notesAdapter: NotesAdapter by lazy { NotesAdapter(this) }

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // Composite Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // Database
    private val db by lazy { (application as MyApplication).db }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)

        // 아이 정보가 넘어오지 않았을때 화면 종료
        if (this.babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    /**
     * 뷰 초기화
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // RecyclerView 설정
        recycler_view.adapter = notesAdapter

        // 툴바 뒤로가기 버튼 클릭
        back_button.setOnClickListener { finish() }

        // 글 작성 버튼 클릭
        add_memo_button.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
                    .putExtra("babyId", babyId)
            startActivity(intent)
        }
    }

    /**
     * 노트 데이터 요청
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun fetchNotes() {
        db.noteDao().getNotes(babyId = babyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            // 메모 비어있을때 보여주는 뷰 가시성 설정
                            empty_memo_text.isVisible = it.isEmpty()

                            notesAdapter.items.clear()
                            notesAdapter.items.addAll(it)
                            notesAdapter.notifyDataSetChanged()
                        },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = compositeDisposable.dispose()

    /**
     * 노트 선택해서 수정 모드 진입
     */
    override fun onNoteClick(note: Note) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("noteId", note.id)
        intent.putExtra("babyId", babyId)
        startActivity(intent)
    }
}
