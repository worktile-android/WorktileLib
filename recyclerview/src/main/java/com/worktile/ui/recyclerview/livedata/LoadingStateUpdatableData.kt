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

    override fun update(value: LoadingState) {
        when (value) {
            LoadingState.LOADING -> {
                Log.e("common", "LoadingStateUpdatableData update loading loading")
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(mutableListOf(loadingItemViewModel))
                )
            }
            LoadingState.EMPTY -> {
                Log.e("common", "LoadingStateUpdatableData update loading empty")
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(mutableListOf(emptyItemViewModel))
                )
            }
            LoadingState.FAILED -> {
                Log.e("common", "LoadingStateUpdatableData update loading failed")
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(mutableListOf(failureItemViewModel))
                )
            }
            LoadingState.SUCCESS -> {
                Log.e("common", "LoadingStateUpdatableData update loading success")
                defaultViewModel.adapterData.internalPostValue(
                    AdapterLiveDataValue(mutableListOf())
                )
            }
            else -> {}
        }
    }
}