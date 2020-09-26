package com.worktile.ui.recyclerview.viewmodels

import androidx.lifecycle.MutableLiveData
import com.worktile.base.arch.viewmodel.Default
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.LoadingState

interface RecyclerViewViewModel {
    val recyclerViewData: MutableLiveData<MutableList<Definition>>
    val loadingState: MutableLiveData<LoadingState>
    val footerState: MutableLiveData<EdgeState>
    val onLoadFailedRetry: (() -> Unit)?
    val onLoadMore: (() -> Unit)?
    val onLoadMoreRetry: (() -> Unit)?

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = MutableLiveData<MutableList<Definition>>(mutableListOf())
            override val loadingState = MutableLiveData(LoadingState.INIT)
            override val footerState = MutableLiveData(EdgeState.INIT)
            override val onLoadFailedRetry: (() -> Unit)? = null
            override val onLoadMore: (() -> Unit)? = null
            override val onLoadMoreRetry: (() -> Unit)? = null
        }
    }
}