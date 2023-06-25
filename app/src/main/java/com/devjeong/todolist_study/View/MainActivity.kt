package com.devjeong.todolist_study.View

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
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
import com.devjeong.todolist_study.View.CustomDialog.CustomDialog
import com.devjeong.todolist_study.View.CustomDialog.CustomDialogInterface
import com.devjeong.todolist_study.ViewModel.TodoListViewModel
import com.devjeong.todolist_study.ViewModel.TodoViewModel
import com.devjeong.todolist_study.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private lateinit var todoViewModel: TodoListViewModel
    private lateinit var searchViewModel: TodoViewModel
    private lateinit var adapter: TodoItemAdapter
    private lateinit var containerLayout: LinearLayout
    private lateinit var scrollView: ScrollView

    private lateinit var groupedAdapters: MutableList<TodoItemAdapter> // 그룹별 TodoItemAdapter 저장 리스트

    private var hideCompleted = false
    private var filteredItems: List<TodoItem> = emptyList()

    private var currentPage = 1
    private var isFetchingData = false

    private var beforeDate : String ?= ""

    private lateinit var groupRecyclerViews: MutableList<RecyclerView> // 그룹별 RecyclerView 저장 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TodoItemAdapter(mutableListOf(), deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
        })
        groupedAdapters = mutableListOf() // 그룹별 TodoItemAdapter 리스트 초기화
        groupRecyclerViews = mutableListOf() // 그룹별 RecyclerView 리스트 초기화

        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        containerLayout = binding.containerLayout
        //refreshLayout = binding.refreshLayout
        scrollView = binding.scrollView

        todoViewModel.deleteResult.observe(this) { result ->
            if (result) {
                fetchTodoItems()
                Log.d("TodoViewModel", "삭제 성공")
            } else {
                // 삭제 실패 처리
            }
        }

        fetchTodoItems()
        setupScrollListener()

        /*refreshLayout.setDistanceToTriggerSync(400)
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            fetchTodoItems()
        }*/

        binding.addDialogBtn.setOnClickListener {
            val customDialog = CustomDialog(this@MainActivity, object : CustomDialogInterface {
                override fun onAddButtonClicked() {
                    // Add 버튼이 클릭되었을 때의 동작 처리
                    currentPage = 1
                    fetchTodoItems()
                }
            })

            customDialog.show()
        }

        binding.CompleteBtn.setOnClickListener {
            hideCompleted = !hideCompleted
            fetchTodoItems()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
    private fun createGroupRecyclerView(date: String, items: MutableList<TodoItem>) {
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

        Log.d("beforeDate", beforeDate.toString())
        if(!date.equals(beforeDate)){
            val dateTextView = TextView(this)
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTypeface(null, Typeface.BOLD)
            containerLayout.addView(dateTextView)
        }
        containerLayout.addView(recyclerView)

        beforeDate = date

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
    private fun observeTodoItems(todoItems: List<TodoItem>, isNewData: Boolean) {
        filteredItems = if (hideCompleted) {
            todoItems.filter { !it.is_done }
        } else {
            todoItems
        }

        val groupedItems = filteredItems.groupBy {
            it.updated_at.replace("-", ".").substring(0, 10) // updated_at 값을 기준으로 그룹화
        }

        if (isNewData) {
            containerLayout.removeAllViews() // 기존의 dateTextView 제거
        }

        for (group in groupedItems) {
            val date = group.key
            val items = group.value.toMutableList()

            // 동일한 updated_at 값을 가진 경우에는 생성을 건너뜁니다.
            if (items.size <= 1 && !isNewData) continue

            val index = groupedAdapters.indexOfFirst { it.date == date }
            if (index != -1) {
                val adapter = groupedAdapters[index]
                adapter.updateItems(items) // 기존의 adapter에 새로운 아이템 추가
            } else {
                createGroupRecyclerView(date, items)
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

            beforeDate = changedUpdatedAt

            containerLayout.addView(dateTextView)
            containerLayout.addView(recyclerView)
        }
    }

    private fun fetchTodoItems() {
        todoViewModel.fetchTodoItems(currentPage) { success ->
            val todoItems = todoViewModel.todoItems.value ?: emptyList()
            if (currentPage > 1) {
                observeTodoItems(todoItems, false)
            } else {
                observeTodoItems(todoItems, true)
            }
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

    private fun setupScrollListener() {
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = view.bottom - (scrollView.height + scrollView.scrollY)
            if (diff == 0 && !isFetchingData) {
                // 스크롤이 화면 하단에 도달한 경우
                isFetchingData = true // 중복 호출 방지를 위해 플래그 설정
                currentPage++ // 페이지 값 증가
                todoViewModel.fetchTodoItems(currentPage) { success ->
                    isFetchingData = false // 데이터 호출이 완료되면 플래그 해제

                    // 새로운 아이템을 가져와서 기존 groupRecyclerView에 추가
                    val todoItems = todoViewModel.todoItems.value ?: emptyList()
                    if (currentPage > 1) {
                        observeTodoItems(todoItems, false)
                    } else {
                        observeTodoItems(todoItems, true)
                    }
                }
            }
        }
    }
}