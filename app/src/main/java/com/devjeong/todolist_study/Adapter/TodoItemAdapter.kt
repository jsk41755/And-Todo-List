package com.devjeong.todolist_study.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.devjeong.todolist_study.view.custom_dialog.ui.CustomDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodoItemAdapter(
    private val todoList: MutableList<TodoItem>,
    private val deleteItemCallback: (todoItem: TodoItem) -> Unit,
    private val updateItemCallback: (todoItem: TodoItem) -> Unit
) : RecyclerView.Adapter<TodoItemAdapter.ViewHolder>() {

    var date: String = ""

    interface OnItemClickListener{
        fun onItemClick(v:View, data: TodoItem, pos : Int)
    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        Log.d("isClicked", date)
        this.listener = listener
    }

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
    fun updateItems(items: List<TodoItem>) {
        todoList.clear()
        todoList.addAll(items)
        notifyDataSetChanged()
    }

    fun removeData(position: Int) {
        val deletedItem = todoList.removeAt(position)
        notifyItemRemoved(position)
        deleteItemCallback(deletedItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.TodoSaveContentTxt)
        private val isDoneCheckBox: CheckBox = itemView.findViewById(R.id.TodoIsDone)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.TodoSaveTimeTxt)
        private val removeTxt: TextView = itemView.findViewById(R.id.RemoveTxt)

        fun bind(todoItem: TodoItem) {
            titleTextView.text = todoItem.title
            isDoneCheckBox.isChecked = todoItem.is_done

            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val parsedDateTime = LocalDateTime.parse(todoItem.created_at, formatter)

            val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            val formattedDateTime = parsedDateTime.format(outputFormatter)
            createdAtTextView.text = formattedDateTime

            removeTxt.setOnClickListener {
                removeData(this.layoutPosition)
                Toast.makeText(itemView.context, "삭제완료!", Toast.LENGTH_SHORT).show()
            }

            isDoneCheckBox.setOnCheckedChangeListener(null)
            isDoneCheckBox.setOnCheckedChangeListener { _, isChecked ->
                todoItem.is_done = isChecked
                updateItemCallback(todoItem)
            }

            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, todoItem, pos)
                }
            }
        }
    }
}