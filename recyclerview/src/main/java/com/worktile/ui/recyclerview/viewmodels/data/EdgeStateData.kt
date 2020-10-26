package com.worktile.ui.recyclerview.viewmodels.data

import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

data class EdgeStateData(
    val state: EdgeState,
    val currentData: MutableList<ItemDefinition>
)