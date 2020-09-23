package com.worktile.ui.recyclerview.binder

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

internal fun <T> SimpleRecyclerView.bind(
    data: MutableLiveData<MutableList<T>>,
    owner: LifecycleOwner
) where T : ItemViewModel, T : ItemBinder {
    var adapter: SimpleAdapter<T>? = null
    data.observe(owner) {
        val cloneDataList = mutableListOf<T>()
        cloneDataList.addAll(it)
        adapter?.updateData(cloneDataList) ?: run {
            adapter = SimpleAdapter(cloneDataList)
            setAdapter(adapter)
        }
    }
}

fun SimpleRecyclerView.bind(
    data: RecyclerViewViewModel,
    owner: LifecycleOwner
) {
    this.bind(data.recyclerViewData, owner)
}