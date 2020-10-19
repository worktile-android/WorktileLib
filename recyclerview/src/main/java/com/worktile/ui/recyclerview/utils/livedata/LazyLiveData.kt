package com.worktile.ui.recyclerview.utils.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicInteger

class LazyLiveData<T> : LiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    private var needUpdateCount = AtomicInteger(0)

    internal fun internalPost(value: T?) {
        needUpdateCount.incrementAndGet()
        postValue(value)
    }

    internal fun internalSet(value: T?) {
        needUpdateCount.incrementAndGet()
        setValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) {
            if (needUpdateCount.get() > 0) {
                observer.onChanged(it)
                needUpdateCount.decrementAndGet()
            }
        }
    }
}



