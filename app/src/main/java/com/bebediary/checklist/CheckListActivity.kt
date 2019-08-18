package com.bebediary.checklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bebediary.R
import com.hyundeee.app.wingsui.adapter.CheckListAdapter
import kotlinx.android.synthetic.main.activity_checklist.*

class CheckListActivity : AppCompatActivity() {

    lateinit var checkListAdapter: CheckListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        back_button.setOnClickListener {
            this.finish()
        }
        check_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            this@CheckListActivity,
            androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
            false
        )
        checkListAdapter = CheckListAdapter()
        check_recycler_view.adapter = checkListAdapter
    }


}
