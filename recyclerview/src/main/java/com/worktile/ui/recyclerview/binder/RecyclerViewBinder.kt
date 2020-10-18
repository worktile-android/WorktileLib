package com.worktile.ui.recyclerview.binder

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.common.arch.livedata.lazyObserve
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.utils.livedata.set
import com.worktile.ui.recyclerview.viewmodels.LoadingStateItemViewModel
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStatePair
import kotlinx.android.synthetic.main.item_empty.view.*

fun <T> RecyclerView.bind(
    data: T,
    owner: LifecycleOwner,
    config: Config = Config()
) where T : RecyclerViewViewModel, T : ViewModel {
    val adapter: SimpleAdapter<ItemDefinition> = SimpleAdapter(data.binderCache.adapterData ?: mutableListOf(), owner)
    val updateCallback = { data.binderCache.adapterData = adapter.data }
    val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
        override fun viewCreator() = config.loadingViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
        }
    }

    val emptyItemViewModel = object : LoadingStateItemViewModel(LoadingState.EMPTY) {
        override fun viewCreator() = config.emptyViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
        }
    }

    val failureItemViewModel = object : LoadingStateItemViewModel(LoadingState.FAILED) {
        override fun viewCreator() = config.failureViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false).apply {
                empty_hint.text = parent.context.getString(R.string.retry_hint)
                if (data.onLoadFailedRetry != null) {
                    setOnClickListener {
                        data.loadingState.value = LoadingState.LOADING
                        data.onLoadFailedRetry?.invoke()
                    }
                }
            }
        }
    }

    fun isToEnd(): Boolean {
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

    fun observeLoadingState(state: LoadingState) {
        if (state == LoadingState.INIT) return
        data.binderCache.latestUpdateType = UpdateType.LoadingState
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "loadingState changed: $state")
        }
        when (state) {
            LoadingState.LOADING -> {
                adapter.updateData({ listOf(loadingItemViewModel) }, "LOADING", updateCallback)
            }
            LoadingState.EMPTY -> {
                adapter.updateData({ listOf(emptyItemViewModel) }, "EMPTY", updateCallback)
            }
            LoadingState.FAILED -> {
                adapter.updateData({ listOf(failureItemViewModel) }, "FAILED", updateCallback)
            }
            LoadingState.SUCCESS -> {
                adapter.updateData({ emptyList() }, "SUCCESS", updateCallback)
            }
            else -> {
            }
        }
    }

    fun updateFooterItemViewModel(
        currentData: MutableList<ItemDefinition>,
        footerItemViewModel: EdgeItemViewModel?,
        scrollToEnd: Boolean = false
    ) {
        adapter.updateData({
            mutableListOf<ItemDefinition>().apply {
                addAll(currentData)
                footerItemViewModel?.run {
                    add(footerItemViewModel)
                }
            }
        }, "updateFooter") {
            updateCallback()
            if (scrollToEnd) scrollToPosition(adapter.itemCount - 1)
        }
    }

    fun observeEdgeState(statePair: EdgeStatePair) {
        if (!config.loadMoreOnFooter || statePair.state == EdgeState.INIT) return
        data.binderCache.latestUpdateType = UpdateType.EdgeState
        when (statePair.state) {
            EdgeState.LOADING -> {
                data.binderCache.isLoadingMore = true
                updateFooterItemViewModel(
                    statePair.currentData,
                    EdgeItemViewModel(
                        EdgeState.LOADING,
                        config.footerLoadingViewCreator,
                        R.layout.item_footer_loading
                    ),
                    true
                )
            }

            EdgeState.NO_MORE -> {
                data.binderCache.isLoadingMore = false
                updateFooterItemViewModel(
                    statePair.currentData,
                    EdgeItemViewModel(
                        EdgeState.NO_MORE,
                        config.footerNoMoreViewCreator,
                        R.layout.item_footer_no_more
                    )
                )
            }

            EdgeState.FAILED -> {
                updateFooterItemViewModel(statePair.currentData, object : EdgeItemViewModel(
                    EdgeState.FAILED,
                    config.footerFailureViewCreator,
                    R.layout.item_footer_failed
                ) {
                    override fun viewCreator() = { parent: ViewGroup ->
                        super.viewCreator().invoke(parent).apply {
                            if (data.onLoadMoreRetry != null) {
                                setOnClickListener {
                                    data.edgeState set EdgeState.LOADING
                                    data.onLoadMoreRetry?.invoke()
                                }
                            }
                        }
                    }
                })
            }

            EdgeState.SUCCESS -> {
                data.binderCache.isLoadingMore = false
                updateFooterItemViewModel(statePair.currentData,null)
            }

            else -> {}
        }
    }

    fun observeRecyclerViewData(list: MutableList<ItemDefinition>) {
        data.binderCache.latestUpdateType = UpdateType.Data
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recyclerViewData changed")
        }
        val cloneDataList = mutableListOf<ItemDefinition>()
        cloneDataList.addAll(list)
        adapter.updateData({ cloneDataList }, "dataNotifyChanged") {
            data.binderCache.adapterData = adapter.data
        }
    }

    layoutManager = LinearLayoutManager(this.context)

    if (config.loadMoreOnFooter) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val isEnd: Boolean = isToEnd()
                    if (isEnd && !data.binderCache.isLoadingMore) {
                        data.onLoadMore?.invoke()
                    }
                }
            }
        })
    }

    setAdapter(adapter)
    data.binderCache.latestUpdateType?.run {
        data.loadingState.lazyObserve(owner) { state -> observeLoadingState(state) }
        data.edgeState.lazyObserve(owner) { statePair -> observeEdgeState(statePair) }
    } ?: run {
        data.loadingState.observe(owner) { state -> observeLoadingState(state) }
        data.edgeState.observe(owner) { statePair -> observeEdgeState(statePair) }
    }
    data.recyclerViewData.lazyObserve(owner) { list -> observeRecyclerViewData(list) }

    owner.lifecycle.run {
        addObserver(adapter)
    }
}

enum class UpdateType {
    LoadingState,
    EdgeState,
    Data
}

internal open class EdgeItemViewModel(
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

    var loadMoreOnFooter = true
    var footerLoadingViewCreator: ViewCreator? = null
    var footerNoMoreViewCreator: ViewCreator? = null
    var footerFailureViewCreator: ViewCreator? = null
}

class BinderCache {
    var adapterData: MutableList<ItemDefinition>? = null
    var latestUpdateType: UpdateType? = null
    var isLoadingMore = false
}