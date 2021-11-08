package com.worktile.ui.recyclerview.data

import android.view.LayoutInflater
import android.view.ViewGroup
import com.worktile.ui.recyclerview.Config
import com.worktile.ui.recyclerview.InnerViewModel
import com.worktile.ui.recyclerview.R
import com.worktile.ui.recyclerview.ViewCreator

internal open class EdgeItemViewModel(
    key: EdgeState,
    private val creator: () -> ViewCreator?,
    private val layoutId: Int
) : LoadingStateItemViewModel(key) {
    override fun viewCreator() = creator.invoke() ?: {
        LayoutInflater.from(it.context).inflate(layoutId, it, false)
    }
}

class EdgeStateData {
    var state: EdgeState = EdgeState.SUCCESS
        private set
    internal var config: Config? = null
    internal var onEdgeLoadMoreRetry: (() -> Unit)? = null

    internal val loadingItemViewViewModel = EdgeItemViewModel(
        EdgeState.LOADING,
        {
            config?.run {
                if (loadMoreOnFooter) {
                    footerLoadingViewCreator
                } else {
                    headerLoadingViewCreator
                }
            }
        },
        R.layout.item_footer_loading
    )

    internal val noMoreItemViewModel = EdgeItemViewModel(
        EdgeState.NO_MORE,
        {
            config?.run {
                if (loadMoreOnFooter) {
                    footerNoMoreViewCreator
                } else {
                    headerNoMoreViewCreator
                }
            }
        },
        R.layout.item_footer_no_more
    )

    internal val failItemViewModel = object : EdgeItemViewModel(
        EdgeState.FAILED,
        {
            config?.run {
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
                        set(EdgeState.LOADING)
                        onEdgeLoadMoreRetry?.invoke()
                    }
                }
            }
        }
    }

    private var innerViewModel: InnerViewModel? = null

    internal fun setInnerViewModel(innerViewModel: InnerViewModel) {
        this.innerViewModel = innerViewModel
    }

    infix fun set(newState: EdgeState) {
        if (newState != state) {
            state = newState
            if (newState != EdgeState.SUCCESS) {
                innerViewModel?.updateAdapterData()
            }
        }
    }
}