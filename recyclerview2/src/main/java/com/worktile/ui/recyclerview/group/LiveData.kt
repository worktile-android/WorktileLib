package com.worktile.ui.recyclerview.group

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.extensionsPackage
import com.worktile.ui.recyclerview.viewModel
import java.util.UUID

class GroupLiveData<T>(
    private val groupId: String = UUID.randomUUID().toString()
) : MediatorLiveData<T>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        throw RuntimeException("call observe(RecyclerView, LifecycleOwner, Observer)")
    }

    fun observe(
        recyclerView: RecyclerView,
        owner: LifecycleOwner,
        observer: (group: Group, t: T) -> Unit
    ) {
        val group = Group(recyclerView.viewModel).apply {
            setId(groupId)
            recyclerView.addGroup(this)
        }
        super.observe(owner) {
            observer(group, it)
            recyclerView.apply {
                unObservedGroups.remove(group.id)
                if (unObservedGroups.isEmpty()) {
                    allGroupsFirstObserveCompleted?.invoke()
                    extensionsPackage.allGroupsFirstObserveCompleted = null
                }
            }
        }
    }
}

fun <T> LiveData<T>.toGroup(
    groupId: String = UUID.randomUUID().toString()
) = GroupLiveData<T>(groupId).apply {
    addSource(this@toGroup) {
        postValue(it)
    }
}

data class MultiGroupDataItem(
    val id: String,
    val value: Any
)

data class MultiGroupItem(
    val group: Group,
    val data: Any
)

class MultiGroupLiveData : MediatorLiveData<List<MultiGroupDataItem>>() {
    private val labelId = UUID.randomUUID().toString()
    private val cachedIds = mutableListOf<String>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in List<MultiGroupDataItem>>) {
        throw RuntimeException("call observe(RecyclerView, LifecycleOwner, Observer)")
    }

    fun observe(
        recyclerView: RecyclerView,
        owner: LifecycleOwner,
        observer: (List<MultiGroupItem>) -> Unit
    ) {
        recyclerView.unObservedGroups.add(labelId)
        super.observe(owner) { groupDataList ->
            cachedIds.forEach {
                recyclerView.removeGroup(it)
            }
            cachedIds.clear()
            val groupItems = groupDataList.map { groupData ->
                val groupId = groupData.id
                cachedIds.add(groupId)
                val group = Group(recyclerView.viewModel).apply {
                    setId(groupId)
                    recyclerView.addGroup(this)
                    recyclerView.unObservedGroups.remove(groupId)
                }
                MultiGroupItem(group, groupData.value)
            }
            observer(groupItems)
            recyclerView.apply {
                unObservedGroups.remove(labelId)
                if (unObservedGroups.isEmpty()) {
                    allGroupsFirstObserveCompleted?.invoke()
                    extensionsPackage.allGroupsFirstObserveCompleted = null
                }
            }
        }
    }
}

fun <T : List<MultiGroupDataItem>> LiveData<T>.toMultiGroup() = run {
    MultiGroupLiveData().apply {
        addSource(this@toMultiGroup) {
            postValue(it)
        }
    }
}