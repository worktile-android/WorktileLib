package com.worktile.ui.recyclerview.viewmodels

import androidx.lifecycle.MutableLiveData
import com.worktile.base.arch.viewmodel.Default
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.LoadingState

interface RecyclerViewViewModel {
    val recyclerViewData: MutableLiveData<MutableList<Definition>>
    val loadingState: MutableLiveData<LoadingState>

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = MutableLiveData<MutableList<Definition>>(mutableListOf())
            override val loadingState = MutableLiveData(LoadingState.EMPTY)
        }
    }
}