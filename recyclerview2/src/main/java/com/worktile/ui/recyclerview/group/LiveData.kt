package com.worktile.ui.recyclerview.group

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

class GroupLiveData<T> : MediatorLiveData<T>() {
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        throw RuntimeException("call observe(RecyclerView, LifecycleOwner, Observer)")
    }

    fun observe(
        recyclerView: RecyclerView,
        owner: LifecycleOwner,
        observer: (group: Group, t: T) -> Unit
    ) {
        val group = Group().apply {
            recyclerView.addGroup(this)
        }
        super.observe(owner) {
            observer(group, it)
        }
    }
}

fun <T> LiveData<T>.toMutableGroup() = GroupLiveData<T>().apply {
    addSource(this@toMutableGroup) {
        postValue(it)
    }
}

fun <T> LiveData<T>.toGroup(): LiveData<T> = toMutableGroup()