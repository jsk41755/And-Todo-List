package com.devjeong.todolist_study.view.custom_dialog.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.Model.TodoItemDTO
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.view.custom_dialog.CustomDialogInterface
import com.devjeong.todolist_study.view.ui.HomeFragment
import com.devjeong.todolist_study.viewModel.TodoListViewModel
import kotlinx.coroutines.flow.collect

class CustomDialog(
    private val fragment: HomeFragment,
    private val dialogInterface: CustomDialogInterface
) : Dialog(fragment.requireActivity()), LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var addButton: Button
    private lateinit var todoTitle : TextView
    private lateinit var todoSwitch : Switch

    private lateinit var todoViewModel: TodoListViewModel

    private var itemId: Int = 0
    private var itemTitle: String = ""
    private var itemIsDone: Boolean = false
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

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        todoViewModel = ViewModelProvider(fragment.requireActivity())[TodoListViewModel::class.java]

        addButton = findViewById(R.id.addBtn)
        todoTitle = findViewById(R.id.todoTitle)
        todoSwitch = findViewById(R.id.isDoneSwitch)

        addButton.setOnClickListener {
            if (itemId != 0) {
                itemTitle = todoTitle.text.toString()
                itemIsDone = todoSwitch.isChecked
                val todoItem = TodoItem(itemId, itemTitle, itemIsDone, "", "")
                Log.d("수정 완료", todoItem.toString())
                todoViewModel.updateTodoItem(todoItem)

                dialogInterface.onAddButtonClicked()
                dismiss()
            } else {
                val todoItem = TodoItemDTO(todoTitle.text.toString(), todoSwitch.isChecked)
                Log.d("추가 완료", "${todoTitle.text}, ${todoSwitch.isChecked}")
                todoViewModel.addTodoItem(todoItem)

                lifecycleScope.launchWhenStarted {
                    todoViewModel.snackMessage.collect{message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        Log.d("TodoViewModel", message.toString())
                    }
                }

                if(todoTitle.text.toString().length >= 6){
                    Toast.makeText(context, "추가 완료!", Toast.LENGTH_SHORT).show()
                    dialogInterface.onAddButtonClicked()
                    dismiss()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }
    fun setData(id: Int, title: String, isDone: Boolean) {
        itemId = id
        itemTitle = title
        itemIsDone = isDone

        todoTitle.text = title
        todoSwitch.isChecked = isDone
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}