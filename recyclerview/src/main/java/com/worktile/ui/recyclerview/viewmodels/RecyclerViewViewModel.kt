package com.worktile.ui.recyclerview.viewmodels

import com.worktile.common.Default
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.livedata.*
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStateData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface RecyclerViewViewModel {
    val recyclerViewData: ContentUpdatableData
    val loadingState: LoadingStateUpdatableData
    val edgeState: EdgeStateUpdatableData
    val onLoadFailedRetry: (() -> Unit)?
    val onLoadMore: (() -> Unit)?
    val onLoadMoreRetry: (() -> Unit)?

    val adapterData: AdapterLiveData

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = ContentUpdatableData(this)
            override val loadingState = LoadingStateUpdatableData(this)
            override val edgeState = EdgeStateUpdatableData(this)
            override val onLoadFailedRetry: (() -> Unit)? = null
            override val onLoadMore: (() -> Unit)? = null
            override val onLoadMoreRetry: (() -> Unit)? = null
            override val adapterData = AdapterLiveData(AdapterLiveDataValue(listOf()))
        }
    }

}