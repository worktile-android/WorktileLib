package com.worktile.ui.recyclerview

const val ITEM_GROUP_NO_ID = ""

@Deprecated("")
open class ItemGroup(
    items: List<ItemDefinition> = emptyList(),
    val id: String = ITEM_GROUP_NO_ID
) {
    val items = mutableListOf<ItemDefinition>().apply {
        addAll(items)
    }
    private var _isOpen = true
    val isOpen get() = _isOpen
    private var _title: ItemDefinition? = null
    val title get() = _title

    fun setTitle(title: ItemDefinition) {
        _title = title
    }

    fun setItems(items: List<ItemDefinition>) {
        this.items.apply {
            clear()
            addAll(items)
        }
    }

    fun openOrClose() {
        _isOpen = !_isOpen
    }
}