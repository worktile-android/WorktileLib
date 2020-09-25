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
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.viewmodels.LoadingStateItemViewModel
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.item_empty.view.*

fun RecyclerView.bind(
    data: RecyclerViewViewModel,
    owner: LifecycleOwner,
    config: Config? = null
) {
    var adapter: SimpleAdapter<Definition>? = null
    data.recyclerViewData.observe(owner) {
        val cloneDataList = mutableListOf<Definition>()
        cloneDataList.addAll(it)
        adapter?.updateData(cloneDataList) ?: run {
            adapter = SimpleAdapter(cloneDataList, owner)
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(this.context)

            val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
                override fun viewCreator() = config?.loadingViewCreator ?: { parent: ViewGroup ->
                    LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
                }
            }

            val emptyItemViewModel = object : LoadingStateItemViewModel(LoadingState.EMPTY) {
                override fun viewCreator() = config?.emptyViewCreator ?: { parent: ViewGroup ->
                    LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
                }
            }

            val failureItemViewModel = object : LoadingStateItemViewModel(LoadingState.FAILED) {
                override fun viewCreator() = config?.failureViewCreator ?: { parent: ViewGroup ->
                    LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false).apply {
                        empty_hint.text = parent.context.getString(R.string.retry_hint)
                        setOnClickListener {
                            data.loadingState.value = LoadingState.LOADING
                            config?.failureRetry?.invoke()
                        }
                    }
                }
            }

            data.loadingState.observe(owner) { state ->
                when (state) {
                    LoadingState.LOADING -> adapter?.updateData(listOf(loadingItemViewModel))
                    LoadingState.EMPTY -> adapter?.updateData(listOf(emptyItemViewModel))
                    LoadingState.FAILED -> adapter?.updateData(listOf(failureItemViewModel))
                    else -> { }
                }
            }

            data.footerState.observe(owner) footer@ { state ->
                if (config?.loadMoreOnFooter == false) return@footer
                when (state) {
                    EdgeState.LOADING -> {
                        val clonedDataList = mutableListOf<Definition>()
                        clonedDataList.addAll(data.recyclerViewData.value ?: emptyList())
                        clonedDataList.add(EdgeItemViewModel(
                            EdgeState.LOADING,
                            config?.footerLoadingViewCreator,
                            R.layout.item_footer_loading
                        ))
                        adapter?.updateData(clonedDataList)
                    }
                }
            }
        }
    }
}

internal class EdgeItemViewModel(
    key: EdgeState,
    private val creator: ViewCreator?,
    private val layoutId: Int
) : LoadingStateItemViewModel(key) {
    override fun viewCreator() = creator ?: {
        LayoutInflater.from(it.context).inflate(layoutId, it, false)
    }
}

class Config {
    var loadingViewCreator: ViewCreator? = null
    var emptyViewCreator: ViewCreator? = null
    var failureViewCreator: ViewCreator? = null
    var failureRetry: (() -> Unit)? = null

    var loadMoreOnFooter = true
    var footerLoadingViewCreator: ViewCreator? = null
    var footerNoMoreViewCreator: ViewCreator? = null
    var footerFailureViewCreator: ViewCreator? = null
    var footerFailureRetry: (() -> Unit)? = null
}