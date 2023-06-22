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
    private val deleteItemCallback: (adapter: TodoItemAdapter, position: Int) -> Unit
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

    fun deleteItem(position: Int) {
        if (position in 0 until todoList.size) {
            todoList.removeAt(position)
            notifyItemRemoved(position)
            // 삭제한 이후의 아이템들의 인덱스 조정
            notifyItemRangeChanged(position, todoList.size - position)
        }
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
