package com.worktile.ui.recyclerview.utils.livedata.extension

import android.os.Looper
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.utils.livedata.LazyActiveLiveData
import com.worktile.ui.recyclerview.utils.livedata.RecordableLiveData
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStatePair
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun <T> LazyActiveLiveData<MutableList<T>>.notifyChanged() {
    active = true
    if (Thread.currentThread().id == Looper.getMainLooper().thread.id) {
        internalSet(value)
    } else {
        internalPost(value)
    }
}

infix fun RecordableLiveData<EdgeStatePair>.set(state: EdgeState) {
    val currentValue = value ?: return
    GlobalScope.launch {
        internalPost(EdgeStatePair(state, currentValue.viewModel, mutableListOf<ItemDefinition>().apply {
            addAll(currentValue.viewModel.recyclerViewData.value ?: emptyList())
        }))
    }
}

infix fun RecordableLiveData<LoadingState>.set(state: LoadingState) {
    if (Thread.currentThread().id == Looper.getMainLooper().thread.id) {
        internalSet(state)
    } else {
        internalPost(state)
    }
}