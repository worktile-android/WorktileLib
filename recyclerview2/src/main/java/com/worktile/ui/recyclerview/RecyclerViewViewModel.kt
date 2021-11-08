package com.worktile.ui.recyclerview

import com.worktile.common.Default
import com.worktile.ui.recyclerview.data.EdgeStateData
import com.worktile.ui.recyclerview.data.LoadingStateData
import com.worktile.ui.recyclerview.data.RecyclerViewData

interface RecyclerViewViewModel {
    val recyclerViewData: RecyclerViewData
    val loadingState: LoadingStateData
    val onLoadFailedRetry: (() -> Unit)?
    val onEdgeLoadMore: (() -> Unit)?
    val onEdgeLoadMoreRetry: (() -> Unit)?
    val footerState: EdgeStateData
    val headerState: EdgeStateData

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = RecyclerViewData()
            override val loadingState = LoadingStateData()
            override val onLoadFailedRetry: (() -> Unit)? = null
            override val onEdgeLoadMore: (() -> Unit)? = null
            override val onEdgeLoadMoreRetry: (() -> Unit)? = null
            override val footerState = EdgeStateData()
            override val headerState = EdgeStateData()
        }
    }
}