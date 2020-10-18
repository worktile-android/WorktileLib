package com.worktile.common.arch.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

/**
 * Created by Android Studio.
 * User: HuBo
 * Email: hubozzz@163.com
 * Date: 2020/9/23
 * Time: 2:25 PM
 * Desc:
 */

fun <T> MutableLiveData<MutableList<T>>.notifyChanged() {
    postValue(value)
}

fun <T> MutableLiveData<T>.lazyObserve(owner: LifecycleOwner, block: (value: T) ->Unit) {
    observe(owner, object : LazyObserver<T>() {
        override fun onLazyChanged(value: T) {
            block.invoke(value)
        }
    })
}