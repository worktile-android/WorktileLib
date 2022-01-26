package com.worktile.ui.recyclerview.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.Config
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.ViewCreator
import com.worktile.ui.recyclerview.setEdgeState

internal open class EdgeItemDefinition(
    key: EdgeState,
    private val creator: () -> ViewCreator?,
    private val layoutId: Int
) : LoadingStateItemDefinition(key) {
    override fun viewCreator() = creator.invoke() ?: {
        LayoutInflater.from(it.context).inflate(layoutId, it, false)
    }
}

class EdgeStateData(
    recyclerView: RecyclerView,
    config: Config,
    onEdgeLoadMoreRetry: (() -> Unit)? = null
) {
    internal val loadingItemViewViewModel = EdgeItemDefinition(
        EdgeState.LOADING,
        {
            config.run {
                if (loadMoreOnFooter) {
                    footerLoadingViewCreator
                } else {
                    headerLoadingViewCreator
                }
            }
        },
        R.layout.item_footer_loading
    )

    internal val noMoreItemViewModel = EdgeItemDefinition(
        EdgeState.NO_MORE,
        {
            config.run {
                if (loadMoreOnFooter) {
                    footerNoMoreViewCreator
                } else {
                    headerNoMoreViewCreator
                }
            }
        },
        R.layout.item_footer_no_more
    )

    internal val failItemViewModel = object : EdgeItemDefinition(
        EdgeState.FAILED,
        {
            config.run {
                if (loadMoreOnFooter) {
                    footerFailureViewCreator
                } else {
                    headerFailureViewCreator
                }
            }
        },
        R.layout.item_footer_failed
    ) {
        override fun viewCreator() = { parent: ViewGroup ->
            super.viewCreator().invoke(parent).apply {
                setOnClickListener {
                    if (onEdgeLoadMoreRetry != null) {
                        recyclerView.setEdgeState(EdgeState.LOADING)
                        onEdgeLoadMoreRetry.invoke()
                    }
                }
            }
        }
    }
}