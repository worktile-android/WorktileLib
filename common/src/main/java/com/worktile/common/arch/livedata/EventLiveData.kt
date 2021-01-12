package com.worktile.common.arch.livedata

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * SingleLiveEvent.java
 * https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java
 */
class EventLiveData<T> : LiveData<T>() {

    private val foreverObservers = mutableListOf<Observer<T>>()
    private var updateIndex = -1
    private var observeIndex = -1

    fun update(value: T? = null) {
        updateIndex++
        if (Thread.currentThread().id == Looper.getMainLooper().thread.id) {
            this.value = value
        } else {
            postValue(value)
        }
    }

    fun observe(owner: LifecycleOwner, observer: () -> Unit) {
        observe(owner, Observer {
            if (observeIndex >= updateIndex) {
                return@Observer
            }
            observeIndex = updateIndex
            observer.invoke()
        })
    }

    fun observeForever(observer: () -> Unit) {
        observeForever(Observer<T> {
            observer.invoke()
        }.apply { foreverObservers.add(this) })
    }

    fun clearForeverObservers() {
        foreverObservers.forEach {
            removeObserver(it)
        }
    }
}