package com.devjeong.todolist_study.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.SwipeHelperCallback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodoItemAdapter(
    private val todoList: MutableList<TodoItem>,
    private val deleteItemCallback: (todoItem: TodoItem) -> Unit
) : RecyclerView.Adapter<TodoItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = todoList[position]
        holder.bind(todoItem)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun getTodoItemAt(position: Int): Int? {
        return if (position in 0 until todoList.size) {
            todoList[position].id
        } else {
            null
        }
    }
    fun deleteItem(position: Int) {
        val deletedItem = todoList.removeAt(position)
        deleteItemCallback(deletedItem)
        notifyItemRemoved(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.TodoSaveContentTxt)
        private val isDoneCheckBox: CheckBox = itemView.findViewById(R.id.TodoIsDone)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.TodoSaveTimeTxt)

        fun bind(todoItem: TodoItem) {
            titleTextView.text = todoItem.title
            isDoneCheckBox.isChecked = todoItem.is_done

            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val parsedDateTime = LocalDateTime.parse(todoItem.created_at, formatter)

            val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            val formattedDateTime = parsedDateTime.format(outputFormatter)
            createdAtTextView.text = formattedDateTime
        }

        init {
            // Swipe 동작 시 onSwiped() 메서드 호출
            itemView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onSwiped()
                }
                false
            }
        }

        fun onSwiped() {
            val position = adapterPosition
            deleteItem(position)
        }
    }
}
