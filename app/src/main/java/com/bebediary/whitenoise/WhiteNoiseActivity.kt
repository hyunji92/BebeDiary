package com.bebediary.whitenoise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bebediary.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class WhiteNoiseActivity : AppCompatActivity() {
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.timer -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.play -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.volume -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whitenoise)

        val navView: BottomNavigationView = findViewById(R.id.sound_nav_view)
        navView.apply {
            isItemHorizontalTranslationEnabled = false
            setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        }
    }

}
