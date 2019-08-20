package com.bebediary.checklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bebediary.R
import com.bebediary.checklist.adapter.CheckListAdapter
import kotlinx.android.synthetic.main.activity_checklist.*

class CheckListActivity : AppCompatActivity() {

    private val checkListAdapter: CheckListAdapter by lazy { CheckListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        // 뒤로 가기 버튼
        back_button.setOnClickListener { finish() }

        check_recycler_view.adapter = checkListAdapter
    }

}
