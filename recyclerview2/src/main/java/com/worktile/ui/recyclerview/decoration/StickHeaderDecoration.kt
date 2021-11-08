package com.worktile.ui.recyclerview.decoration

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.ItemGroup
import com.worktile.ui.recyclerview.SimpleAdapter

class StickHeaderDecoration(
    private val itemGroups: List<ItemGroup>
) : RecyclerView.ItemDecoration() {
    private val titleItemDefinitions = mutableSetOf<ItemDefinition>().apply {
        itemGroups.forEach { group ->
            val title = group.title
            if (title != null) {
                add(title)
            }
        }
    }
    private var currentTitleView: View? = null

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val adapter = parent.adapter as? SimpleAdapter<*>
        val layoutManager = parent.layoutManager as? LinearLayoutManager
        if (adapter == null || layoutManager == null) return
        if (currentTitleView == null) {
            val firstPosition = layoutManager.findFirstVisibleItemPosition()
            if (titleItemDefinitions.contains(adapter.data[firstPosition])) {
                currentTitleView = parent.findViewHolderForAdapterPosition(firstPosition)
                    ?.apply {
                        setIsRecyclable(false)
                        parent.recycledViewPool.setMaxRecycledViews(itemViewType, 0)
                    }
                    ?.itemView
                    ?.apply {
                        parent.layoutManager?.ignoreView(this)
                    }
            }
        }
        currentTitleView?.apply {
            translationY = -top.toFloat()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = parent.adapter as? SimpleAdapter<*>
        val layoutManager = parent.layoutManager as? LinearLayoutManager
        if (adapter == null || layoutManager == null) return

    }
}