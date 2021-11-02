package com.worktile.ui.recyclerview.livedata

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.binder.Config
import com.worktile.ui.recyclerview.livedata.extension.set
import com.worktile.ui.recyclerview.viewmodels.LoadingStateItemViewModel
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.item_empty.view.*

class LoadingStateUpdatableData(
    internal val defaultViewModel: RecyclerViewViewModel
) : UpdatableData<LoadingState>() {
    var currentState = LoadingState.INIT
    private var config: Config? = null

    override fun onFirstInitialization(viewModel: RecyclerViewViewModel, config: Config) {
        this.config = config
    }

    override fun onEachInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    internal val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
        override fun viewCreator() = config?.loadingViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
        }
    }

    internal val emptyItemViewModel = object : LoadingStateItemViewModel(LoadingState.EMPTY) {
        override fun viewCreator() = config?.emptyViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
        }
    }

    internal val failureItemViewModel = object : LoadingStateItemViewModel(LoadingState.FAILED) {
        override fun viewCreator() = config?.failureViewCreator ?: { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false).apply {
                empty_hint.text = parent.context.getString(R.string.retry_hint)
                setOnClickListener {
                    viewModel?.run {
                        if (onLoadFailedRetry != null) {
                            loadingState set LoadingState.LOADING
                            onLoadFailedRetry?.invoke()
                        }
                    }
                }
            }
        }
    }

    fun update(value: LoadingState) {
        if (value != currentState) {
            println("rvv, value != currentState")
            when (value) {
                LoadingState.LOADING -> {
                    println("rvv, post loadingState")
                    defaultViewModel.adapterData.internalPostValue(
                        AdapterLiveDataValue(mutableListOf(loadingItemViewModel))
                    )
                }
                LoadingState.EMPTY -> {
                    defaultViewModel.adapterData.internalPostValue(
                        AdapterLiveDataValue(mutableListOf(emptyItemViewModel))
                    )
                }
                LoadingState.FAILED -> {
                    defaultViewModel.adapterData.internalPostValue(
                        AdapterLiveDataValue(mutableListOf(failureItemViewModel))
                    )
                }
                LoadingState.SUCCESS -> {
                    defaultViewModel.adapterData.internalPostValue(
                        AdapterLiveDataValue(mutableListOf())
                    )
                }
                else -> {
                }
            }
            currentState = value
        }
    }
}