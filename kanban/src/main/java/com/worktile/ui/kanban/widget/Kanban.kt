package com.worktile.ui.kanban.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.worktile.ui.R
import com.worktile.ui.kanban.adapter.KanbanPagerAdapter
import java.util.*
import kotlin.math.abs
import kotlin.math.sign


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

    companion object {
        private val dragScrollInterpolator = Interpolator { t ->
            t * t * t * t * t
        }
        private val dragViewScrollCapInterpolator = Interpolator { input ->
            val t = input - 1f
            t * t * t * t * t + 1f
        }
        private const val DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000L
    }

    private var peekOffset = 0
    var pagerAdapter: KanbanPagerAdapter? = null
        set(value) {
            value?.kanban = this
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
    private var currentContentRecyclerView: RecyclerView? = null
    private var onLongPressed = false
    internal var selectedItemViewHolder: RecyclerView.ViewHolder? = null
    private var selectedStartEvent: MotionEvent? = null
    // 当长按时，落点和itemView左上角的offset值
    private var selectedLongPressOffsetX: Float = 0f
    private var selectedLongPressOffsetY: Float = 0f
    private val dragScrollStartTimeInMsMap = WeakHashMap<RecyclerView, Long>()
    private var currentRawX: Float? = null
    private var currentRawY: Float? = null
    private var lastTouchContentRecyclerView: RecyclerView? = null
    private val swapTargetsMap = WeakHashMap<RecyclerView, MutableList<RecyclerView.ViewHolder>>()
    private val distancesMap = WeakHashMap<RecyclerView, MutableList<Int>>()

    private val windowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val dragWindowParams by lazy {
        WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            alpha = 1.0f
            format = PixelFormat.TRANSLUCENT
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
    }
    private val dragView by lazy {
        ImageView(context).apply {
            setPadding(0, 0, 0, 0)
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        }
    }
    private val maxScrollSpeed by lazy {
        (20 * Resources.getSystem().displayMetrics.density).toInt()
    }

    private val viewPagerScrollRunnable = object : Runnable {
        override fun run() {
            if (selectedItemViewHolder != null && scrollIfNecessary(pagerRecyclerView)) {
                pagerRecyclerView.removeCallbacks(this)
                ViewCompat.postOnAnimation(pagerRecyclerView, this)
            }
        }
    }
    private val contentRecyclerViewScrollRunnable = object : Runnable {
        override fun run() {
            val contentRecyclerView = currentContentRecyclerView
            if (contentRecyclerView != null &&
                selectedItemViewHolder != null &&
                scrollIfNecessary(contentRecyclerView)) {
                contentRecyclerView.removeCallbacks(this)
                ViewCompat.postOnAnimation(contentRecyclerView, this)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Kanban)
        peekOffset = typedArray.getDimensionPixelOffset(
            R.styleable.Kanban_pagePeekOffset,
            resources.getDimensionPixelOffset(R.dimen.peekOffset)
        )
        typedArray.recycle()
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
            currentContentRecyclerView?.apply {
                val location = intArrayOf(0, 0)
                getLocationOnScreen(location)
                val itemView = findChildViewUnder(
                    e.rawX - location[0],
                    e.rawY - location[1]
                )
                selectedItemViewHolder = itemView?.run {
                    val viewHolder = getChildViewHolder(this)
                    viewHolder.setIsRecyclable(false)
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
                currentContentRecyclerView?.apply {
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
        event?.apply {
            currentContentRecyclerView = findContentRecyclerViewUnder(event.rawX, event.rawY)
        }
        gestureDetector.onTouchEvent(event)
        when(event?.action) {
            MotionEvent.ACTION_MOVE -> {
                currentRawX = event.rawX
                currentRawY = event.rawY
                selectedItemViewHolder?.apply {
                    translateItem(event, this)
                    pagerRecyclerView.removeCallbacks(viewPagerScrollRunnable)
                    viewPagerScrollRunnable.run()
                    currentContentRecyclerView?.apply {
                        removeCallbacks(contentRecyclerViewScrollRunnable)
                        contentRecyclerViewScrollRunnable.run()
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                selectedItemViewHolder?.apply {
                    dropItem(event, this)
                }
            }
        }
    }

    private fun selectItem(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        println("selectItem")
        dragScrollStartTimeInMsMap.clear()
        val selectedView = selected.itemView.apply {
            selectedStartEvent = event
            val location = intArrayOf(0, 0)
            getLocationOnScreen(location)
            selectedLongPressOffsetX = event.rawX - location[0]
            selectedLongPressOffsetY = event.rawY - location[1]
        }
        val selectedBitmap = selectedView.run {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            bitmap
        }
        dragWindowParams.apply {
            val itemLocation = intArrayOf(0, 0)
            selected.itemView.getLocationOnScreen(itemLocation)
            x = itemLocation[0]
            y = itemLocation[1]
        }
        dragView.apply {
            setImageBitmap(selectedBitmap)
        }
        windowManager.addView(dragView, dragWindowParams)
        dragView.post { selectedView.visibility = View.INVISIBLE }
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                dragView.apply {
                    rotation = 5 * value
                    alpha = 1f - 0.2f * value
                }
            }
            duration = 200
            start()
        }
        snapHelper?.attachToRecyclerView(null)
    }

    private fun translateItem(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        dragWindowParams.apply {
            x = (event.rawX - selectedLongPressOffsetX).toInt()
            y = (event.rawY - selectedLongPressOffsetY).toInt()
        }
        windowManager.updateViewLayout(dragView, dragWindowParams)
    }

    private fun dropItem(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        println("dropItem")
        windowManager.removeView(dragView)
        selectedItemViewHolder?.itemView?.visibility = View.VISIBLE
        selectedItemViewHolder = null
        snapHelper?.attachToRecyclerView(pagerRecyclerView)
    }

    private fun scrollIfNecessary(
        recyclerView: RecyclerView
    ): Boolean {
        val selected = selectedItemViewHolder ?: run {
            dragScrollStartTimeInMsMap.remove(recyclerView)
            return false
        }
        val now = System.currentTimeMillis()
        val dragScrollStartTimeInMs = dragScrollStartTimeInMsMap[recyclerView]
        val scrollDuration = if (dragScrollStartTimeInMs == null) {
            0
        } else {
            now - dragScrollStartTimeInMs
        }

        val layoutManager = recyclerView.layoutManager ?: return false
        val startEvent = this.selectedStartEvent ?: return false
        val currentRawX = this.currentRawX ?: return false
        val currentRawY = this.currentRawY ?: return false
        val offsetX = selectedLongPressOffsetX
        val offsetY = selectedLongPressOffsetY

        val recyclerViewLocation = intArrayOf(0, 0)
        recyclerView.getLocationOnScreen(recyclerViewLocation)
        val xDiff = run {
            if (layoutManager.canScrollHorizontally()) {
                if (currentRawX < startEvent.rawX) {
                    val itemLeft = currentRawX - offsetX + selected.itemView.paddingLeft
                    val paddingLeft = if (recyclerView == pagerRecyclerView) {
                        0
                    } else {
                        recyclerView.paddingLeft
                    }
                    val leftDiff = itemLeft - recyclerViewLocation[0] - paddingLeft
                    if (leftDiff < 0) {
                        return@run leftDiff.toInt()
                    }
                } else {
                    val itemRight = currentRawX + selected.itemView.width - offsetX -
                            selected.itemView.paddingRight
                    val paddingRight = if (recyclerView == pagerRecyclerView) {
                        0
                    } else {
                        recyclerView.paddingRight
                    }
                    val rightDiff = itemRight - recyclerViewLocation[0] - recyclerView.width +
                            paddingRight
                    if (rightDiff > 0) {
                        return@run rightDiff.toInt()
                    }
                }
            }
            0
        }

        val yDiff = run {
            if (layoutManager.canScrollVertically()) {
                if (currentRawY < startEvent.rawY) {
                    val itemTop = currentRawY - offsetY
                    val topDiff = itemTop - recyclerViewLocation[1] - recyclerView.paddingTop
                    if (topDiff < 0) {
                        return@run topDiff.toInt()
                    }
                } else {
                    val itemBottom = currentRawY + selected.itemView.height - offsetY
                    val bottomDiff = itemBottom - recyclerViewLocation[1] - recyclerView.height +
                            recyclerView.paddingBottom
                    if (bottomDiff > 0) {
                        return@run bottomDiff.toInt()
                    }
                }
            }
            0
        }

        val scrollX = if (xDiff != 0) {
            interpolateOutOfBoundsScroll(selected.itemView.width, xDiff, scrollDuration)
        } else 0
        val scrollY = if (yDiff != 0) {
            interpolateOutOfBoundsScroll(selected.itemView.height, yDiff, scrollDuration)
        } else 0
        if (scrollX != 0 || scrollY != 0) {
            if (dragScrollStartTimeInMs == null) {
                dragScrollStartTimeInMsMap[recyclerView] = now
            }
            recyclerView.scrollBy(scrollX, scrollY)
            return true
        }
        dragScrollStartTimeInMsMap.remove(recyclerView)
        return false
    }

    /**
     * see [ItemTouchHelper.Callback.interpolateOutOfBoundsScroll]
     */
    private fun interpolateOutOfBoundsScroll(
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        msSinceStartScroll: Long
    ): Int {
        val absOutOfBounds = abs(viewSizeOutOfBounds)
        val direction = sign(viewSizeOutOfBounds.toFloat()).toInt()
        // might be negative if other direction
        val outOfBoundsRatio = 1f.coerceAtMost(1f * absOutOfBounds / viewSize)
        val cappedScroll = direction * maxScrollSpeed *
                dragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio)
        val timeRatio: Float = if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
            1f
        } else {
            msSinceStartScroll.toFloat() / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS
        }
        val value = (cappedScroll * dragScrollInterpolator.getInterpolation(timeRatio)).toInt()
        return if (value == 0) {
            if (viewSizeOutOfBounds > 0) 1 else -1
        } else value
    }

    private fun moveIfNecessary(event: MotionEvent, selected: RecyclerView.ViewHolder) {
        val contentRecyclerView = findContentRecyclerViewUnder(event.rawX, event.rawY)
        if (lastTouchContentRecyclerView != null && contentRecyclerView == null) {
            // 移出last
        } else if (lastTouchContentRecyclerView == null && contentRecyclerView != null) {
            // 移入现在的contentRecyclerView
        }
        lastTouchContentRecyclerView = contentRecyclerView

        if (contentRecyclerView?.isLayoutRequested == true) {
            return
        }



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