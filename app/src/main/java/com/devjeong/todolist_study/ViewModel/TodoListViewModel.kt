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
}

