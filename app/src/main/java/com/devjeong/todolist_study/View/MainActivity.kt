package com.devjeong.todolist_study.View

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.ViewModel.TodoListViewModel
import com.devjeong.todolist_study.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private lateinit var todoViewModel: TodoListViewModel
    private lateinit var adapter: TodoItemAdapter

    private var hideCompleted = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TodoItemAdapter(emptyList()) // 초기에는 빈 리스트로 설정

        // ViewModel 초기화
        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]

        fetchTodoItems()

        binding.CompleteBtn.setOnClickListener {
            hideCompleted = !hideCompleted
            fetchTodoItems()
        }
    }

    private fun observeTodoItems(todoItems: List<TodoItem>) {
        val containerLayout = binding.containerLayout

        containerLayout.removeAllViews() // 기존의 RecyclerView 삭제

        val filteredItems = if (hideCompleted) {
            todoItems.filter { !it.is_done }
        } else {
            todoItems
        }

        val groupedItems = filteredItems.groupBy {
            val changedUpdatedAt = it.updated_at.replace("-", ".")
            changedUpdatedAt.substring(0, 10)
        }

        for (group in groupedItems) {
            val date = group.key
            val items = group.value

            val recyclerView = RecyclerView(this)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = TodoItemAdapter(items)

            val dateTextView = TextView(this)
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTypeface(null, Typeface.BOLD)

            containerLayout.addView(dateTextView)
            containerLayout.addView(recyclerView)
        }
    }

    private fun fetchTodoItems() {
        todoViewModel.fetchTodoItems()

        todoViewModel.todoItems.observe(this) { todoItems ->
            observeTodoItems(todoItems)
        }
    }
}