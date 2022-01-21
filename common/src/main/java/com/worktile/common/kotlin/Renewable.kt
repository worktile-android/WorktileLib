package com.worktile.common.kotlin

import java.io.Serializable

fun <T> renewable(initializer: (Renewable<T>) -> T): Lazy<T> {
    return Renewable(initializer)
}

private object EMPTY

@Suppress("UNCHECKED_CAST")
class Renewable<out T>(private val initializer: (Renewable<T>) -> T, lock: Any? = null) : Lazy<T>, Serializable {
    @Volatile private var _value: Any? = EMPTY
    private val lock = lock ?: this

    override val value: T
        get() {
            synchronized(lock) {
                if (_value == EMPTY) {
                    _value = initializer.invoke(this)
                }
                return _value as T
            }
        }

    override fun isInitialized(): Boolean = _value != null

    fun destroy() {
        _value = EMPTY
    }
}