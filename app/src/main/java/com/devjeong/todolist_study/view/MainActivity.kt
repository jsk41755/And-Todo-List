package com.devjeong.todolist_study.view

import android.os.Bundle
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.databinding.ActivityMainBinding
import com.devjeong.todolist_study.view.ui.HomeFragment

class MainActivity : BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HomeFragment를 호스팅하는 작업 등을 수행
        val fragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}