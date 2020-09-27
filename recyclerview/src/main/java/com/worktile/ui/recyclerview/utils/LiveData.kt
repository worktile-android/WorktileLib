package com.worktile.ui.recyclerview.utils

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.viewmodels.EdgeStatePair
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

infix fun MutableLiveData<EdgeStatePair>.set(state: EdgeState) {
    val currentValue = value ?: return
    GlobalScope.launch {
        postValue(EdgeStatePair(state, currentValue.viewModel, mutableListOf<Definition>().apply {
            addAll(currentValue.viewModel.recyclerViewData.value ?: emptyList())
        }))
    }
}