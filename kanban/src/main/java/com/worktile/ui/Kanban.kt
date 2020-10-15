package com.worktile.ui

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class Kanban : ViewGroup {
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

    var pageMargin = resources.getDimensionPixelOffset(R.dimen.pageMargin)
    var peekOffset = resources.getDimensionPixelOffset(R.dimen.peekOffset)

    private val viewPager2 = ViewPager2(context)
    private val tmpContainerRect = Rect()
    private val tmpChildRect = Rect()

    private fun init(context: Context, attributeSet: AttributeSet?) {
        viewPager2.run {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            offscreenPageLimit = 1
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                val padding = pageMargin / 2 + peekOffset
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                // TODO: expose in later versions not to rely on getChildAt(0) which might break
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
        }
        attachViewToParent(viewPager2, 0, viewPager2.layoutParams)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(viewPager2, widthMeasureSpec, heightMeasureSpec)
        var width: Int = viewPager2.measuredWidth
        var height: Int = viewPager2.measuredHeight
        val childState: Int = viewPager2.measuredState

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        width = width.coerceAtLeast(suggestedMinimumWidth)
        height = height.coerceAtLeast(suggestedMinimumHeight)

        setMeasuredDimension(
            resolveSizeAndState(width, widthMeasureSpec, childState),
            resolveSizeAndState(
                height, heightMeasureSpec,
                childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width: Int = viewPager2.measuredWidth
        val height: Int = viewPager2.measuredHeight

        tmpContainerRect.left = paddingLeft
        tmpContainerRect.right = r - l - paddingRight
        tmpContainerRect.top = paddingTop
        tmpContainerRect.bottom = b - t - paddingBottom

        Gravity.apply(Gravity.TOP or Gravity.START, width, height, tmpContainerRect, tmpChildRect)
        viewPager2.layout(
            tmpChildRect.left, tmpChildRect.top, tmpChildRect.right,
            tmpChildRect.bottom
        )
    }
}