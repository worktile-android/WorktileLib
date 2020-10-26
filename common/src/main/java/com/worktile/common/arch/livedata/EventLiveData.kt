package com.worktile.common.arch.livedata

import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class EventLiveData : LiveData<EventLiveData.Event>() {
    class Event
    private val foreverObservers = mutableListOf<Observer<Event>>()
    private var updateIndex = -1
    private var observeIndex = -1

    fun update() {
        updateIndex++
        if (Thread.currentThread().id == Looper.getMainLooper().thread.id) {
            value = Event()
        } else {
            postValue(Event())
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
        observeForever(Observer<Event> {
            observer.invoke()
        }.apply { foreverObservers.add(this) })
    }

    fun clearForeverObservers() {
        foreverObservers.forEach {
            removeObserver(it)
        }
    }
}