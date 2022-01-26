package com.worktile.ui.recyclerview

import com.worktile.common.Default
import com.worktile.ui.recyclerview.data.EdgeState
import com.worktile.ui.recyclerview.data.LoadingState

class States {
    internal var loadingState: LoadingState = LoadingState.SUCCESS
    internal var footerState: EdgeState = EdgeState.SUCCESS
    internal var headerState: EdgeState = EdgeState.SUCCESS

    internal var loadingStateInitialized = false
}

interface RecyclerViewViewModel {
    val states: States

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val states = States()
        }
    }
}