package com.devjeong.todolist_study.View.CustomDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.Model.TodoItemDTO
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.View.MainActivity
import com.devjeong.todolist_study.ViewModel.TodoListViewModel

class CustomDialog(
    private val activity: MainActivity,
    private val dialogInterface: CustomDialogInterface
) : Dialog(activity) {
    private lateinit var addButton: Button
    private lateinit var todoTitle : TextView
    private lateinit var todoSwitch : Switch

    private lateinit var todoViewModel: TodoListViewModel

    init {
        Log.d("CustomDialog", "Context: $context")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

        val window = window
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        window?.attributes = layoutParams

        todoViewModel = ViewModelProvider(activity)[TodoListViewModel::class.java]

        addButton = findViewById(R.id.addBtn)
        todoTitle = findViewById(R.id.todoTitle)
        todoSwitch = findViewById(R.id.isDoneSwitch)

        addButton.setOnClickListener {
            dialogInterface.onAddButtonClicked()
            val todoItem = TodoItemDTO(todoTitle.text.toString(), todoSwitch.isChecked)

            todoViewModel.addTodoItem(todoItem)
            dismiss()
        }
    }
}