package com.worktile.ui.recyclerview.utils.livedata

import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.EdgeState
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