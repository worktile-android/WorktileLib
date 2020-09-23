package com.worktile.ui.recyclerview.binder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

internal fun <T> SimpleRecyclerView.bind(
    data: MutableLiveData<MutableList<T>>,
    owner: LifecycleOwner,
    itemViewCreator: (type: Any) -> View?
) where T : ItemViewModel, T : ItemBinder {
    data.observe(owner) {
        var adapter: SimpleAdapter<T>? = null
        val cloneDataList = mutableListOf<T>()
        cloneDataList.addAll(it)
        adapter?.updateData(cloneDataList) ?: run {
            adapter = SimpleAdapter(cloneDataList, itemViewCreator)
            setAdapter(adapter)
        }
    }
}

fun SimpleRecyclerView.bind(
    data: RecyclerViewViewModel,
    owner: LifecycleOwner,
    itemViewCreator: (type: Any) -> View?
) {
    this.bind(data.recyclerViewData, owner, itemViewCreator)
}