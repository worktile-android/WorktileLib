package com.worktile.ui.recyclerview

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.data.EdgeItemViewModel
import com.worktile.ui.recyclerview.data.EdgeState
import com.worktile.ui.recyclerview.data.LoadingState
import com.worktile.ui.recyclerview.decoration.StickHeaderDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val TAG = "RecyclerView"

fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: () -> Unit
) {
    Handler(Looper.getMainLooper()).post(object : Runnable {
        override fun run() {
            if (isAnimating) {
                itemAnimator?.isRunning {
                    Handler(Looper.getMainLooper()).post(this)
                }
                return
            }
            callback.invoke()
        }
    })
}

fun <T> RecyclerView.bind(
    viewModel: T,
    activity: ComponentActivity,
    config: Config = Config()
) where T : RecyclerViewViewModel, T : ViewModel {
    bind(viewModel, activity as LifecycleOwner, config)
}

fun <T> RecyclerView.bind(
    viewModel: T,
    fragment: Fragment,
    config: Config = Config()
) where T : RecyclerViewViewModel, T : ViewModel {
    bind(viewModel, fragment.viewLifecycleOwner, config)
}

@SuppressLint("ClickableViewAccessibility")
private fun <T> RecyclerView.bind(
    viewModel: T,
    lifecycleOwner: LifecycleOwner,
    config: Config
) where T : RecyclerViewViewModel, T : ViewModel {
    val innerViewModel = InnerViewModel(viewModel, config) {
//        CoroutineScope(Dispatchers.Main).launch {
//            for (index in 0 until itemDecorationCount) {
//                if (getItemDecorationAt(index) is StickHeaderDecoration) {
//                    removeItemDecorationAt(index)
//                    val firstViewHolder = findViewHolderForAdapterPosition(0)
//                    if (firstViewHolder == null) {
//
//                    }
//                    break
//                }
//            }
//            addItemDecoration(StickHeaderDecoration(viewModel.recyclerViewData.itemGroups))
//        }
    }

    viewModel.apply {
        loadingState.apply {
            this.config = config
            onLoadFailedRetry = viewModel.onLoadFailedRetry
        }

        footerState.apply {
            this.config = config
            onEdgeLoadMoreRetry = viewModel.onEdgeLoadMoreRetry
        }

        headerState.apply {
            this.config = config
            onEdgeLoadMoreRetry = viewModel.onEdgeLoadMoreRetry
        }
    }

    layoutManager = LinearLayoutManager(context)

    var isDown = true
    var down = 0f
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                down = event.y
            }

            MotionEvent.ACTION_UP -> {
                isDown = down - event.y <= ViewConfiguration.get(context).scaledTouchSlop
            }
        }
        false
    }
    val saveOnScrollListener = getTag(R.id.saved_on_scroll_listener) as? RecyclerView.OnScrollListener
    if (saveOnScrollListener == null) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    var stopScrollDown = !canScrollVertically(1)
                    var stopScrollUp = !canScrollVertically(-1)
                    if (stopScrollDown && stopScrollUp) {
                        stopScrollDown = !isDown
                        stopScrollUp = isDown
                    }
                    val canLoadMoreOnFooter = config.loadMoreOnFooter
                            && stopScrollDown
                            && viewModel.footerState.state == EdgeState.SUCCESS
                    val canLoadMoreOnHeader = config.loadMoreOnHeader
                            && stopScrollUp
                            && viewModel.headerState.state == EdgeState.SUCCESS
                    if (canLoadMoreOnFooter || canLoadMoreOnHeader) {
                        viewModel.onEdgeLoadMore?.invoke()
                    }
                }
            }
        }.apply {
            setTag(R.id.saved_on_scroll_listener, this)
        })
    }

    adapter = SimpleAdapter(
        innerViewModel.adapterData.value,
        config.log
    ).apply adapter@{
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                innerViewModel
                    .adapterData
                    .collect {
                        updateData(it)
                    }
            }
        }
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (itemCount == 1) {
                    if (positionStart == this@adapter.itemCount - 1
                        && data.lastOrNull() is EdgeItemViewModel
                    ) {
                        scrollToPosition(this@adapter.itemCount - 1)
                    }
                    if (positionStart == 0 && data.firstOrNull() is EdgeItemViewModel) {
                        scrollToPosition(0)
                    }
                }
            }
        })
    }
}

