package com.worktile.ui.recyclerview.data

import androidx.lifecycle.LiveData
import com.worktile.ui.recyclerview.InnerViewModel
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.ItemGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class RecyclerViewData : ArrayList<ItemDefinition>() {
    val itemGroups = mutableListOf<ItemGroup>()
    private var innerViewModel: InnerViewModel? = null

    internal fun setInnerViewModel(innerViewModel: InnerViewModel) {
        this.innerViewModel = innerViewModel
    }

    fun notifyChanged() {
        innerViewModel?.updateAdapterData()
    }
}