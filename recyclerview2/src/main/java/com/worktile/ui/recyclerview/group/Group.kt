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

internal val RecyclerView.groupData get() = extensionsPackage.groupData
internal val RecyclerView.unObservedGroups get() = extensionsPackage.unObservedGroups
internal val RecyclerView.allGroupsFirstObserveCompleted get() = extensionsPackage.allGroupsFirstObserveCompleted
internal val RecyclerView.allGroupsFirstUpdated get() = extensionsPackage.allGroupsFirstUpdated
internal val RecyclerView.allGroupsFistNotify get() = extensionsPackage.allGroupsFirstNotify
internal val RecyclerView.groupSortedBy get() = extensionsPackage.groupSortedBy

fun RecyclerView.addGroup(group: Group) {
    groupData.add(group)
    unObservedGroups.add(group.id)
}

fun RecyclerView.removeGroup(group: Group) {
    groupData.remove(group)
    unObservedGroups.remove(group.id)
}

fun RecyclerView.removeGroup(groupId: String) {
    val iterator = groupData.listIterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (item.id == groupId) {
            iterator.remove()
            unObservedGroups.remove(groupId)
        }
    }
}

fun RecyclerView.clearGroups() {
    groupData.clear()
    unObservedGroups.clear()
}

fun RecyclerView.hasGroup(): Boolean {
    return groupData.isNotEmpty()
}

fun RecyclerView.setAllGroupsFirstObserveCompleted(block: () -> Unit) {
    extensionsPackage.allGroupsFirstObserveCompleted = {
        extensionsPackage.allGroupsFirstNotify = true
        block()
    }
    if (unObservedGroups.isEmpty()) {
        allGroupsFirstObserveCompleted?.invoke()
        extensionsPackage.allGroupsFirstObserveCompleted = null
    }
}

fun RecyclerView.setAllGroupsFirstUpdated(block: () -> Unit) {
    extensionsPackage.allGroupsFirstUpdated = block
}

fun RecyclerView.setGroupSortedBy(sortedBy: List<String/*groupId*/>) {
    extensionsPackage.groupSortedBy.apply {
        clear()
        addAll(sortedBy)
    }
    notifyGroupChangedNonFirst()
}

private fun RecyclerView.collectDataFromGroups() {
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
    if (allGroupsFistNotify) {
        notifyGroupChanged()
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