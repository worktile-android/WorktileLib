package com.worktile.ui.recyclerview

import com.worktile.ui.recyclerview.livedata.RecyclerViewData

class ItemGroup(
    items: List<ItemDefinition>
) {
    internal var recyclerViewData: RecyclerViewData? = null
    private var _title: ItemDefinition? = null
    val title get() = _title
    internal val items = mutableListOf<ItemDefinition>().apply {
        addAll(items)
    }
    private var isOpen = true
    private val cacheItems = mutableListOf<ItemDefinition>().apply {
        addAll(items)
    }

    fun setTitle(title: ItemDefinition) {
        _title = title
        recyclerViewData?.resetValueByGroups()
    }

    fun setItems(items: List<ItemDefinition>) {
        this.items.apply {
            clear()
            addAll(items)
        }
        recyclerViewData?.resetValueByGroups()
    }

    fun openOrClose() {
        if (isOpen) {
            close()
        } else {
            open()
        }
    }

    fun open() {
        if (isOpen) return
        items.apply {
            clear()
            addAll(cacheItems)
        }
        recyclerViewData?.resetValueByGroups()
        isOpen = true
    }

    fun close() {
        if (!isOpen) return
        cacheItems.apply {
            clear()
            addAll(items)
        }
        items.clear()
        recyclerViewData?.resetValueByGroups()
        isOpen = false
    }
}