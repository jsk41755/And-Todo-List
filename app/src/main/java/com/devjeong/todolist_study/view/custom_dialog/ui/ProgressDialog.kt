package com.devjeong.todolist_study.view.custom_dialog.ui

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.devjeong.todolist_study.R

class ProgressDialog(context: Context) : Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.progress_dialog)
    }
}
