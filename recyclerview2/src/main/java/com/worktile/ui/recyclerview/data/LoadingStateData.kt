package com.worktile.ui.recyclerview.data

import android.view.LayoutInflater
import android.view.ViewGroup
import com.worktile.ui.recyclerview.Config
import com.worktile.ui.recyclerview.LoadingStateItemViewModel
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.RecyclerViewViewModel
import com.worktile.ui.recyclerview.databinding.ItemEmptyBinding

class LoadingStateData {
    internal var state: LoadingState = LoadingState.INIT
    internal var config: Config? = null
    internal var viewModel: RecyclerViewViewModel? = null

    private val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
        override fun viewCreator() = config?.loadingViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
        }
    }

    internal val emptyItemViewModel = object : LoadingStateItemViewModel(LoadingState.EMPTY) {
        override fun viewCreator() = config?.emptyViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_empty, parent, false)
        }
    }

    internal val failureItemViewModel = object : LoadingStateItemViewModel(LoadingState.FAILED) {
        override fun viewCreator() = config?.failureViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_empty, parent, false)
                .apply {
                    val itemBinding = ItemEmptyBinding.bind(this)
                    itemBinding.emptyHint.text = context.getString(R.string.retry_hint)
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

    infix fun set(newState: LoadingState) {

    }
}