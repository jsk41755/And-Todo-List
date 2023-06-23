package com.devjeong.todolist_study.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.Retrofit.ApiService
import com.devjeong.todolist_study.Retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TodoListViewModel : ViewModel() {
    private val _todoItems = MutableLiveData<List<TodoItem>>()
    val todoItems: LiveData<List<TodoItem>> get() = _todoItems

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult
    fun fetchTodoItems() {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.getTodoLists().execute()
                }

                if (response.isSuccessful) {
                    val todoResponse = response.body()
                    if (todoResponse != null) {
                        val todoItems = todoResponse.data
                        _todoItems.value = todoItems
                    }
                } else {
                    Log.d("TodoViewModel", "API 호출 실패")
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
            }
        }
    }
    fun deleteTodoItem(todoId: Int) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.deleteTodoItem(todoId).execute()
                }
                if (response.isSuccessful) {
                    _deleteResult.value = true
                    Log.d("TodoViewModel", "삭제 성공")
                } else {
                    _deleteResult.value = false
                    Log.d("TodoViewModel", "삭제 실패")
                }
            } catch (e: IOException) {
                _deleteResult.value = false
                Log.e("TodoViewModel", "삭제 실패: ${e.message}")
            }
        }
    }

    fun updateTodoItem(todoItem: TodoItem) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.updateTodoItem(todoItem.id, todoItem.title, todoItem.is_done)
                        .execute()
                }

                if (response.isSuccessful) {
                    val updatedTodoItem = response.body()
                    if (updatedTodoItem != null) {
                        // 업데이트된 아이템 처리
                        // 필요한 로직을 추가해주세요
                    }
                } else {
                    Log.d("TodoViewModel", "API 호출 실패")
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
            }
        }
    }
}

