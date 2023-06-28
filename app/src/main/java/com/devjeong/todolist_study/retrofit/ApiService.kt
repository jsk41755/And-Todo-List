package com.devjeong.todolist_study.retrofit

import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.Model.TodoItemDTO
import com.devjeong.todolist_study.Model.TodoResponse
import com.devjeong.todolist_study.Model.TodoSearchResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("todos")
    fun getTodoLists(
        @Query("page") page: Int = 1,
        @Query("filter") filter: String = "created_at",
        @Query("order_by") orderBy: String = "desc",
        @Query("per_page") perPage: Int = 15
    ): Call<TodoResponse>

    //검색 기능인데 프래그먼트로 바꾸고 로직 처리를 달리 해야함.
    @GET("todos/search")
    fun getTodoItem(
        @Query("query") title: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Call<TodoResponse>

    @GET("todos/{id}")
    fun getTodoDetailItem(
        @Path("id") itemId: String
    ): Call<TodoSearchResponse>

    @DELETE("todos/{id}")
    fun deleteTodoItem(@Path("id") todoId: Int
    ): Call<Void>

    @FormUrlEncoded
    @PUT("todos/{id}")
    fun updateTodoItem(
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("is_done") isDone: Boolean
    ): Call<TodoItem>

    @POST("todos")
    suspend fun addTodo(
        @Body todoItemDTO: TodoItemDTO
    ): Response<ResponseBody>
}

