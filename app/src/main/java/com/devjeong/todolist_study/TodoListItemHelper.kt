package com.devjeong.todolist_study

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.getTag
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devjeong.todolist_study.Adapter.TodoItemAdapter
import kotlin.math.min

class TodoListItemHelper(private val adapter: TodoItemAdapter):
    ItemTouchHelper.Callback() {

    private var currentPosition: Int? = null // 현재 선택된 recycler view의 position
    private var previousPosition: Int? = null // 이전에 선택했던 recycler view의 position
    private var currentDx = 0f // 현재 x 값
    private var clamp = 0f

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int) {

        //val todoViewHolder = viewHolder as TodoItemAdapter.ViewHolder
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f //현재 x 위치 초기화
        previousPosition = viewHolder.adapterPosition // 드래그 또는 스와이프 동작이 끝난 view의 position 기억하기
        getDefaultUIUtil().clearView(getView(viewHolder))
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
        if (actionState == ACTION_STATE_SWIPE) {
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)
            val newX = clampViewPositionHorizontal(dX, isClamped, isCurrentlyActive)  // newX 만큼 이동(고정 시 이동 위치/고정 해제 시 이동 위치 결정

            if(newX == -clamp){
                getView(viewHolder).animate().translationX(-clamp).setDuration(100L).start()
                return
            }

            currentDx = newX
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                view,
                newX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10

    // swipe_view 반환 -> swipe_view만 이동할 수 있게 해줌
    private fun getView(viewHolder: RecyclerView.ViewHolder) : View = viewHolder.itemView.findViewById(R.id.swipe_view)

    private fun clampViewPositionHorizontal(
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ): Float {
        //Right 방향으로 swipe 막기
        val max = 0f

        val newX = if (isClamped) {
            // 현재 swipe 중이면 swipe되는 영역 제한
            if(isCurrentlyActive)
                //  오른쪽 swipe일 때
                if(dX < 0) dX/3 - clamp
                //왼쪽 swipe일 때
                else dX - clamp
            //swipe 중이 아니면 고정시키
            else -clamp
        }
        else dX / 2

        return min(newX, max)
    }

    // isClamped를 view의 tag로 관리
    // isClamped = true : 고정, false : 고정 해제
    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) {
        viewHolder.itemView.tag = isClamped
    }
    private fun getTag(viewHolder: ViewHolder) : Boolean = viewHolder.itemView.tag as? Boolean ?: false

    fun setClamp(clamp: Float) { this.clamp = clamp }

    fun removePreviousClamp(recyclerView: RecyclerView) {
        // 현재 선택한 view가 이전에 선택한 view와 같으면 패스
        if (currentPosition == previousPosition) return

        // 이전에 선택한 위치의 view 고정 해제
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).animate().x(0f).setDuration(100L).start()
            setTag(viewHolder, false)
            previousPosition = null
        }

    }

}