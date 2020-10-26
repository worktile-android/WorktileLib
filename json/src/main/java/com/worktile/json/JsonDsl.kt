package com.worktile.json

import android.util.Log
import com.worktile.json.annotation.Deserializer
import com.worktile.json.annotation.Ignore
import com.worktile.json.annotation.SerializedName
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JsonDsl(private val deserialize: Boolean = true) {
    companion object {
        const val TAG = "JsonDsl"
        internal val jsonMap = ConcurrentHashMap<Any/*object need to be filled*/, ParserData>()
    }

    fun parse(jsonObject: JSONObject, kClass: KClass<*>): Any {
        return kClass.java.newInstance().apply {
            jsonMap[this] = ParserData(this@JsonDsl, jsonObject)
            if (deserialize) {
                deserialize(this, kClass, jsonObject)
            }
            var found = false
            kClass.java.methods.forEach methods@{ method ->
                method.annotations.forEach {
                    if (it.annotationClass == Deserializer::class) {
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

    private fun deserialize(
        context: Any,
        kClass: KClass<*>,
        jsonObject: JSONObject
    ) {
        val classFields = kClass.java.declaredFields
        for (field in classFields) {
            if (field.annotations.any { it.annotationClass == Ignore::class }) continue
            val jsonFieldName = getSerializedName(field.annotations) ?: field.name
            field.isAccessible = true
            jsonObject.opt(jsonFieldName)?.let {
                when (it) {
                    is JSONObject -> {
                        field.set(context, JsonDsl(deserialize).parse(it, field.type.kotlin))
                    }
                    is JSONArray -> {
                        val arrayObj = mutableListOf<Any>()
                        for (index in 0 until it.length()) {
                            val arrayGenericType =
                                ((field.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)
                            arrayObj.add(
                                JsonDsl(deserialize).parse(
                                    it.getJSONObject(index),
                                    arrayGenericType.kotlin
                                )
                            )
                        }
                        field.set(context, arrayObj)
                    }
                    is String -> field.set(context, it)
                    is Number -> {
                        val numberValue = when (field.type) {
                            Int::class.javaObjectType -> it.toInt()
                            Double::class.javaObjectType -> it.toDouble()
                            Long::class.javaObjectType -> it.toLong()
                            Float::class.javaObjectType -> it.toFloat()
                            Short::class.javaObjectType -> it.toShort()
                            Char::class.javaObjectType -> it.toChar()
                            Byte::class.javaObjectType -> it.toByte()
                            else -> {
                                Log.w(TAG, "需要一个Number类型属性，但${field.name}不是")
                                0
                            }
                        }
                        field.set(context, numberValue)
                    }
                    is Boolean -> field.set(context, it)
                    JSONObject.NULL -> {
                    }
                    else -> {
                        Log.w(TAG, "无法解析，key：$jsonFieldName, value=$it jsonObject: $jsonObject")
                    }
                }
            }
        }
    }

    private fun getSerializedName(annotation: Array<Annotation>): String? {
        val serializedNameAnnotation =
            annotation.find { it.annotationClass == SerializedName::class }
        return serializedNameAnnotation?.let { (it as SerializedName).value }
    }
}