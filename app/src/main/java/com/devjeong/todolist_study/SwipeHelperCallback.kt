package com.devjeong.todolist_study

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.Adapter.TodoItemAdapter

class SwipeHelperCallback(private val adapter: TodoItemAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteButtonWidth: Int = 200 // 삭제 버튼의 가로 길이 (임의로 설정)
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        /*val position = viewHolder.adapterPosition
        adapter.deleteItem(position)*/
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val background = ColorDrawable(Color.RED)
        val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.baseline_delete_24)

        val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconBottom = iconTop + deleteIcon.intrinsicHeight

        if (dX > 0) {
            // 왼쪽으로 스와이프 시 삭제 버튼 표시
            val iconLeft = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            // Swipe 거리에 따라 삭제 버튼을 일부분만 보이도록 함
            val backgroundRight = itemView.left + dX.toInt()
            val backgroundLeft = backgroundRight - deleteButtonWidth
            background.setBounds(backgroundLeft, itemView.top, backgroundRight, itemView.bottom)
        }

        background.draw(c)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
