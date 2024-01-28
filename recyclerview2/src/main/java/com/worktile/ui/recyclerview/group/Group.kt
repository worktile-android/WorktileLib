@file:Suppress("unused")

package com.worktile.ui.recyclerview.group

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.data.RecyclerViewData
import com.worktile.ui.recyclerview.extensionsPackage
import java.util.UUID

class Group(viewModel: RecyclerViewViewModel) {
    private var _id = UUID.randomUUID().toString()
    val id get() = _id
    val data = mutableListOf<ItemDefinition>()
    private val groupOpened = viewModel.groupOpened

    val isOpen get() = groupOpened[id] ?: true

    private var _hasTitle = false
    val hasTitle get() = _hasTitle

    fun setFirstItemAsTitle() {
        _hasTitle = true
    }

    fun unsetFirstItemAsTitle() {
        _hasTitle = false
    }

    fun setId(id: String) {
        _id = id
    }

    fun defaultClose() {
        if (groupOpened[id] == null) {
            groupOpened[id] = false
        }
    }

    fun openOrClose() {
        val opened = groupOpened[id] ?: true
        groupOpened[id] = !opened
    }
}

fun RecyclerView.addGroup(group: Group) {
    extensionsPackage.apply {
        groupData.add(group)
        unObservedGroups.add(group.id)
    }
}

fun RecyclerView.removeGroup(group: Group) {
    extensionsPackage.apply {
        groupData.remove(group)
        unObservedGroups.remove(group.id)
    }
}

fun RecyclerView.removeGroup(groupId: String) {
    extensionsPackage.apply {
        val iterator = groupData.listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.id == groupId) {
                iterator.remove()
                unObservedGroups.remove(groupId)
            }
        }
    }
}

fun RecyclerView.clearGroups() {
    extensionsPackage.apply {
        groupData.clear()
        unObservedGroups.clear()
    }
}

fun RecyclerView.hasGroup(): Boolean {
    return extensionsPackage.run {
        groupData.isNotEmpty()
    }
}

fun RecyclerView.setAllGroupsFirstObserveCompleted(
    waitGroupSortedBy: Boolean = false,
    block: () -> Unit
) {
    extensionsPackage.apply {
        this.waitGroupSortedBy = waitGroupSortedBy
        allGroupsFirstObserveCompleted = {
            allGroupsFirstNotify = true
            block()
        }
        invokeAllGroupsFirstObserveCompleted()
    }
}

internal fun RecyclerView.invokeAllGroupsFirstObserveCompleted() {
    extensionsPackage.apply {
        if (unObservedGroups.isEmpty() && (!waitGroupSortedBy || hasGroupSortedBy)) {
            allGroupsFirstObserveCompleted?.invoke()
            allGroupsFirstObserveCompleted = null
        }
    }
}

fun RecyclerView.setAllGroupsFirstUpdated(block: () -> Unit) {
    extensionsPackage.allGroupsFirstUpdated = block
}

fun RecyclerView.setGroupSortedBy(sortedBy: List<String/*groupId*/>) {
    extensionsPackage.apply {
        hasGroupSortedBy = true
        groupSortedBy.apply {
            clear()
            addAll(sortedBy)
        }
        notifyGroupChangedNonFirst()
        invokeAllGroupsFirstObserveCompleted()
    }
}

private fun RecyclerView.collectDataFromGroups() {
    extensionsPackage.apply {
        data.clear()
        groupData.sortBy {
            groupSortedBy.indexOf(it.id)
        }
        groupData.forEach { group ->
            if (group.isOpen) {
                data.addAll(
                    group.data.map {
                        GroupItemDefinition(group.id, it)
                    }
                )
            } else if (group.hasTitle) {
                group.data.firstOrNull()?.apply {
                    data.add(
                        GroupItemDefinition(group.id, this)
                    )
                }
            } else {
                Log.w("wt-recyclerview", "forget call group.setFirstItemAsTitle()?")
                data.addAll(
                    group.data.map {
                        GroupItemDefinition(group.id, it)
                    }
                )
            }
        }
    }
}

fun RecyclerView.notifyGroupChanged(
    callback: (() -> Unit)? = null,
    processAllData: ((allData: RecyclerViewData) -> Unit)? = null
) {
    collectDataFromGroups()
    processAllData?.invoke(data)
    notifyChanged(callback)
}

fun RecyclerView.notifyGroupChanged(
    key: String,
    processAllData: ((allData: RecyclerViewData) -> Unit)? = null
) {
    collectDataFromGroups()
    processAllData?.invoke(data)
    notifyChanged(key)
}

fun RecyclerView.notifyGroupChangedNonFirst() {
    extensionsPackage.apply {
        if (allGroupsFirstNotify) {
            notifyGroupChanged()
        }
    }
}

class GroupItemDefinition(
    private val groupId: String,
    val source: ItemDefinition
) : ItemDefinition by source {
    override fun groupId(): String {
        return groupId
    }
}