package com.devjeong.todolist_study.Model

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class TodoResponse(
    val data: List<TodoItem>,
    val message: String
)

data class TodoSearchResponse(
    val data: TodoItem,
    val message: String
)

data class TodoItem(
    val id: Int,
    val title: String,
    var is_done: Boolean,
    val created_at: String,
    val updated_at: String
)

data class TodoItemDTO(
    @SerializedName("title") val title: String,
    @SerializedName("is_done") val is_done: Boolean
)

data class Meta(
    val current_page: Int,
    val from: Int,
    val last_page: Int,
    val per_page: Int,
    val to: Int,
    val total: Int
)


