package com.worktile.ui.recyclerview.utils.livedata

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.viewmodels.data.EdgeStatePair
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

infix fun MutableLiveData<EdgeStatePair>.set(state: EdgeState) {
    val currentValue = value ?: return
    GlobalScope.launch {
        postValue(EdgeStatePair(state, currentValue.viewModel, mutableListOf<ItemDefinition>().apply {
            addAll(currentValue.viewModel.recyclerViewData.value ?: emptyList())
        }))
    }
}

infix fun MutableLiveData<LoadingState>.set(state: LoadingState) {
    if (Thread.currentThread().id == Looper.getMainLooper().thread.id) {
        value = state
    } else {
        postValue(state)
    }
}