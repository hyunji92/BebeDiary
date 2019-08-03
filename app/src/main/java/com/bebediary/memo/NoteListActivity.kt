package com.bebediary.memo

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import com.bebediary.R
import com.bebediary.memo.adapter.NotesAdapter
import com.bebediary.memo.memodb.NoteDatabase
import com.bebediary.memo.memodb.model.Note
import kotlinx.android.synthetic.main.activity_memo.*
import kotlinx.android.synthetic.main.note_list_item.*
import java.lang.ref.WeakReference
import java.util.*

class NoteListActivity : AppCompatActivity(), NotesAdapter.OnNoteItemClick {


    private var noteDatabase: NoteDatabase? = null
    lateinit var notes: MutableList<Note>
    private var notesAdapter: NotesAdapter? = null
    private var pos: Int = 0

    private val listener =
        View.OnClickListener { startActivityForResult(Intent(this@NoteListActivity, AddNoteActivity::class.java), 100) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)
        initializeVies()
        displayList()

        back_button.setOnClickListener {
            this.finish()
        }

    }


    private fun displayList() {
        noteDatabase = NoteDatabase.getInstance(this@NoteListActivity)
        RetrieveTask(this).execute()
    }

    private class RetrieveTask// only retain a weak reference to the activity
    internal constructor(context: NoteListActivity) : AsyncTask<Void, Void, List<Note>>() {

        private val activityReference: WeakReference<NoteListActivity> = WeakReference(context)

        override fun doInBackground(vararg voids: Void): List<Note>? {
            return if (activityReference.get() != null)
                activityReference.get()?.noteDatabase?.noteDao?.notes
            else
                null
        }

        override fun onPostExecute(notes: List<Note>?) {
            if (notes != null && notes.isNotEmpty()) {
                activityReference.get()?.notes?.clear()
                activityReference.get()?.notes?.addAll(notes)

                // hides empty text view
                activityReference.get()?.empty_memo_text?.visibility = View.GONE
                activityReference.get()?.notesAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun initializeVies() {
        val addMemoBtn = findViewById<View>(R.id.add_memo_button) as ImageButton
        addMemoBtn.setOnClickListener(listener)

        recycler_view.layoutManager = LinearLayoutManager(this@NoteListActivity)
        notes = ArrayList()
        notesAdapter = NotesAdapter(notes, this@NoteListActivity)
        recycler_view.adapter = notesAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode > 0) {
            if (resultCode == 1) {
                notes.add(data!!.getSerializableExtra("note") as Note)
            } else if (resultCode == 2) {
                notes[pos] = data!!.getSerializableExtra("note") as Note
            }
            listVisibility()
        }
    }

    override fun onNoteClick(pos: Int) {
        /*AlertDialog.Builder(this@NoteListActivity)
            .setTitle("Select Options")
            .setItems(arrayOf("Delete", "Update")) { dialogInterface, i ->
                when (i) {
                    0 -> {
                        noteDatabase!!.noteDao.deleteNote(notes[pos])
                        notes.removeAt(pos)
                        listVisibility()
                    }
                    1 -> {
                        this@NoteListActivity.pos = pos
                        startActivityForResult(
                            Intent(
                                this@NoteListActivity,
                                AddNoteActivity::class.java
                            ).putExtra("note", notes[pos]),
                            100
                        )
                    }
                }
            }.show()
*/
        this@NoteListActivity.pos = pos
        startActivityForResult(
            Intent(
                this@NoteListActivity,
                AddNoteActivity::class.java
            ).putExtra("note", notes[pos]),
            100
        )
    }

    private fun listVisibility() {
        var emptyMsgVisibility = View.GONE
        if (notes.size == 0) { // no item to display
            if (empty_memo_text.visibility == View.GONE)
                emptyMsgVisibility = View.VISIBLE
        }
        empty_memo_text.visibility = emptyMsgVisibility
        notesAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        noteDatabase!!.cleanUp()
        super.onDestroy()
    }
}
