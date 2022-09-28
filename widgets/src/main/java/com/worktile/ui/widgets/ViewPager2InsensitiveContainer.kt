package com.worktile.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * viewpager2中如果有viewpager2 orientation垂直方向滑动的内容，例如横向的viewpager2中有竖向滑动的RecyclerView，
 * 那么在滑动RecyclerView的时候，特别容易引起viewPager2的滑动，因此加上一层手势检测，在适当的时候禁止viewpager2响应
 */
class ViewPager2InsensitiveContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var scrollPointerId = INVALID_POINTER
    private var initialTouchX = 0
    private var initialTouchY = 0
    private var initialIsUserInputEnabled: Boolean? = null

    private val viewPager2: ViewPager2 by lazy {
        val child = getChildAt(0)
        if (child !is ViewPager2) {
            throw Exception("必须有且仅有一个ViewPager2")
        }
        if (initialIsUserInputEnabled == null) {
            initialIsUserInputEnabled = child.isUserInputEnabled
        }
        child
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        val actionIndex = ev.actionIndex
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = ev.getPointerId(0)
                initialTouchX = ev.x.toInt()
                initialTouchY = ev.y.toInt()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = ev.getPointerId(actionIndex)
                initialTouchX = ev.getX(actionIndex).toInt()
                initialTouchY = ev.getY(actionIndex).toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                if (!viewPager2.isUserInputEnabled) {
                    return super.dispatchTouchEvent(ev)
                }
                val index = ev.findPointerIndex(scrollPointerId)
                if (index < 0) {
                    return super.dispatchTouchEvent(ev)
                }
                val x = ev.getX(index).toInt()
                val y = ev.getY(index).toInt()

                when(viewPager2.orientation) {
                    ViewPager2.ORIENTATION_HORIZONTAL-> {
                        if (abs(x - initialTouchX) < abs(y - initialTouchY)) {
                            viewPager2.isUserInputEnabled = false
                        }
                    }

                    ViewPager2.ORIENTATION_VERTICAL -> {
                        if (abs(y - initialTouchY) < abs(x - initialTouchX)) {
                            viewPager2.isUserInputEnabled = false
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (scrollPointerId == ev.getPointerId(0)) {
                    initialTouchX = 0
                    initialTouchY = 0
                    viewPager2.isUserInputEnabled = initialIsUserInputEnabled ?: true
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (scrollPointerId == ev.getPointerId(actionIndex)) {
                    initialTouchX = 0
                    initialTouchY = 0
                    viewPager2.isUserInputEnabled = initialIsUserInputEnabled ?: true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                viewPager2.isUserInputEnabled = initialIsUserInputEnabled ?: true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        private const val INVALID_POINTER = -1
    }
}