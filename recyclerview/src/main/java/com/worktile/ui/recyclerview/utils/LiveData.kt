package com.worktile.ui.recyclerview.utils

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.viewmodels.EdgeStatePair

infix fun MutableLiveData<EdgeStatePair>.set(state: EdgeState) {
    val currentValue = value ?: return
    val clonedCurrentData = mutableListOf<Definition>().apply {
        addAll(currentValue.viewModel.recyclerViewData.value ?: emptyList())
    }
    if (Looper.getMainLooper().thread.id == Thread.currentThread().id) {
        value = EdgeStatePair(state, currentValue.viewModel, clonedCurrentData)
    } else {
        postValue(EdgeStatePair(state, currentValue.viewModel, clonedCurrentData))
    }
}