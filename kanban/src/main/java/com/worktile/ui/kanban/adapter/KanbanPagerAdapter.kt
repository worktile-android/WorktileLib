package com.worktile.ui.kanban.adapter

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class KanbanPagerAdapter : FragmentStateAdapter {
    constructor(fragment: Fragment) : super(fragment)
    constructor(activity: FragmentActivity) : super(activity)
    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle)
            : super(fragmentManager, lifecycle)

    internal var kanbanGestureDetector: GestureDetectorCompat? = null

    @SuppressLint("ClickableViewAccessibility")
    final override fun createFragment(position: Int): Fragment {
        val fragment = newFragment(position)
//        fragment.viewLifecycleOwnerLiveData.observe(fragment) {
//            if (it == null) return@observe
//            fragment.view?.let { view ->
//                if (view !is ViewGroup) return@let
//                findRecyclerViewAndPerform(view) { recyclerView ->
//                    recyclerView.apply {
//                        setOnTouchListener { _, event ->
//                            kanbanGestureDetector?.onTouchEvent(event)
//                            onTouchEvent(event)
//                        }
//                    }
//                }
//            }
//        }
        return fragment
    }

    abstract fun newFragment(position: Int): Fragment

    private fun findRecyclerViewAndPerform(viewGroup: ViewGroup, action: (RecyclerView) -> Unit) {
        for (index in 0 until viewGroup.childCount) {
            when (val child = viewGroup[index]) {
                is RecyclerView -> {
                    action.invoke(child)
                    continue
                }
                is ViewGroup -> {
                    findRecyclerViewAndPerform(child, action)
                }
            }
        }
    }
}