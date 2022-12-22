@file:Suppress("unused")

package com.worktile.ui.recyclerview.group

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.data.RecyclerViewData
import com.worktile.ui.recyclerview.extensionsPackage
import java.util.UUID

class Group(viewModel: RecyclerViewViewModel) {
    val id = UUID.randomUUID().toString()
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

    fun openOrClose() {
        val opened = groupOpened[id] ?: true
        groupOpened[id] = !opened
    }
}

internal val RecyclerView.groupData get() = extensionsPackage.groupData

fun RecyclerView.addGroup(group: Group) {
    groupData.add(group)
}

fun RecyclerView.clearGroups() {
    groupData.clear()
}

private fun RecyclerView.collectDataFromGroups() {
    data.clear()
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

class GroupItemDefinition(
    private val groupId: String,
    val source: ItemDefinition
) : ItemDefinition by source {
    override fun groupId(): String {
        return groupId
    }
}