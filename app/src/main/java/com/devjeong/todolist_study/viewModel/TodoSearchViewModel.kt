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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TodoSearchViewModel : ViewModel() {
    private val _todoItem = MutableLiveData<List<TodoItem>>()
    val todoItem: LiveData<List<TodoItem>> get() = _todoItem

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

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
                        _todoItem.value = todoItems
                        callback(true) // 성공적으로 데이터를 가져왔음을 알림
                    }
                } else {
                    Log.d("TodoViewModel", "API 호출 실패")
                    callback(false) // 데이터 가져오기 실패를 알림
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
                callback(false) // 데이터 가져오기 실패를 알림
            }
        }
    }
}
