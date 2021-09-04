package com.worktile.ui.recyclerview.binder

import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.livedata.AdapterLiveDataValue
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

fun <T> RecyclerView.bind(
    viewModel: T,
    owner: LifecycleOwner,
    config: Config = Config(),
    updateCallback: (() -> Unit)? = null
) where T : RecyclerViewViewModel, T : ViewModel {
    layoutManager = LinearLayoutManager(this.context)
    val adapter: SimpleAdapter<ItemDefinition> = SimpleAdapter(
        viewModel.adapterData.value?.items?.toMutableList() ?: mutableListOf(),
        owner
    )

    viewModel.apply {
        loadingState.init(this, config)
        edgeState.init(this, config)
        recyclerViewData.init(this, config)
    }

    viewModel.edgeState.scrollToEndEventLiveData.observe(owner) {
        adapter.apply { scrollToPosition(itemCount - 1) }
    }

    fun isToEnd(): Boolean {
        if (adapter.data.size == 1) {
            adapter.data[0].run item@ {
                viewModel.loadingState.apply {
                    if (this@item == loadingItemViewModel
                        || this@item == emptyItemViewModel
                        || this@item == failureItemViewModel) {
                        return false
                    }
                }
            }
        }
        val lastChildView = layoutManager!!.getChildAt(layoutManager!!.childCount - 1) ?: return false
        val lastChildPosition = IntArray(2)
        lastChildView.getLocationInWindow(lastChildPosition)
        val lastChildBottom = lastChildView.height + lastChildPosition[1]
        val recyclerViewPosition = IntArray(2)
        getLocationInWindow(recyclerViewPosition)
        val recyclerBottom = height + recyclerViewPosition[1] - paddingBottom
        val lastPosition = layoutManager!!.getPosition(lastChildView)
        return lastChildBottom == recyclerBottom && lastPosition == layoutManager!!.itemCount - 1
    }

    if (config.loadMoreOnFooter) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val isEnd: Boolean = isToEnd()
                    val canAutoLoadMore = viewModel.edgeState.state.run {
                        return@run this == EdgeState.INIT || this == EdgeState.SUCCESS
                    }
                    if (isEnd && canAutoLoadMore) {
                        viewModel.onLoadMore?.invoke()
                    }
                }
            }
        })
    }

    setAdapter(adapter)

    viewModel.adapterData.run {
        removeObserve()
        observerWrapper.realObserver = Observer {
            adapter.updateData({ it.items }) {
                it.updateCallback.invoke()
                it.updateCallback = {}
                updateCallback?.invoke()
            }
        }
        observe(owner)
    }

    owner.lifecycle.run {
        addObserver(adapter)
    }

    viewModel.registerClearCallback {
        println("$viewModel onClear")
    }
}

class Config {
    var loadingViewCreator: ViewCreator? = null
    var emptyViewCreator: ViewCreator? = null
    var failureViewCreator: ViewCreator? = null

    var loadMoreOnFooter = true
    var footerLoadingViewCreator: ViewCreator? = null
    var footerNoMoreViewCreator: ViewCreator? = null
    var footerFailureViewCreator: ViewCreator? = null
}