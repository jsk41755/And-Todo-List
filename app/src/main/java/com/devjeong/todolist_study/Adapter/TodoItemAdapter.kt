package com.devjeong.todolist_study.Adapter

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Model.TodoItem
import com.devjeong.todolist_study.R
import com.google.android.material.snackbar.Snackbar
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

            if(todoItem.is_done){
                titleTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                titleTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray500))
                createdAtTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                createdAtTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray500))
            }

            removeTxt.setOnClickListener {
                removeData(this.layoutPosition)
                val snackBar= Snackbar.make(it, "삭제완료!", Snackbar.LENGTH_SHORT)
                snackBar.setAction("확인"){
                    //복원 기능을 넣어야 하나 고민
                }
                snackBar.show()
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