package com.worktile.ui.recyclerview.binder

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.ItemData
import com.worktile.ui.recyclerview.ItemViewModel
import com.worktile.ui.recyclerview.SimpleAdapter
import com.worktile.ui.recyclerview.SimpleRecyclerView
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

internal fun <T : ItemViewModel> SimpleRecyclerView.bind(data: MutableLiveData<MutableList<ItemData<out T>>>, owner: LifecycleOwner) {
    data.observe(owner) {
        var adapter: SimpleAdapter<T>? = null
        val cloneDataList = mutableListOf<ItemData<out T>>()
        cloneDataList.addAll(it)
        adapter?.updateData(cloneDataList) ?: run {
            adapter = setData(cloneDataList)
        }
    }
}

fun SimpleRecyclerView.bind(data: RecyclerViewViewModel, owner: LifecycleOwner) {
    this.bind(data.recyclerViewData, owner)
}