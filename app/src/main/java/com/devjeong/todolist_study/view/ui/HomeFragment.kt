package com.devjeong.todolist_study.view.ui

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import com.devjeong.todolist_study.BaseFragment
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.TodoListItemHelper
import com.devjeong.todolist_study.databinding.FragmentHomeBinding
import com.devjeong.todolist_study.view.custom_dialog.CustomDialogInterface
import com.devjeong.todolist_study.view.custom_dialog.ui.CustomDialog
import com.devjeong.todolist_study.viewModel.TodoListViewModel
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.view.custom_dialog.ui.ProgressDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var todoViewModel: TodoListViewModel
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

    private lateinit var customProgressDialog: ProgressDialog

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customProgressDialog = ProgressDialog(requireContext())
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        customProgressDialog.setCancelable(false)
        customProgressDialog.show()

        adapter = TodoItemAdapter(mutableListOf(), deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
        })
        groupedAdapters = mutableListOf() // 그룹별 TodoItemAdapter 리스트 초기화
        groupRecyclerViews = mutableListOf() // 그룹별 RecyclerView 리스트 초기화

        todoViewModel = ViewModelProvider(this)[TodoListViewModel::class.java]
        containerLayout = binding.containerLayout
        scrollView = binding.scrollView

        todoViewModel.deleteResult.onEach { _ ->
            fetchTodoItems {
                customProgressDialog.dismiss()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        todoViewModel.snackMessage
            .filter { message -> message.isNotBlank() }
            .onEach {message ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        setupScrollListener()

        binding.refreshLayout.setDistanceToTriggerSync(400)
        binding.refreshLayout.setOnRefreshListener {
            customProgressDialog.show()

            binding.refreshLayout.isRefreshing = false
            fetchTodoItems { _ ->
                customProgressDialog.dismiss()
            }
        }

        binding.addDialogBtn.setOnClickListener {
            val customDialog = CustomDialog(this@HomeFragment, object : CustomDialogInterface {
                override fun onAddButtonClicked() {
                    currentPage = 1
                    containerLayout.removeAllViews()
                    fetchTodoItems { _ ->
                        customProgressDialog.dismiss()
                    }
                }
            })
            customDialog.show()
        }

        binding.CompleteBtn.setOnClickListener {
            customProgressDialog.show()

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
                fetchTodoItems { _ ->
                    customProgressDialog.dismiss()
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val fragment = SearchFragment()
                val bundle = Bundle()
                bundle.putString("query", query)
                fragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)  // 수정: fragment 변수를 전달
                    .addToBackStack(null)
                    .commit()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        fetchTodoItems { _ ->
            customProgressDialog.dismiss()
        }
    }

    private fun createGroupRecyclerView(date: String, items: MutableList<TodoItem>) {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val groupAdapter = TodoItemAdapter(items, deleteItemCallback = { todoItem ->
            deleteTodoItem(todoItem)
        }, updateItemCallback = { todoItem ->
            updateTodoItem(todoItem)
            fetchTodoItems { _ ->
                customProgressDialog.dismiss()
            }
        })

        groupAdapter.setOnItemClickListener(object : TodoItemAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: TodoItem, pos: Int) {
                Log.d("isClicked", currentPage.toString())
                val customDialog = CustomDialog(this@HomeFragment, object : CustomDialogInterface {
                    override fun onAddButtonClicked() {
                        currentPage = 1
                        //containerLayout.removeAllViews()
                        fetchTodoItems { _ ->
                            customProgressDialog.dismiss()
                        }
                    }
                })
                customDialog.show()
                customDialog.setData(data.id, data.title, data.is_done)
            }
        })

        recyclerView.adapter = groupAdapter
        groupedAdapters.add(groupAdapter)

        recyclerView.isNestedScrollingEnabled = false

        Log.d("beforeDate", beforeDate.toString())
        if(!date.equals(beforeDate)){
            val dateTextView = TextView(requireContext())
            dateTextView.text = date
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            dateTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            dateTextView.setTypeface(null, Typeface.BOLD)
            containerLayout.addView(dateTextView)
        }
        containerLayout.addView(recyclerView)

        beforeDate = date

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
            containerLayout.removeAllViews()
        }

        for (group in groupedItems) {
            val date = group.key
            val items = group.value.toMutableList()

            if (items.size <= 1 && !isNewData) continue

            val index = groupedAdapters.indexOfFirst { it.date == date }
            if (index != -1) {
                val adapter = groupedAdapters[index]
                adapter.updateItems(items)
            } else {
                createGroupRecyclerView(date, items)
            }
        }
    }

    private fun fetchTodoItems(completion: (Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            todoViewModel.fetchTodoItems(currentPage) { success ->
                val todoItems = todoViewModel.todoItems.value ?: emptyList()
                if (currentPage > 1) {
                    observeTodoItems(todoItems, false)
                } else {
                    observeTodoItems(todoItems, true)
                }
                completion(success)
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
                todoViewModel.fetchTodoItems(currentPage) { success ->
                    isFetchingData = false

                    // 새로운 아이템을 가져와서 기존 groupRecyclerView에 추가
                    val todoItems = todoViewModel.todoItems.value ?: emptyList()
                    if (currentPage > 1) {
                        binding.progressBar.visibility = View.VISIBLE
                        observeTodoItems(todoItems, false)
                        binding.progressBar.visibility = View.INVISIBLE
                    } else {
                        observeTodoItems(todoItems, true)
                    }
                }
            }
        }
    }
}