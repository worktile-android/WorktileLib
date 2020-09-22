package com.worktile.ui.recyclerview.binder

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.ItemData
import com.worktile.ui.recyclerview.ItemViewModel
import com.worktile.ui.recyclerview.SimpleAdapter
import com.worktile.ui.recyclerview.SimpleRecyclerView

fun <T : ItemViewModel> SimpleRecyclerView.bind(data: MutableLiveData<MutableList<ItemData<out T>>>, owner: LifecycleOwner) {
    data.observe(owner) {
        val cloneDataList = mutableListOf<ItemData<out T>>()
        cloneDataList.addAll(it)
        this.adapter?.let { adapter ->
            if (adapter is SimpleAdapter<*>) {
                (adapter as SimpleAdapter<T>).updateData(cloneDataList)
            }
        } ?: run {
            setData(cloneDataList)
        }
    }
}