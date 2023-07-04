package com.devjeong.todolist_study.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.retrofit.ApiService
import com.devjeong.todolist_study.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TodoSearchViewModel : ViewModel() {
    private val _todoItem = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItem: StateFlow<List<TodoItem>> get() = _todoItem

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> get() = _snackMessage

    fun fetchTodoSearchItem(query: String, page: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.getTodoItem(query, page = page).execute()
                }
                if (response.isSuccessful) {
                    val todoResponse = response.body()
                    if (todoResponse != null) {
                        val todoItems = todoResponse.data
                        _todoItem.emit(todoItems)
                        callback(true)
                    }
                    if(response.code() == 204){
                        _snackMessage.emit(todoResponse?.message)
                    }
                } else {
                    Log.d("TodoViewModel", "API 호출 실패")
                    callback(false)
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
                callback(false)
            }
        }
    }
}
