package com.worktile.ui.recyclerview.viewmodels

import androidx.lifecycle.MutableLiveData
import com.worktile.common.arch.viewmodel.Default
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.binder.BinderCache
import com.worktile.ui.recyclerview.utils.livedata.LazyLiveData
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStatePair

interface RecyclerViewViewModel {
    val recyclerViewData: LazyLiveData<MutableList<ItemDefinition>>
    val loadingState: LazyLiveData<LoadingState>
    val edgeState: LazyLiveData<EdgeStatePair>
    val onLoadFailedRetry: (() -> Unit)?
    val onLoadMore: (() -> Unit)?
    val onLoadMoreRetry: (() -> Unit)?
    val binderCache: BinderCache

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = LazyLiveData<MutableList<ItemDefinition>>(mutableListOf())
            override val loadingState = LazyLiveData(LoadingState.INIT)
            override val edgeState = LazyLiveData(EdgeStatePair(EdgeState.INIT, this, recyclerViewData.value ?: mutableListOf()))
            override val onLoadFailedRetry: (() -> Unit)? = null
            override val onLoadMore: (() -> Unit)? = null
            override val onLoadMoreRetry: (() -> Unit)? = null
            override val binderCache = BinderCache()
        }
    }

}