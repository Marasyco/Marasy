package com.blueray.marasy.helpers

import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    context: Context,
    private val childHandlers: Map<Int, (row: View, position: Int) -> Unit> = emptyMap(),
    private val onItemClick: (row: View, position: Int) -> Unit
) : RecyclerView.SimpleOnItemTouchListener() {

    private val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent) = true
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            // Don’t let SwipeRefreshLayout or parents hijack this gesture
            rv.requestDisallowInterceptTouchEvent(true)
        }

        val row = rv.findChildViewUnder(e.x, e.y) ?: return false

        // 1) Check if tap landed on any registered child (e.g., favoriteCheckBox)
        if (childHandlers.isNotEmpty()) {
            val rawX = e.rawX.toInt()
            val rawY = e.rawY.toInt()
            val rect = Rect()

            for ((childId, handler) in childHandlers) {
                val child = row.findViewById<View>(childId) ?: continue
                if (!child.isShown) continue
                child.getGlobalVisibleRect(rect)
                if (rect.contains(rawX, rawY)) {
                    val pos = rv.getChildAdapterPosition(row)
                    if (pos != RecyclerView.NO_POSITION) {
                        handler(row, pos)
                        return true // consume: we handled the child click
                    }
                }
            }
        }

        // 2) Otherwise treat it as a row tap
        if (detector.onTouchEvent(e)) {
            val pos = rv.getChildAdapterPosition(row)
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick(row, pos)
                return true
            }
        }
        return false
    }
}
