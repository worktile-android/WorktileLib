package com.worktile.ui.recyclerview.livedata

import com.worktile.ui.recyclerview.binder.Config
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel

abstract class UpdatableData<T> {
    internal var viewModel: RecyclerViewViewModel? = null
    private var initialized = false

    internal fun init(
        viewModel: RecyclerViewViewModel,
        config: Config
    ) {
        this.viewModel = viewModel
        if (!initialized) {
            onFirstInitialization(viewModel, config)
            initialized = true
        }
        onEachInitialization(viewModel, config)
    }

    internal abstract fun onFirstInitialization(viewModel: RecyclerViewViewModel, config: Config)
    internal abstract fun onEachInitialization(viewModel: RecyclerViewViewModel, config: Config)
}

