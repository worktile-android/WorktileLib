package com.worktile.ui.recyclerview.livedata.extension

import android.util.Log
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.livedata.*
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStateData
import kotlinx.coroutines.*

infix fun LoadingStateUpdatableData.set(state: LoadingState) {
    synchronized(defaultViewModel) {
        runBlocking {
            withContext(Dispatchers.Default) {
                (viewModel?.recyclerViewData?.value ?: emptyList()).also {
                    synchronized(it) {
                        println("rvv, update(state)")
                        update(state)
                    }
                }
            }
        }
    }
}

infix fun EdgeStateUpdatableData.set(state: EdgeState) {
    synchronized(defaultViewModel) {
        runBlocking {
            withContext(Dispatchers.Default) {
                (viewModel?.recyclerViewData?.value ?: emptyList()).also {
                    synchronized(it) {
                        update(EdgeStateData(
                            state,
                            mutableListOf<ItemDefinition>().apply {
                                addAll(it)
                            }
                        ))
                    }
                }
            }
        }
    }
}

fun ContentUpdatableData.notifyChanged(keepEdgeState: Boolean = false) {
    synchronized(defaultViewModel) {
        runBlocking {
            withContext(Dispatchers.Default) {
                value.also {
                    synchronized(it) {
                        update(mutableListOf<ItemDefinition>().apply {
                            addAll(it)
                        }, keepEdgeState)
                    }
                }
            }
        }
    }
}