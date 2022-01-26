package com.worktile.ui.recyclerview.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.databinding.ItemEmptyBinding

internal abstract class LoadingStateItemDefinition(
    private val key: Any
) : ItemDefinition {
    override fun key(): Any = key
    override fun type() = key()
    override fun bind(itemView: View) {}
    override fun content(): Array<ContentItem<*>>? {
        return emptyArray()
    }
}

class LoadingStateData(
    recyclerView: RecyclerView,
    config: Config,
    onLoadFailedRetry: (() -> Unit)? = null
) {
    internal val loadingItemViewModel = object : LoadingStateItemDefinition(LoadingState.LOADING) {
        override fun viewCreator() = config.loadingViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
        }
    }

    internal val emptyItemViewModel = object : LoadingStateItemDefinition(LoadingState.EMPTY) {
        override fun viewCreator() = config.emptyViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_empty, parent, false)
        }
    }

    internal val failureItemViewModel = object : LoadingStateItemDefinition(LoadingState.FAILED) {
        override fun viewCreator() = config.failureViewCreator ?: { parent: ViewGroup ->
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_empty, parent, false)
                .apply {
                    val itemBinding = ItemEmptyBinding.bind(this)
                    itemBinding.emptyHint.text = context.getString(R.string.retry_hint)
                    setOnClickListener {
                        if (onLoadFailedRetry != null) {
                            recyclerView.setLoadingState(LoadingState.LOADING)
                            onLoadFailedRetry.invoke()
                        }
                    }
            }
        }
    }
}