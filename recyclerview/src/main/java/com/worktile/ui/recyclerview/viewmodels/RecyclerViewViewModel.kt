package com.worktile.ui.recyclerview.viewmodels

import androidx.lifecycle.MutableLiveData
import com.worktile.base.arch.viewmodel.Default
import com.worktile.ui.recyclerview.ItemData

interface RecyclerViewViewModel {
    val recyclerViewData: MutableLiveData<MutableList<ItemData<*>>>

    companion object {
        @Default
        fun default() = object : RecyclerViewViewModel {
            override val recyclerViewData = MutableLiveData<MutableList<ItemData<*>>>()
        }
    }
}