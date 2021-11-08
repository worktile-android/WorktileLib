package com.worktile.ui.recyclerview.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.InnerViewModel
import com.worktile.ui.recyclerview.databinding.ItemEmptyBinding

internal abstract class LoadingStateItemViewModel(
    private val key: Any
) : DiffItemViewModel, ItemDefinition {
    override fun key(): Any = key
    override fun type() = key()
    override fun bind(itemView: View) {}
}

class LoadingStateData {
    var state: LoadingState = LoadingState.SUCCESS
        private set
    internal var config: Config? = null
    internal var onLoadFailedRetry: (() -> Unit)? = null

    internal val loadingItemViewModel = object : LoadingStateItemViewModel(LoadingState.LOADING) {
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
                        if (onLoadFailedRetry != null) {
                            set(LoadingState.LOADING)
                            onLoadFailedRetry?.invoke()
                        }
                    }
            }
        }
    }

    private var innerViewModel: InnerViewModel? = null

    internal fun setInnerViewModel(innerViewModel: InnerViewModel) {
        this.innerViewModel = innerViewModel
    }

    infix fun set(newState: LoadingState) {
        if (newState != state) {
            state = newState
            innerViewModel?.updateAdapterData()
        }
    }
}