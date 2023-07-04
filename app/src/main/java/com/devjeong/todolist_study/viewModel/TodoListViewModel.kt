package com.devjeong.todolist_study.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.Model.TodoItemDTO
import com.devjeong.todolist_study.retrofit.ApiService
import com.devjeong.todolist_study.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TodoListViewModel : ViewModel() {
    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems: StateFlow<List<TodoItem>> get() = _todoItems

    private val _deleteResult = MutableStateFlow(false)
    val deleteResult: StateFlow<Boolean> get() = _deleteResult

    private val _snackMessage = MutableStateFlow("")
    val snackMessage: StateFlow<String> get() = _snackMessage
    fun fetchTodoItems(page: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.getTodoLists(page = page).execute()
                }

                if (response.isSuccessful) {
                    val todoResponse = response.body()
                    if (todoResponse != null) {
                        val todoItems = todoResponse.data
                        _todoItems.emit(todoItems)
                        callback(true)
                    }
                    if(response.code() == 204){
                        _snackMessage.emit("마지막 페이지 입니다.")
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
    fun deleteTodoItem(todoId: Int) {
        viewModelScope.launch {
            try {
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    retrofit.deleteTodoItem(todoId).execute()
                }
                if (response.isSuccessful) {
                    _deleteResult.emit(true)
                    Log.d("TodoViewModel", "삭제 성공")
                } else {
                    _deleteResult.emit(false)
                    Log.d("TodoViewModel", "삭제 실패")
                }
            } catch (e: IOException) {
                _deleteResult.emit(false)
                Log.e("TodoViewModel", "삭제 실패: ${e.message}")
            }
        }
    }

    fun addTodoItem(todoItem: TodoItemDTO) {
        viewModelScope.launch {
            try {
                val todoItemDTO = TodoItemDTO(todoItem.title, todoItem.is_done)
                val retrofit = RetrofitClient.createService(ApiService::class.java)
                val response = retrofit.addTodo(todoItemDTO)

                if (response.isSuccessful) {
                    Log.d("TodoViewModel", "API 호출 성공")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val decodedMessage = decodeUnicodeEscapeSequence(errorBody.toString())
                    Log.d("TodoViewModel", "API 호출 실패, 오류 메시지: $decodedMessage")
                    _snackMessage.emit(decodedMessage)
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
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


                    }
                } else {
                    Log.d("TodoViewModel", "API 호출 실패, ${response.message()}")
                }
            } catch (e: IOException) {
                Log.e("TodoViewModel", "API 호출 실패: ${e.message}")
            }
        }
    }

    private fun decodeUnicodeEscapeSequence(input: String): String {
        return input.replace("\\\\u([0-9a-fA-F]{4})".toRegex()) { matchResult ->
            val hexCode = matchResult.groupValues[1]
            try {
                val unicodeValue = hexCode.toInt(16)
                unicodeValue.toChar().toString()
            } catch (e: NumberFormatException) {
                matchResult.value // 유효하지 않은 유니코드 이스케이프 시퀀스는 그대로 유지
            }
        }
    }
}