internal class InnerViewModel(
    private val viewModel: RecyclerViewViewModel,
    private val config: Config,
    private val onGroupMode: () -> Unit
) : ViewModel() {
    val adapterData by lazy { MutableStateFlow(collectToAdapterData()) }

    init {
        viewModel.apply {
            recyclerViewData.setInnerViewModel(this@InnerViewModel)
            loadingState.setInnerViewModel(this@InnerViewModel)
            footerState.setInnerViewModel(this@InnerViewModel)
            headerState.setInnerViewModel(this@InnerViewModel)
        }
    }

    @Synchronized
    fun collectToAdapterData(): List<ItemDefinition> {
        return AlwaysNotEqualList<ItemDefinition>().apply {
            when (viewModel.loadingState.state) {
                LoadingState.EMPTY -> {
                    add(viewModel.loadingState.emptyItemViewModel)
                }

                LoadingState.LOADING -> {
                    add(viewModel.loadingState.loadingItemViewModel)
                }

                LoadingState.FAILED -> {
                    add(viewModel.loadingState.failureItemViewModel)
                }

                LoadingState.SUCCESS -> {
                    if (config.loadMoreOnHeader) {
                        when (viewModel.headerState.state) {
                            EdgeState.FAILED -> add(viewModel.headerState.failItemViewModel)
                            EdgeState.LOADING -> add(viewModel.headerState.loadingItemViewViewModel)
                            EdgeState.NO_MORE -> add(viewModel.headerState.noMoreItemViewModel)
                            else -> {
                            }
                        }
                    }
                    if (viewModel.recyclerViewData.itemGroups.isNotEmpty()) {
                        onGroupMode()
                        viewModel.recyclerViewData.itemGroups.forEach { itemGroup ->
                            itemGroup.title?.apply { add(this) }
                            if (itemGroup.isOpen) {
                                addAll(itemGroup.items)
                            }
                        }
                    } else {
                        addAll(viewModel.recyclerViewData)
                    }
                    if (config.loadMoreOnFooter) {
                        when (viewModel.footerState.state) {
                            EdgeState.FAILED -> add(viewModel.footerState.failItemViewModel)
                            EdgeState.LOADING -> add(viewModel.footerState.loadingItemViewViewModel)
                            EdgeState.NO_MORE -> add(viewModel.footerState.noMoreItemViewModel)
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateAdapterData() {
        adapterData.value = collectToAdapterData()
    }
}

@Suppress("EqualsOrHashCode")
private class AlwaysNotEqualList<T> : ArrayList<T>() {
    override fun equals(other: Any?): Boolean {
        return false
    }
}

class Config {
    var log = false
    var loadingViewCreator: ViewCreator? = null
    var emptyViewCreator: ViewCreator? = null
    var failureViewCreator: ViewCreator? = null

    var loadMoreOnFooter = true
    var footerLoadingViewCreator: ViewCreator? = null
    var footerNoMoreViewCreator: ViewCreator? = null
    var footerFailureViewCreator: ViewCreator? = null

    var loadMoreOnHeader = false
    var headerLoadingViewCreator: ViewCreator? = null
    var headerNoMoreViewCreator: ViewCreator? = null
    var headerFailureViewCreator: ViewCreator? = null
}

abstract class AdapterUpdateCallback : RecyclerView.AdapterDataObserver() {
    abstract fun onUpdate()

    override fun onChanged() {
        onUpdate()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        onUpdate()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onUpdate()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        onUpdate()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        onUpdate()
    }
}

fun RecyclerView.registerAdapterUpdateCallback(callback: () -> Unit) {
    adapter?.registerAdapterDataObserver(object : AdapterUpdateCallback() {
        override fun onUpdate() {
            callback()
        }
    })
}