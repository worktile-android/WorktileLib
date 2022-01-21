package com.worktile.common

import kotlin.collections.HashMap

class CopyOnWriteHashMap<K, V> : HashMap<K, V?>() {
    @Volatile
    private var internalMap: HashMap<K, V?>

    init {
        internalMap = HashMap()
    }

    override fun put(key: K, value: V?): V? {
        synchronized(this) {
            val newMap = HashMap(internalMap)
            val `val` = newMap.put(key, value)
            internalMap = newMap
            return `val`
        }
    }

    override fun putAll(from: Map<out K, V?>) {
        synchronized(this) {
            val newMap = HashMap(internalMap)
            newMap.putAll(from)
            internalMap = newMap
        }
    }

    override operator fun get(key: K): V? {
        return internalMap[key]
    }

    override val size: Int
        get() = internalMap.size

    override fun clear() {
        internalMap.clear()
    }
}