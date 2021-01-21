package com.worktile.json

import android.util.Log
import com.worktile.json.annotation.Deserializer
import com.worktile.json.annotation.Ignore
import com.worktile.json.annotation.SerializedName
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

class JsonDsl(private val autoDeserialize: Boolean = true) {
    companion object {
        const val TAG = "JsonDsl"
        internal val jsonMap = ConcurrentHashMap<Any/*object need to be filled*/, ParserData>()
    }

    fun parse(jsonObject: JSONObject, kClass: KClass<*>): Any {
        val result = if (autoDeserialize) {
            deserialize(kClass, jsonObject)
        } else kClass.java.newInstance()

        val parseMethod: Method? = run {
            kClass.java.methods.forEach methods@{ method ->
                method.annotations.forEach {
                    if (it.annotationClass == Deserializer::class) {
                        return@run method
                    }
                }
            }
            return@run null
        }

        if (parseMethod != null) {
            jsonMap[result] = ParserData(this, jsonObject)
            parseMethod.invoke(result)
            jsonMap.remove(result)
        } else if (!autoDeserialize) {
            Log.e(TAG, "${kClass}中没有解析方法")
        }

        return result
    }

    inline fun <reified T> parse(jsonObject: JSONObject): T {
        return parse(jsonObject, T::class) as T
    }

    inline fun <reified T> parse(json: String): T {
        return parse(JSONObject(json), T::class) as T
    }

    private fun deserialize(
        kClass: KClass<*>,
        jsonObject: JSONObject
    ): Any {
        val params = mutableListOf<Pair<KProperty1<*, *>, Any>>()

        kClass.declaredMemberProperties.forEach { property ->
            val shouldIgnore = property
                .javaField
                ?.annotations
                ?.any {
                    it.annotationClass == Ignore::class
                } == true
            if (shouldIgnore) return@forEach
            val jsonFieldName = getSerializedName(property.javaField?.annotations) ?: property.name
            property.isAccessible = true
            jsonObject.opt(jsonFieldName)?.let {
                when (it) {
                    is JSONObject -> {
                        property.actualClass()?.run {
                            params.add(property to JsonDsl(autoDeserialize).parse(it, this))
                        }
                    }

                    is JSONArray -> {
                        val arrayObj = mutableListOf<Any>()
                        for (index in 0 until it.length()) {
                            val arrayGenericType = property.returnType.arguments[0]
                            (arrayGenericType.type?.classifier as? KClass<*>)?.run {
                                arrayObj.add(
                                    JsonDsl(autoDeserialize).parse(
                                        it.getJSONObject(index),
                                        this
                                    )
                                )
                            } ?: Log.w(TAG, "在${property}中找不到泛型类型")
                        }
                        params.add(property to arrayObj)
                    }

                    is Number -> {
                        val numberValue = when (property.actualClass()) {
                            Int::class -> it.toInt()
                            Double::class -> it.toDouble()
                            Long::class -> it.toLong()
                            Float::class -> it.toFloat()
                            Short::class -> it.toShort()
                            Char::class -> it.toChar()
                            Byte::class -> it.toByte()
                            else -> {
                                Log.w(TAG, "需要一个Number类型属性，但${property.name}不是")
                                0
                            }
                        }
                        params.add(property to numberValue)
                    }

                    is Boolean -> params.add(property to it)

                    is String -> params.add(property to it)

                    JSONObject.NULL -> {
                    }

                    else -> {
                        Log.w(TAG, "无法解析，key：$jsonFieldName, value=$it jsonObject: $jsonObject")
                    }
                }
            }
        }

        val result = kClass.primaryConstructor?.run {
            val args = mutableListOf<Any>()
            parameters.forEach { parameter ->
                params.find { (property, _) ->
                    property.name == parameter.name
                }?.also { (_, value) ->
                    args.add(value)
                }
            }
            try {
                call(*args.toTypedArray())
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "")
                Log.e(TAG, "构造${kClass}对象需要的参数: $parameters, 但只提供了: $args")
            }
        } ?: kClass.java.newInstance()

        params.forEach { (property, value) ->
            @Suppress("UNCHECKED_CAST")
            (property as? KMutableProperty1<Any, Any>)?.set(result, value)
        }

        return result
    }

    private fun getSerializedName(annotation: Array<Annotation>? = emptyArray()): String? {
        val serializedNameAnnotation = annotation?.find {
            it.annotationClass == SerializedName::class
        }
        return serializedNameAnnotation?.let { (it as SerializedName).value }
    }
}