package com.worktile.common.arch.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * 如果一个livedata存在于ViewModel中，存在这样一种情况：
 * 当视图被销毁，ViewModel依然存活，接着视图被重新创建，ViewModel重新绑定到视图，就有可能会再次执行liveData的observe
 * 方法（当然这取决于代码是怎么写的），若一旦执行observe方法，Observer onChanged方法就会被调用，于是可能造成界面的重复刷新，
 * 但实际上LiveData的值并没有被改变。这是因为在执行observe之后，会生成新的ObserverWrapper对象，其中的index就会被重置，
 * 而LiveData持有的index不会被重置，ObserverWrapper的index小于LiveData的index，满足了Observer的onChanged被调用的
 * 条件。
 * 为了防止这种情况造成的重复刷新，设计了这样一个类，如果传入的是同一个
 */
open class LazyLiveData<T> : LiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    private var setValueIndex = -1
    private val observerWrapperMap = mutableMapOf<Observer<in T>, ObserverDecoration<T>>()

    @Suppress("RedundantVisibilityModifier")
    protected override fun setValue(value: T) {
        setValueIndex++
        super.setValue(value)
    }

    private fun getObserverDecoration(observer: Observer<in T>): ObserverDecoration<T> {
        return observerWrapperMap.getOrPut(observer) {
            object : ObserverDecoration<T>() {
                override fun onChanged(t: T) {
                    if (index >= setValueIndex) {
                        return
                    }
                    index = setValueIndex
                    observer.onChanged(t)
                }
            }
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, getObserverDecoration(observer))
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(getObserverDecoration(observer))
    }

//    override fun removeObserver(observer: Observer<in T>) {
//        super.removeObserver(observer)
//        observerWrapperMap.remove(observer)
//    }
}

private abstract class ObserverDecoration<T> : Observer<T> {
    var index = -1
}