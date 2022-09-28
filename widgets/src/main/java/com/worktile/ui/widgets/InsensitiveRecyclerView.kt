package com.worktile.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: Moki
 * Email: mosicou@gmail.com
 * Date: 2018/7/19
 * Time: 15:28
 * Desc: 普通的RecyclerView放在例如ViewPager2中，滑动会特别敏感，有时候上下滑动时，轻轻动一下横向，就横向滑动了，
 * 因此，自定义一个低敏感的RecyclerView，当滑动距离超过touchSlop才会响应
 */
class InsensitiveRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private var scrollPointerId = INVALID_POINTER
    private var initialTouchX = 0
    private var initialTouchY = 0
    private var touchSlop = 0

    init {
        val viewConfiguration = ViewConfiguration.get(context)
        touchSlop = viewConfiguration.scaledTouchSlop
    }

    override fun setScrollingTouchSlop(slopConstant: Int) {
        super.setScrollingTouchSlop(slopConstant)
        val viewConfiguration = ViewConfiguration.get(context)
        when (slopConstant) {
            TOUCH_SLOP_DEFAULT -> touchSlop = viewConfiguration.scaledTouchSlop
            TOUCH_SLOP_PAGING -> touchSlop = viewConfiguration.scaledPagingTouchSlop
            else -> {}
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        val action = e.actionMasked
        val actionIndex = e.actionIndex
        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                scrollPointerId = e.getPointerId(0)
                initialTouchX = (e.x + 0.5f).toInt()
                initialTouchY = (e.y + 0.5f).toInt()
                super.onInterceptTouchEvent(e)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                scrollPointerId = e.getPointerId(actionIndex)
                initialTouchX = (e.getX(actionIndex) + 0.5f).toInt()
                initialTouchY = (e.getY(actionIndex) + 0.5f).toInt()
                super.onInterceptTouchEvent(e)
            }
            MotionEvent.ACTION_MOVE -> {
                val index = e.findPointerIndex(scrollPointerId)
                if (index < 0) {
                    return false
                }
                val x = (e.getX(index) + 0.5f).toInt()
                val y = (e.getY(index) + 0.5f).toInt()
                if (scrollState != SCROLL_STATE_DRAGGING) {
                    val dx = x - initialTouchX
                    val dy = y - initialTouchY
                    val canScrollHorizontally = layoutManager!!.canScrollHorizontally()
                    val canScrollVertically = layoutManager!!.canScrollVertically()
                    var startScroll = false
                    if (canScrollHorizontally && abs(dx) > touchSlop && (abs(dx) >= abs(dy) || canScrollVertically)) {
                        startScroll = true
                    }
                    if (canScrollVertically && abs(dy) > touchSlop && (abs(dy) >= abs(dx) || canScrollHorizontally)) {
                        startScroll = true
                    }
                    return startScroll && super.onInterceptTouchEvent(e)
                }
                super.onInterceptTouchEvent(e)
            }
            else -> super.onInterceptTouchEvent(e)
        }
    }

    companion object {
        private const val INVALID_POINTER = -1
    }
}