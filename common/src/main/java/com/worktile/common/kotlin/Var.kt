package com.worktile.common.kotlin

import java.util.concurrent.locks.ReentrantLock

class Var<T>(initialValue: T? = null) {
    @Volatile
    private var _value: T? = initialValue
    private val lock = ReentrantLock()

    val value: T?
        get() = _value

    fun setValue(value: T?) {
        lock.lock()
        _value = value
        lock.unlock()
    }
}