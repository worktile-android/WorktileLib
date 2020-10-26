package com.worktile.ui.recyclerview.livedata

import android.util.Log
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.binder.Config
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

class ContentUpdatableData(
    internal val defaultViewModel: RecyclerViewViewModel
) : UpdatableData<MutableList<ItemDefinition>>() {
    var value: MutableList<ItemDefinition> = mutableListOf()

    override fun onFirstInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    override fun onEachInitialization(viewModel: RecyclerViewViewModel, config: Config) {

    }

    override fun update(value: MutableList<ItemDefinition>) {
        defaultViewModel.adapterData.internalPostValue(AdapterLiveDataValue(value))
    }
}