package com.worktile.ui.recyclerview.data

import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.ItemGroup

class RecyclerViewData : ArrayList<ItemDefinition>() {
    @Deprecated("")
    val itemGroups = mutableListOf<ItemGroup>()

    override fun clear() {
        super.clear()
        itemGroups.clear()
    }
}