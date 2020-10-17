package com.worktile.common.arch.livedata

import androidx.lifecycle.Observer

abstract class LazyObserver<T> : Observer<T> {
    private var initialized = false

    override fun onChanged(t: T) {
        if (!initialized) {
            initialized = true
            return
        }
        onLazyChanged(t)
    }

    abstract fun onLazyChanged(value: T)
}