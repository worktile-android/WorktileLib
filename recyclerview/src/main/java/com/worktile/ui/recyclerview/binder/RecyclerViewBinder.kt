package com.worktile.ui.recyclerview.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.base.Worktile
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

private val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
    override fun viewCreator() = { parent: ViewGroup ->
        LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
    }
}

private val emptyItemViewModel = object : LoadingStateItemViewModel(LoadingState.EMPTY) {
    override fun viewCreator() = { _: ViewGroup ->
        TextView(Worktile.applicationContext).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }
}

abstract class LoadingStateItemViewModel(private val key: Any) : DiffItemViewModel, Definition {
    override fun key(): Any = key
    override fun type() = key()
    override fun bind(itemView: View) {}
}

fun RecyclerView.bind(
    data: RecyclerViewViewModel,
    owner: LifecycleOwner
) {
    var adapter: SimpleAdapter<Definition>? = null
    data.recyclerViewData.observe(owner) {
        val cloneDataList = mutableListOf<Definition>()
        cloneDataList.addAll(it)
        adapter?.updateData(cloneDataList) ?: run {
            adapter = SimpleAdapter(cloneDataList, owner)
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(this.context)

            data.loadingState.observe(owner) { state ->
                when (state) {
                    LoadingState.LOADING -> {
                        adapter?.updateData(listOf(loadingItemViewModel))
                    }
                }
            }
        }
    }
}