package com.devjeong.todolist_study.Retrofit

import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.Model.TodoResponse
import com.devjeong.todolist_study.Model.TodoSearchResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("todos")
    fun getTodoLists(
        @Query("page") page: Int = 1,
        @Query("filter") filter: String = "created_at",
        @Query("order_by") orderBy: String = "desc",
        @Query("per_page") perPage: Int = 10
    ): Call<TodoResponse>

    @GET("todos/{id}")
    fun getTodoItem(
        @Path("id") itemId: String
    ): Call<TodoSearchResponse>
}

