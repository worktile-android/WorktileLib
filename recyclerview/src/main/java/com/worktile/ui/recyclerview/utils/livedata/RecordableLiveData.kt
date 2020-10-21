package com.worktile.ui.recyclerview.utils.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.worktile.common.arch.livedata.LazyObserver

open class RecordableLiveData<T> : LiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    private var updated = false

    internal fun internalSet(value: T?) {
        setValue(value)
    }

    internal fun internalPost(value: T?) {
        postValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (!updated) {
            super.observe(owner) { t ->
                updated = true
                observer.onChanged(t)
            }
        } else {
            super.observe(owner, object : LazyObserver<T>() {
                override fun onLazyChanged(value: T) {
                    updated = true
                    observer.onChanged(value)
                }
            })
        }
    }

    fun clearUpdateRecord() {
        updated = false
    }
}

class LazyActiveLiveData<T>(value: T) : RecordableLiveData<T>(value) {
    internal var active = false
}



