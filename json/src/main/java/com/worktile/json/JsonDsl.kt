package com.worktile.json

import android.util.Log
import com.worktile.json.annotation.Deserializer
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JsonDsl {
    companion object {
        const val TAG = "JsonDsl"
        internal val jsonMap = ConcurrentHashMap<Any/*object need to be filled*/, ParserData>()
    }

    fun parse(jsonObject: JSONObject, kClass: KClass<*>): Any {
        return kClass.java.newInstance().apply {
            jsonMap[this] = ParserData(this@JsonDsl, jsonObject)
            var found = false
            kClass.java.methods.forEach methods@ { method ->
                method.annotations.forEach {
                    if (it.javaClass == Deserializer::class.java) {
                        found = true
                        method.invoke(this)
                        return@methods
                    }
                }
            }
            jsonMap.remove(this)
            if (!found) {
                Log.e(TAG, "${kClass}中没有解析方法")
            }
        }
    }

    inline fun <reified T> parse(jsonObject: JSONObject): T {
        return parse(jsonObject, T::class) as T
    }

    inline fun <reified T> parse(json: String): T {
        return parse(JSONObject(json), T::class) as T
    }
}