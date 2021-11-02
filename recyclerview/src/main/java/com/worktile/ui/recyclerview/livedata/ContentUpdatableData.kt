package com.worktile.ui.recyclerview.livedata

import com.worktile.ui.recyclerview.EdgeState
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.ItemGroup
import com.worktile.ui.recyclerview.binder.Config
import com.worktile.ui.recyclerview.livedata.extension.notifyChanged
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

typealias RecyclerViewData = ContentUpdatableData

class ContentUpdatableData(
    internal val defaultViewModel: RecyclerViewViewModel
) : UpdatableData<MutableList<ItemDefinition>>() {
    val value: MutableList<ItemDefinition> = mutableListOf()
    internal val groups = mutableListOf<ItemGroup>()

    override fun onFirstInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    override fun onEachInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    fun update(value: MutableList<ItemDefinition>, keepEdgeState: Boolean = false) {
        defaultViewModel.adapterData.apply {
            if (!keepEdgeState) {
                postPendingEdgeStateValue()
            } else {
                defaultViewModel.edgeState.run {
                    val edgeItem = when (state) {
                        EdgeState.LOADING -> loadingItemViewViewModel
                        EdgeState.NO_MORE -> noMoreItemViewModel
                        EdgeState.FAILED -> failItemViewModel
                        else -> null
                    }
                    if (edgeItem != null) {
                        value.add(edgeItem)
                    }
                }
            }
            internalPostValue(AdapterLiveDataValue(value))
        }
    }

    fun addGroups(vararg group: ItemGroup) {
        if (value.isNotEmpty()) {
            groups.add(ItemGroup(mutableListOf<ItemDefinition>().apply { addAll(value) }))
        }
        group.forEach {
            it.recyclerViewData = this
        }
        groups.addAll(group)
        resetValueByGroups()
    }

    fun clearGroups() {
        groups.clear()
    }

    fun resetValueByGroups() {
        synchronized(value) {
            value.clear()
            groups.forEach {
                it.title?.apply { value.add(this) }
                value.addAll(it.items)
            }
        }
        notifyChanged()
    }
}