package com.devjeong.todolist_study.View

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.ViewModel.TodoListViewModel
import com.devjeong.todolist_study.ViewModel.TodoViewModel
import com.devjeong.todolist_study.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private lateinit var todoViewModel: TodoListViewModel
    private lateinit var searchViewModel: TodoViewModel
    private lateinit var adapter: TodoItemAdapter
    private lateinit var searchView: SearchView
    private lateinit var containerLayout: LinearLayout

    private var hideCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TodoItemAdapter(emptyList())
        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        searchView = binding.searchView
        containerLayout = binding.containerLayout

        fetchTodoItems()

        binding.CompleteBtn.setOnClickListener {
            hideCompleted = !hideCompleted
            fetchTodoItems()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val itemId = query?.trim()
                fetchTodoSearchItems(itemId)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun observeTodoItems(todoItems: List<TodoItem>) {
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

    private fun observeTodoSearchItem(todoItem: TodoItem) {
        containerLayout.removeAllViews() // 기존의 RecyclerView 삭제

        val changedUpdatedAt = todoItem.updated_at.replace("-", ".").substring(0, 10)

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TodoItemAdapter(listOf(todoItem))

        val dateTextView = TextView(this)
        dateTextView.text = changedUpdatedAt
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
        dateTextView.setTypeface(null, Typeface.BOLD)

        containerLayout.addView(dateTextView)
        containerLayout.addView(recyclerView)
    }


    private fun fetchTodoItems() {
        todoViewModel.fetchTodoItems()

        todoViewModel.todoItems.observe(this) { todoItems ->
            observeTodoItems(todoItems)
        }
    }

    private fun fetchTodoSearchItems(itemId: String ?= null) {
        if(itemId?.isNotEmpty()!!){
            searchViewModel.fetchTodoSearchItem(itemId)
            searchViewModel.todoItem.observe(this) { todoItem ->
                observeTodoSearchItem(todoItem)
            }
            searchViewModel.toastMessage.observe(this) { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else{
            Toast.makeText(this, "입력을 해주세요!!", Toast.LENGTH_SHORT).show()
        }


    }
}