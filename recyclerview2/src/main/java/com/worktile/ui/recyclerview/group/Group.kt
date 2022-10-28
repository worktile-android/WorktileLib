@file:Suppress("unused")

package com.worktile.ui.recyclerview.group

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.data
import com.worktile.ui.recyclerview.data.RecyclerViewData
import com.worktile.ui.recyclerview.extensionsPackage
import com.worktile.ui.recyclerview.notifyChanged

class Group {
    val data = mutableListOf<ItemDefinition>()

    private var _isOpen = true
    val isOpen get() = _isOpen

    private var _hasTitle = false
    val hasTitle get() = _hasTitle

    fun setFirstItemAsTitle() {
        _hasTitle = true
    }

    fun unsetFirstItemAsTitle() {
        _hasTitle = false
    }

    fun openOrClose() {
        _isOpen = !_isOpen
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
            data.addAll(group.data)
        } else if (group.hasTitle) {
            group.data.firstOrNull()?.apply {
                data.add(this)
            }
        } else {
            Log.w("wt-recyclerview", "forget call group.setFirstItemAsTitle()?")
            data.addAll(group.data)
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