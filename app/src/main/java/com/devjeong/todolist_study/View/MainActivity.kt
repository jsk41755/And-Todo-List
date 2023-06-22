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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.SwipeHelperCallback
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

    private lateinit var groupedAdapters: MutableList<TodoItemAdapter> // 그룹별 TodoItemAdapter 저장 리스트

    private var hideCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TodoItemAdapter(mutableListOf()) { adapter, position ->
            deleteTodoItem(adapter, position)
        }
        groupedAdapters = mutableListOf() // 그룹별 TodoItemAdapter 리스트 초기화

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
            val items = group.value.toMutableList()

            val recyclerView = RecyclerView(this)
            recyclerView.layoutManager = LinearLayoutManager(this)
            //recyclerView.adapter = TodoItemAdapter(items)

            // 새로운 TodoItemAdapter 생성
            val groupAdapter = TodoItemAdapter(items) { adapter, position ->
                deleteTodoItem(adapter, position)
            }
            recyclerView.adapter = groupAdapter
            groupedAdapters.add(groupAdapter) // 그룹별 TodoItemAdapter 리스트에 추가

            val dateTextView = TextView(this)
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTypeface(null, Typeface.BOLD)

            containerLayout.addView(dateTextView)
            containerLayout.addView(recyclerView)

            // 스와이프 동작을 감지하는 ItemTouchHelper 등록
            val itemTouchHelper = ItemTouchHelper(SwipeHelperCallback(groupAdapter))
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }
    }

    private fun observeTodoSearchItem(todoItem: TodoItem) {
        containerLayout.removeAllViews()

        val changedUpdatedAt = todoItem.updated_at.replace("-", ".").substring(0, 10)

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val items = mutableListOf(todoItem) // 검색 결과 아이템을 리스트에 추가

        val adapter = TodoItemAdapter(items) { adapter, position ->
            deleteTodoItem(adapter, position)
        }
        recyclerView.adapter = adapter

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

    private fun deleteTodoItem(groupAdapter: TodoItemAdapter, position: Int) {
        val adapterPosition = calculateAdapterPosition(groupAdapter, position)
        adapterPosition?.let {
            groupAdapter.deleteItem(it)
        }
    }
    private fun calculateAdapterPosition(groupAdapter: TodoItemAdapter, position: Int): Int? {
        var adapterPosition = position
        for (adapter in groupedAdapters) {
            if (adapter === groupAdapter) {
                return adapterPosition
            }
            adapterPosition += adapter.itemCount
        }
        return null
    }

}