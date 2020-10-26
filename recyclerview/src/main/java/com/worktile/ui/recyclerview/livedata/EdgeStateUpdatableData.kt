package com.worktile.ui.recyclerview.livedata

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.worktile.common.arch.livedata.EventLiveData
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.ViewCreator
import com.worktile.ui.recyclerview.binder.Config
import com.worktile.ui.recyclerview.livedata.extension.set
import com.worktile.ui.recyclerview.viewmodels.LoadingStateItemViewModel
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStateData

class EdgeStateUpdatableData(
    internal val defaultViewModel: RecyclerViewViewModel
) : UpdatableData<EdgeStateData>() {
    internal val scrollToEndEventLiveData = EventLiveData()
    private var config: Config? = null

    override fun onFirstInitialization(viewModel: RecyclerViewViewModel, config: Config) {
        this.config = config
    }

    override fun onEachInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    var state = EdgeState.INIT

    private val loadingItemViewViewModel = EdgeItemViewModel(
        EdgeState.LOADING,
        { config?.footerLoadingViewCreator },
        R.layout.item_footer_loading
    )

    private val noMoreItemViewModel = EdgeItemViewModel(
        EdgeState.NO_MORE,
        { config?.footerNoMoreViewCreator },
        R.layout.item_footer_no_more
    )

    private val failItemViewModel = object : EdgeItemViewModel(
        EdgeState.FAILED,
        { config?.footerFailureViewCreator },
        R.layout.item_footer_failed
    ) {
        override fun viewCreator() = { parent: ViewGroup ->
            super.viewCreator().invoke(parent).apply {
                setOnClickListener {
                    viewModel?.apply {
                        if (onLoadMoreRetry != null) {
                            edgeState set EdgeState.LOADING
                            onLoadMoreRetry?.invoke()
                        }
                    }
                }
            }
        }
    }

    override fun update(value: EdgeStateData) {
        state = value.state
        when (value.state) {
            EdgeState.LOADING -> {
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(
                        mutableListOf<ItemDefinition>().apply {
                            addAll(value.currentData)
                            add(loadingItemViewViewModel)
                        }
                    ) {
                        scrollToEndEventLiveData.update()
                    }
                )
            }

            EdgeState.NO_MORE -> {
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(
                        mutableListOf<ItemDefinition>().apply {
                            addAll(value.currentData)
                            add(noMoreItemViewModel)
                        }
                    ) {
                        scrollToEndEventLiveData.update()
                    }
                )
            }

            EdgeState.FAILED -> {
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(
                        mutableListOf<ItemDefinition>().apply {
                            addAll(value.currentData)
                            add(failItemViewModel)
                        }
                    ) {
                        scrollToEndEventLiveData.update()
                    }
                )
            }

            EdgeState.SUCCESS -> {
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(
                        mutableListOf<ItemDefinition>().apply {
                            addAll(value.currentData)
                        }
                    )
                )
            }

            else -> {}
        }
    }

    private open inner class EdgeItemViewModel(
        key: EdgeState,
        private val getCreator: () -> ViewCreator?,
        private val layoutId: Int
    ) : LoadingStateItemViewModel(key) {
        override fun viewCreator() = getCreator.invoke() ?: {
            LayoutInflater.from(it.context).inflate(layoutId, it, false)
        }
    }
}