package com.devjeong.todolist_study.Model

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
    val is_done: Boolean,
    val created_at: String,
    val updated_at: String
)

data class Meta(
    val current_page: Int,
    val from: Int,
    val last_page: Int,
    val per_page: Int,
    val to: Int,
    val total: Int
)


