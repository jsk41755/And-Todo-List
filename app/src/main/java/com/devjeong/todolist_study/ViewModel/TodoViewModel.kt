package com.devjeong.todolist_study.ViewModel

import android.util.Log
import android.widget.Toast
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

class TodoViewModel : ViewModel() {
    private val _todoItem = MutableLiveData<TodoItem>()
    val todoItem: LiveData<TodoItem> get() = _todoItem

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun fetchTodoSearchItem(itemId: String) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.getTodoItem(itemId).execute()
                }
                Log.d("TodoViewModel", response.toString())

                if (response.isSuccessful) {
                    val todoResponse = response.body()
                    if (todoResponse != null) {
                        val todoItem = todoResponse.data
                        _todoItem.value = todoItem
                    } else if (response.code() == 204){
                        _toastMessage.value = "API 응답에 데이터가 없습니다."
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
