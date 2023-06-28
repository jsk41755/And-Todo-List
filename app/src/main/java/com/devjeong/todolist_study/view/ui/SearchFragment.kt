package com.devjeong.todolist_study.view.ui

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseFragment
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.TodoListItemHelper
import com.devjeong.todolist_study.databinding.FragmentSearchBinding
import com.devjeong.todolist_study.viewModel.TodoListViewModel
import com.devjeong.todolist_study.viewModel.TodoSearchViewModel

class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private var query: String? = null

    private lateinit var todoViewModel: TodoListViewModel
    private lateinit var searchViewModel: TodoSearchViewModel
    private lateinit var adapter: TodoItemAdapter
    private lateinit var containerLayout: LinearLayout
    private lateinit var scrollView: NestedScrollView

    private lateinit var groupedAdapters: MutableList<TodoItemAdapter> // 그룹별 TodoItemAdapter 저장 리스트

    private var hideCompleted = false
    private var filteredItems: List<TodoItem> = emptyList()

    private var currentPage = 1
    private var isFetchingData = false

    private var beforeDate : String ?= ""

    private lateinit var groupRecyclerViews: MutableList<RecyclerView> // 그룹별 RecyclerView 저장 리스트

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            query = it.getString("query")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        Navigation.setViewNavController(requireView(), navController)

        Log.d("query", query ?: "Query is null")
        binding.searchTitle.text = "${query}(으)로 검색된 결과"

        adapter = TodoItemAdapter(mutableListOf(), deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
        })
        groupedAdapters = mutableListOf() // 그룹별 TodoItemAdapter 리스트 초기화
        groupRecyclerViews = mutableListOf() // 그룹별 RecyclerView 리스트 초기화

        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[TodoSearchViewModel::class.java]
        containerLayout = binding.containerLayout
        scrollView = binding.scrollView

        todoViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            fetchTodoSearchItems(query!!)
        }

        fetchTodoSearchItems(query!!)
        setupScrollListener()

        binding.CompleteBtn.setOnClickListener {
            hideCompleted = !hideCompleted
            if(!hideCompleted){
                binding.CompleteBtn.text = "완료 숨기기"
            } else {
                binding.CompleteBtn.text = "전체 보기"
            }
            beforeDate = ""
            var tempPage = currentPage
            currentPage = 1
            for(i in 1..tempPage){
                fetchTodoSearchItems(query!!)
            }
        }

        searchViewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createGroupRecyclerView(date: String, items: MutableList<TodoItem>) {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val groupAdapter = TodoItemAdapter(items, deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
        })

        recyclerView.adapter = groupAdapter
        groupedAdapters.add(groupAdapter)

        recyclerView.isNestedScrollingEnabled = false

        if(!date.equals(beforeDate)){
            val dateTextView = TextView(requireContext())
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTypeface(null, Typeface.BOLD)
            containerLayout.addView(dateTextView)
        }
        containerLayout.addView(recyclerView)

        beforeDate = date

        val swipeHelperCallback = TodoListItemHelper(groupAdapter).apply {
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
            containerLayout.removeAllViews()
        }

        for (group in groupedItems) {
            val date = group.key
            val items = group.value.toMutableList()

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
    private fun observeTodoSearchItem(todoItems: List<TodoItem>, isNewData: Boolean) {
        filteredItems = if (hideCompleted) {
            todoItems.filter { !it.is_done }
        } else {
            todoItems
        }

        val groupedItems = filteredItems.groupBy {
            it.updated_at.replace("-", ".").substring(0, 10) // updated_at 값을 기준으로 그룹화
        }

        if (isNewData) {
            containerLayout.removeAllViews()
        }

        for (group in groupedItems) {
            val date = group.key
            val items = group.value.toMutableList()

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

    private fun fetchTodoSearchItems(query: String) {
        searchViewModel.fetchTodoSearchItem(query, currentPage) {
            val todoItems = searchViewModel.todoItem.value ?: emptyList()
            if (currentPage > 1) {
                observeTodoItems(todoItems, false)
            } else {
                observeTodoItems(todoItems, true)
            }
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

                isFetchingData = true
                currentPage++
                searchViewModel.fetchTodoSearchItem(query!!, currentPage) { success ->
                    isFetchingData = false

                    // 새로운 아이템을 가져와서 기존 groupRecyclerView에 추가
                    val todoItems = searchViewModel.todoItem.value ?: emptyList()
                    if (currentPage > 1) {
                        observeTodoSearchItem(todoItems, false)
                    } else {
                        observeTodoSearchItem(todoItems, true)
                    }
                }
            }
        }
    }
}