package com.worktile.ui.recyclerview.decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.worktile.common.Worktile
import com.worktile.common.utils.UnitConversion

class LineDecoration : RecyclerView.ItemDecoration() {
    private val paint: Paint = Paint()

    var marginLeft = 72f
    var lineHeight = 0.5f
    var color = Color.parseColor("#f1f1f1")
    var colorResource: Int? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        Worktile.activityContext?.let { context ->
            for (i in 0 until parent.childCount) {
                val drawLeft = parent.left + UnitConversion.dp2px(context, marginLeft).toFloat()
                val drawRight = parent.right.toFloat()
                val drawBottom = parent.getChildAt(i).bottom.toFloat()
                val drawTop = drawBottom - UnitConversion.dp2px(context, lineHeight)
                c.drawRect(drawLeft, drawTop, drawRight, drawBottom, paint)
            }
        }
    }

    init {
        colorResource?.apply {
            Log.w("LineDecoration", "设置了colorResource，优先使用colorResource")
            Worktile.activityContext?.run {
                paint.color = ContextCompat.getColor(this, this@apply)
            }
        } ?: kotlin.run { paint.color = color }
    }
}