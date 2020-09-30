package com.worktile.json

import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JsonDsl {
    companion object {
        internal val jsonMap = ConcurrentHashMap<Any/*object need to be filled*/, Any/*object from json*/>()
    }

    fun parse(json: String, kClass: KClass<*>): Any {
        return kClass.java.newInstance().apply {
            jsonMap[this] = JSONObject(json)
            // 通过Annotation找到方法解析方法
            // 然后调用解析方法
            jsonMap.remove(this)
        }
    }
}