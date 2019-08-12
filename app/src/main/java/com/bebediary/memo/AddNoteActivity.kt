package com.bebediary.memo

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.bebediary.R
import com.bebediary.memo.memodb.NoteDatabase
import com.bebediary.memo.memodb.model.Note
import kotlinx.android.synthetic.main.activity_add_memo.*
import java.lang.ref.WeakReference

class AddNoteActivity : AppCompatActivity() {

    private var noteDatabase: NoteDatabase? = null
    lateinit var note: Note
    private var update: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memo)

        noteDatabase = NoteDatabase.getInstance(this@AddNoteActivity)
        val button = findViewById<Button>(R.id.save_button)
        if (intent.getSerializableExtra("note") != null) {
            note = intent.getSerializableExtra("note") as Note
            update = true
            save_button.text = "수정"
            et_title.setText(note.title)
            et_content.setText(note.content)
        }

        button.setOnClickListener {
            if (update) {
                note.content = et_content.text.toString()
                note.title = et_title.text.toString()
                noteDatabase?.noteDao?.updateNote(note)
                setResult(note, 2)
            } else {
                note = Note(et_content.text.toString(), et_title.text.toString())
                InsertTask(this@AddNoteActivity, note).execute()
            }
        }

        back_button.setOnClickListener {
            this.finish()
        }
    }

    private fun setResult(note: Note?, flag: Int) {
        setResult(flag, Intent().putExtra("note", note))
        finish()
    }

    private class InsertTask// only retain a weak reference to the activity
    internal constructor(context: AddNoteActivity, private val note: Note) : AsyncTask<Void, Void, Boolean>() {

        private val activityReference: WeakReference<AddNoteActivity> = WeakReference(context)

        // doInBackground methods runs on a worker thread
        override fun doInBackground(vararg objs: Void): Boolean? {
            // retrieve auto incremented note id
            val j = activityReference.get()?.noteDatabase?.noteDao?.insertNote(note)
            if (j != null) {
                note.note_id = j
            }
            Log.e("ID ", "doInBackground: $j")
            return true
        }

        // onPostExecute runs on main thread
        override fun onPostExecute(bool: Boolean?) {
            if (bool!!) {
                activityReference.get()?.setResult(note, 1)
                activityReference.get()?.finish()
            }
        }
    }


}
