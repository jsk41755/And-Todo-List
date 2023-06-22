package com.devjeong.todolist_study.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R

class TodoItemAdapter(private val todoList: List<TodoItem>) :
    RecyclerView.Adapter<TodoItemAdapter.ViewHolder>() {

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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.TodoSaveContentTxt)
        private val isDoneCheckBox: CheckBox = itemView.findViewById(R.id.TodoIsDone)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.TodoSaveTimeTxt)

        fun bind(todoItem: TodoItem) {
            titleTextView.text = todoItem.title
            isDoneCheckBox.isChecked = todoItem.is_done
            createdAtTextView.text = todoItem.created_at
        }
    }
}
