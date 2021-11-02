package com.worktile.ui.recyclerview

import com.worktile.ui.recyclerview.data.LoadingStateData
import com.worktile.ui.recyclerview.data.RecyclerViewData

interface RecyclerViewViewModel {
    val recyclerViewData: RecyclerViewData
    val loadingState: LoadingStateData
    val onLoadFailedRetry: (() -> Unit)?
    val onLoadMore: (() -> Unit)?
    val onLoadMoreRetry: (() -> Unit)?
}