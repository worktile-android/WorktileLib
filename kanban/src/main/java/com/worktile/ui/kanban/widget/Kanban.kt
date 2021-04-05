package com.worktile.ui.kanban.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.worktile.ui.R
import com.worktile.ui.kanban.adapter.KanbanPagerAdapter

class Kanban : FrameLayout {
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        init(context, attributeSet)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attributeSet, defStyleAttr, defStyleRes) {
        init(context, attributeSet)
    }

    var peekOffset = resources.getDimensionPixelOffset(R.dimen.peekOffset)
    var pagerAdapter: KanbanPagerAdapter? = null
        set(value) {
            value?.kanbanGestureDetector = gestureDetector
            field = value
            viewPager.adapter = value
        }

    private val viewPager = ViewPager2(context)
    private val pagerRecyclerView by lazy { viewPager.getChildAt(0) as RecyclerView }
    private val snapHelper by lazy {
        ViewPager2::class.java
            .getDeclaredField("mPagerSnapHelper")
            .apply {
                isAccessible = true
            }
            .get(viewPager) as? SnapHelper
    }

    private val gestureDetector = GestureDetectorCompat(context, GestureDetectorListener())
    private var onLongPressed = false
    private var selectedItemViewHolder: RecyclerView.ViewHolder? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context, attributeSet: AttributeSet?) {
        viewPager.apply {
            offscreenPageLimit = 1
        }
        pagerRecyclerView.apply {
            setPadding(peekOffset, 0, peekOffset, 0)
            clipToPadding = false
            setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                onTouchEvent(event)
            }
        }
        addView(viewPager, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private inner class GestureDetectorListener : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent?) {
            if (e == null) return
            onLongPressed = true
            val contentRecyclerView = findContentRecyclerViewUnder(e.rawX, e.rawY)
            contentRecyclerView?.apply {
                val location = intArrayOf(0, 0)
                getLocationOnScreen(location)
                val itemView = findChildViewUnder(
                    e.rawX - location[0],
                    e.rawY - location[1]
                )
                selectedItemViewHolder = itemView?.run {
                    val viewHolder = getChildViewHolder(this)
                    viewHolder
                }
                println("onLongPress")
            }
            selectedItemViewHolder?.apply {
                selectItem(e, this)
            }
        }
    }

    /**
     * 在[MotionEvent.ACTION_DOWN]的时候，寻找contentRecyclerView，并为它设置OnTouchListener，是为了让其在
     * 响应触摸事件的时候可以同时让[gestureDetector]响应，从而避免kanban的[gestureDetector]因为触摸事件被
     * recyclerView消耗而无法完整执行手势检测流程的问题，例如无法正确remove LONG_PRESS handler message
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                onLongPressed = false
                selectedItemViewHolder = null
                findContentRecyclerViewUnder(ev.rawX, ev.rawY)?.apply {
                    setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event)
                        onTouchEvent(event)
                    }
                }
            }
        }
        handleTouchEvent(ev)
        return onLongPressed || super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        handleTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun handleTouchEvent(event: MotionEvent?) {
        gestureDetector.onTouchEvent(event)
        when(event?.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                selectedItemViewHolder?.apply {
                    dropItem(event, this)
                }
            }
        }
    }

    private fun selectItem(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        println("selectItem")
    }

    private fun dropItem(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        println("dropItem")
    }

    private fun findContentRecyclerViewUnder(rawX: Float, rawY: Float): RecyclerView? {
        var result: RecyclerView? = null
        val pagerRecyclerViewLocation = intArrayOf(0, 0)
        pagerRecyclerView.getLocationOnScreen(pagerRecyclerViewLocation)
        val x = rawX - pagerRecyclerViewLocation[0]
        val y = rawY - pagerRecyclerViewLocation[1]
        val pagerView = pagerRecyclerView.findChildViewUnder(x, y)
        (pagerView as? ViewGroup)?.apply {
            findRecyclerViewAndPerform(this) {
                val location = intArrayOf(0, 0)
                it.getLocationOnScreen(location)
                val left = location[0]
                val top = location[1]
                val right = left + it.measuredWidth
                val bottom = top + it.measuredHeight
                if (rawX >= left && rawX <= right && rawY >= top && rawY <= bottom) {
                    result = it
                    true
                } else {
                    false
                }
            }
        }
        return result
    }

    private fun findRecyclerViewAndPerform(
        viewGroup: ViewGroup,
        action: (RecyclerView) -> Boolean
    ) {
        for (index in 0 until viewGroup.childCount) {
            when (val child = viewGroup[index]) {
                is RecyclerView -> {
                    if (action.invoke(child)) return else continue
                }
                is ViewGroup -> {
                    findRecyclerViewAndPerform(child, action)
                }
            }
        }
    }
}