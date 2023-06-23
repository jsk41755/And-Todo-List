package com.devjeong.todolist_study.View

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseActivity
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.TodoListItemHelper
import com.devjeong.todolist_study.ViewModel.TodoListViewModel
import com.devjeong.todolist_study.ViewModel.TodoViewModel
import com.devjeong.todolist_study.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private lateinit var todoViewModel: TodoListViewModel
    private lateinit var searchViewModel: TodoViewModel
    private lateinit var adapter: TodoItemAdapter
    private lateinit var searchView: SearchView
    private lateinit var containerLayout: LinearLayout
    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var groupedAdapters: MutableList<TodoItemAdapter> // 그룹별 TodoItemAdapter 저장 리스트

    private var hideCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TodoItemAdapter(mutableListOf(), deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
        })
        groupedAdapters = mutableListOf() // 그룹별 TodoItemAdapter 리스트 초기화

        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        searchView = binding.searchView
        containerLayout = binding.containerLayout
        refreshLayout = binding.refreshLayout

        todoViewModel.deleteResult.observe(this) { result ->
            if (result) {
                fetchTodoItems()
                Log.d("TodoViewModel", "삭제 성공")
            } else {
                // 삭제 실패 처리
            }
        }

        fetchTodoItems()

        refreshLayout.setDistanceToTriggerSync(400)
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            fetchTodoItems()
        }

        binding.CompleteBtn.setOnClickListener {
            hideCompleted = !hideCompleted
            fetchTodoItems()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val title = query?.trim()
                fetchTodoSearchItems(title)
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

            val groupAdapter = TodoItemAdapter(items, deleteItemCallback = { todoItem ->
                deleteTodoItem(todoItem)
            }, updateItemCallback = { todoItem ->
                updateTodoItem(todoItem)
            })

            recyclerView.adapter = groupAdapter
            groupedAdapters.add(groupAdapter)

            recyclerView.isNestedScrollingEnabled = false

            val dateTextView = TextView(this)
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTypeface(null, Typeface.BOLD)

            containerLayout.addView(dateTextView)
            containerLayout.addView(recyclerView)


            // 리사이클러뷰에 스와이프, 드래그 기능 달기
            val swipeHelperCallback = TodoListItemHelper(groupAdapter).apply {
                // 스와이프한 뒤 고정시킬 위치 지정
                setClamp(resources.displayMetrics.widthPixels.toFloat() / 4)    // 1080 / 4 = 270
            }
            ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(recyclerView)

            recyclerView.setOnTouchListener { _, _ ->
                swipeHelperCallback.removePreviousClamp(recyclerView)
                false
            }
        }
    }

    private fun observeTodoSearchItem(todoItems: List<TodoItem>) {
        containerLayout.removeAllViews()

        for (todoItem in todoItems) {
            val changedUpdatedAt = todoItem.updated_at.replace("-", ".").substring(0, 10)

            val recyclerView = RecyclerView(this)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val items = mutableListOf(todoItem) // 검색 결과 아이템을 리스트에 추가

            val adapter = TodoItemAdapter(items, deleteItemCallback = { item ->
                deleteTodoItem(item)
            }, updateItemCallback = { item ->
                updateTodoItem(item)
            })

            recyclerView.adapter = adapter

            val dateTextView = TextView(this)
            dateTextView.text = changedUpdatedAt
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

    private fun fetchTodoSearchItems(query: String? = null) {
        if (query?.isNotEmpty() == true) {
            searchViewModel.fetchTodoSearchItem(query)
            searchViewModel.todoItem.observe(this) { todoItems ->
                observeTodoSearchItem(todoItems)
            }
            searchViewModel.toastMessage.observe(this) { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "입력을 해주세요!!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteTodoItem(todoItem: TodoItem) {
        todoViewModel.deleteTodoItem(todoItem.id)
    }

    private fun updateTodoItem(item: TodoItem) {
        todoViewModel.updateTodoItem(item)
    }
}