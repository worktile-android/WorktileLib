package com.worktile.ui.recyclerview.data

import androidx.lifecycle.LiveData
import com.worktile.ui.recyclerview.InnerViewModel
import com.worktile.ui.recyclerview.ItemDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RecyclerViewData : ArrayList<ItemDefinition>() {
    internal var innerViewModel: InnerViewModel? = null

    @Synchronized
    fun notifyChanged() {
        innerViewModel?.adapterData?.value = mutableListOf<ItemDefinition>().apply {
            addAll(this@RecyclerViewData)
        }
    }
}