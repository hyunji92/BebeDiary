package com.bebediary.memo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bebediary.MyApplication
import com.bebediary.R
import com.bebediary.database.entity.Note
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_memo.*

class AddNoteActivity : AppCompatActivity(), LifecycleObserver {

    // 아이 정보
    private val babyId: Long
        get() = intent.getLongExtra("babyId", -1L)

    // 노트 정보
    private val noteId: Long
        get() = intent.getLongExtra("noteId", -1L)

    // 노트 수정모드인지 아닌지 여부
    private val isEdit: Boolean
        get() = noteId != -1L

    // 생성될 혹은 이미 추가되어있는 노트 정보
    private var note: Note? = null

    // Composite Disposable
    private val compositeDisposable by lazy { CompositeDisposable() }

    // Database
    private val db by lazy { (application as MyApplication).db }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memo)

        // 아이 정보가 넘어오지 않았을때 화면 종료
        if (this.babyId == -1L) {
            finish()
            return
        }

        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initializeView() {

        // 뒤로가기 버튼
        back_button.setOnClickListener { finish() }

        // 저장 버튼 텍스트 변경
        save_button.text = if (isEdit) "수정" else "등록"

        // 저장 버튼 클릭 리스너
        save_button.setOnClickListener { editOrSave() }

        // 수정 모드일때 FetchNote
        if (isEdit) prefetchNote()
    }

    /**
     * 수정모드일때 노트 아이디를 이용해서
     * 기존에 작성되어있는 노트 정보를 가져와 뷰를 설정해준다
     */
    private fun prefetchNote() {
        db.noteDao().getNoteById(noteId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            this.note = it

                            et_title.setText(it.title)
                            et_content.setText(it.content)
                        },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    private fun editOrSave() {
        if (et_content.text.isNullOrBlank() || et_content.text.isNullOrBlank()) {
            Toast.makeText(this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEdit) edit() else save()
    }

    private fun edit() {
        val sourceNote = note ?: return
        sourceNote.title = et_content.text.toString()
        sourceNote.content = et_content.text.toString()
        db.noteDao().update(sourceNote)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { finish() },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }

    private fun save() {
        val note = Note(title = et_content.text.toString(), content = et_content.text.toString(), babyId = babyId)
        db.noteDao().insert(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { finish() },
                        { it.printStackTrace() }
                )
                .apply { compositeDisposable.add(this) }
    }
}
