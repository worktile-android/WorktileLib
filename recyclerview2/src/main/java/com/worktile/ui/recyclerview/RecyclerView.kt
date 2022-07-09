package com.worktile.ui.recyclerview

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.data.*
import com.worktile.ui.recyclerview.data.EdgeItemDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

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

private class ExtensionsPackage(val recyclerView: RecyclerView) {
    val recyclerViewData = RecyclerViewData()
    val adapterData = MutableStateFlow<List<ItemDefinition>?>(null)

    var onLoadFailedRetry: (() -> Unit)? = null
    var onEdgeLoadMore: (() -> Unit)? = null
    var onEdgeLoadMoreRetry: (() -> Unit)? = null

    var config: Config = Config()

    @Suppress("PropertyName")
    var _viewModel: RecyclerViewViewModel? = null
    val viewModel: RecyclerViewViewModel get() = _viewModel ?: throw Exception()

    fun collectAdapterData(key: String? = null): List<ItemDefinition> {
        synchronized(recyclerViewData) {
            val loadingStateData = LoadingStateData(recyclerView, config, onLoadFailedRetry)
            return AlwaysNotEqualList<ItemDefinition>(key).apply {
                when (viewModel.states.loadingState) {
                    LoadingState.EMPTY -> {
                        add(loadingStateData.emptyItemViewModel)
                    }

                    LoadingState.LOADING -> {
                        add(loadingStateData.loadingItemViewModel)
                    }

                    LoadingState.FAILED -> {
                        add(loadingStateData.failureItemViewModel)
                    }

                    LoadingState.SUCCESS -> {
                        if (config.loadMoreOnHeader) {
                            val headerStateData = EdgeStateData(
                                recyclerView,
                                config,
                                onEdgeLoadMoreRetry
                            )
                            when (viewModel.states.headerState) {
                                EdgeState.FAILED -> add(headerStateData.failItemViewModel)
                                EdgeState.LOADING -> add(headerStateData.loadingItemViewViewModel)
                                EdgeState.NO_MORE -> add(headerStateData.noMoreItemViewModel)
                                else -> {
                                }
                            }
                        }
                        if (recyclerViewData.itemGroups.isNotEmpty()) {
                            recyclerViewData.itemGroups.forEach { itemGroup ->
                                itemGroup.title?.apply { add(this) }
                                if (itemGroup.isOpen) {
                                    addAll(itemGroup.items)
                                }
                            }
                        } else {
                            addAll(recyclerViewData)
                        }
                        if (config.loadMoreOnFooter) {
                            val footerStateData = EdgeStateData(
                                recyclerView,
                                config,
                                onEdgeLoadMoreRetry
                            )
                            when (viewModel.states.footerState) {
                                EdgeState.FAILED -> add(footerStateData.failItemViewModel)
                                EdgeState.LOADING -> add(footerStateData.loadingItemViewViewModel)
                                EdgeState.NO_MORE -> add(footerStateData.noMoreItemViewModel)
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val RecyclerView.extensionsPackage
    get() = (getTag(R.id.wt_recycler_view_extensions_package) as? ExtensionsPackage)
        ?: run {
            ExtensionsPackage(this).apply {
                setTag(R.id.wt_recycler_view_extensions_package, this)
            }
        }

private val RecyclerView.config get() = extensionsPackage.config
private val RecyclerView.adapterData get() = extensionsPackage.adapterData
private val RecyclerView.viewModel get() = extensionsPackage.viewModel
private val RecyclerView.simpleAdapter get() = adapter as? SimpleAdapter
private fun RecyclerView.collectAdapterData(key: String? = null) =
    extensionsPackage.collectAdapterData(key)

val RecyclerView.data get() = extensionsPackage.recyclerViewData

fun registerUpdateCallback(key: String, callback: () -> Unit) {
    updateCallbacks[key] = callback
}

fun RecyclerView.notifyChanged(key: String) {
    adapterData.value = collectAdapterData(key)
}

fun RecyclerView.notifyChanged(callback: (() -> Unit)? = null) {
    val key = callback?.run {
        val uuid = UUID.randomUUID().toString()
        updateCallbacks[uuid] = this
        uuid
    }
    adapterData.value = collectAdapterData(key)
}

fun RecyclerView.initLoadingState(state: LoadingState = LoadingState.LOADING) {
    if (!viewModel.states.loadingStateInitialized) {
        viewModel.states.loadingStateInitialized = true
        setLoadingState(state)
    }
}

fun RecyclerView.setLoadingState(state: LoadingState) {
    if (viewModel.states.loadingState != state) {
        viewModel.states.loadingState = state
        adapterData.value = collectAdapterData()
    }
}

fun RecyclerView.setEdgeState(state: EdgeState) {
    if (config.loadMoreOnHeader) {
        if (viewModel.states.headerState != state) {
            viewModel.states.headerState = state
            if (state != EdgeState.SUCCESS) {
                adapterData.value = collectAdapterData()
            }
        }
    }

    if (config.loadMoreOnFooter) {
        if (viewModel.states.footerState != state) {
            viewModel.states.footerState = state
            if (state != EdgeState.SUCCESS) {
                adapterData.value = collectAdapterData()
            }
        }
    }
}

fun RecyclerView.setOnLoadFailedRetry(func: () -> Unit) {
    extensionsPackage.onLoadFailedRetry = func
}

fun RecyclerView.setOnEdgeLoadMore(func: () -> Unit) {
    extensionsPackage.onEdgeLoadMore = func
}

fun RecyclerView.setOnEdgeLoadMoreRetry(func: () -> Unit) {
    extensionsPackage.onEdgeLoadMoreRetry = func
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
fun <T> RecyclerView.bind(
    viewModel: T,
    lifecycleOwner: LifecycleOwner,
    config: Config
) where T : RecyclerViewViewModel, T : ViewModel {
    extensionsPackage._viewModel = viewModel
    layoutManager = LinearLayoutManager(context)

    itemAnimator = object : DefaultItemAnimator() {
        override fun onAnimationFinished(viewHolder: RecyclerView.ViewHolder) {
            super.onAnimationFinished(viewHolder)
            (viewHolder as? SimpleAdapter.ItemViewHolder)
                ?.itemData
                ?.itemAnimationFinished(viewHolder.itemView)
        }
    }

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
    val saveOnScrollListener =
        getTag(R.id.saved_on_scroll_listener) as? RecyclerView.OnScrollListener
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
                            && viewModel.states.footerState == EdgeState.SUCCESS
                    val canLoadMoreOnHeader = config.loadMoreOnHeader
                            && stopScrollUp
                            && viewModel.states.headerState == EdgeState.SUCCESS
                    if (canLoadMoreOnFooter || canLoadMoreOnHeader) {
                        extensionsPackage.onEdgeLoadMore?.invoke()
                    }
                }
            }
        }.apply {
            setTag(R.id.saved_on_scroll_listener, this)
        })
    }

    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            adapterData.collect {
                if (it != null) {
                    simpleAdapter?.apply {
                        updateData(it)
                    } ?: run {
                        adapter = SimpleAdapter(it, config.log).apply adapter@{
                            registerAdapterDataObserver(
                                object : RecyclerView.AdapterDataObserver() {
                                    override fun onItemRangeInserted(
                                        positionStart: Int,
                                        itemCount: Int
                                    ) {
                                        super.onItemRangeInserted(positionStart, itemCount)
                                        if (itemCount == 1) {
                                            if (positionStart == this@adapter.itemCount - 1
                                                && data.lastOrNull() is EdgeItemDefinition
                                            ) {
                                                scrollToPosition(this@adapter.itemCount - 1)
                                            }
                                            if (positionStart == 0 &&
                                                    data.firstOrNull() is EdgeItemDefinition) {
                                                scrollToPosition(0)
                                            }
                                        }
                                    }
                                })
                            if (it is AlwaysNotEqualList) {
                                it.key?.apply {
                                    updateCallbacks[this]?.invoke()
                                    updateCallbacks.remove(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("EqualsOrHashCode")
internal class AlwaysNotEqualList<T>(
    val key: String? = null
) : ArrayList<T>() {
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