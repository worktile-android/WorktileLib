@file:Suppress("UNCHECKED_CAST")

package com.worktile.json

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

const val TAG = "JsonDsl"

fun Any.parse(block: Parser.() -> Unit) {
    val parserData = JsonDsl.jsonMap[this]
    parserData?.run {
        block.invoke(Parser(parserData))
    } ?: throw Exception("没有找到json, $this")
}

internal fun KProperty<*>.actualClass(): KClass<*>? {
    return returnType.classifier as? KClass<*>
}

open class Parser(val data: ParserData) {

    infix fun <T> String.into(property: KMutableProperty0<T>): IntoResult<T?> {
        val tkClass = property.actualClass() ?: run {
            Log.w(TAG, "找不到属性${property.name}的类型")
            return IntoResult()
        }
        val value = data.jsonObject.opt(this)
        value?.run {
            when (value) {
                is JSONObject -> {
                    val result = data.jsonDsl.parse(value, tkClass) as T
                    property.set(result)
                    return IntoResult(result)
                }
                is JSONArray -> {
                    if (tkClass.isSubclassOf(List::class)) {
                        val argumentType = property.returnType.arguments[0]
                        (argumentType.type?.classifier as? KClass<*>)?.apply {
                            val list = mutableListOf<Any>()
                            for (index in 0 until value.length()) {
                                val itemJson = value[index] as JSONObject
                                list.add(data.jsonDsl.parse(itemJson, this))
                            }
                            property.set(list as T)
                            return IntoResult(list)
                        } ?: Log.w(TAG, "找不到泛型类型")
                    } else {
                        Log.w(
                            TAG,
                            "key ${this@into}对应的值是List类型，但${property}不是。jsonObject: ${data.jsonObject}"
                        )
                    }
                }
                is Number -> {
                    when {
                        tkClass.isSubclassOf(Number::class) -> {
                            val numberResult = when (tkClass) {
                                Int::class -> value.toInt()
                                Double::class -> value.toDouble()
                                Long::class -> value.toLong()
                                Float::class -> value.toFloat()
                                Short::class -> value.toShort()
                                Char::class -> value.toChar()
                                Byte::class -> value.toByte()
                                else -> 0
                            }
                            property.set(numberResult as T)
                            return IntoResult(numberResult)
                        }
                        tkClass == Any::class -> {
                            property.set(value as T)
                            return IntoResult(value)
                        }
                        else -> {
                            Log.w(TAG, "需要一个Number类型属性，但${property}不是")
                        }
                    }
                }
                is String -> {
                    if (String::class == tkClass || tkClass == Any::class) {
                        property.set(value as T)
                        return IntoResult(value)
                    } else {
                        Log.w(TAG, "需要一个String类型属性，但${property}不是")
                    }
                }
                is Boolean -> {
                    if (Boolean::class == tkClass || tkClass == Any::class) {
                        property.set(value as T)
                        return IntoResult(value)
                    } else {
                        Log.w(TAG, "需要一个Boolean类型属性，但${property}不是")
                    }
                }
                JSONObject.NULL -> {
                }
                else -> {
                    Log.w(
                        TAG,
                        "无法解析，key：${this@into}, value=${value::class} jsonObject: ${data.jsonObject}"
                    )
                }
            }
        } ?: Log.w(TAG, "key \"${this}\"不存在于${data.jsonObject}中")
        return IntoResult()
    }

    inner class IntoResult<T>(val propertyValue: T? = null)

    infix fun <T> IntoResult<T>.attach(block: Parser.(t: T?) -> Unit) {
        block.invoke(this@Parser, this.propertyValue)
    }

    infix fun String.then(block: Parser.() -> Unit): ThenResult {
        val thenObject = data.jsonObject.opt(this@then)
        thenObject?.run {
            if (thenObject is JSONObject) {
                block(Parser(ParserData(data.jsonDsl, thenObject, this@then)))
            } else {
                Log.w(TAG, "key \"${this@then}\"对应的值不是JSONObject，无法执行then方法")
            }
        } ?: Log.w(TAG, "key \"${this@then}\"不存在于${data.jsonObject}中")
        return ThenResult(this)
    }

    inner class ThenResult(val key: String)

    infix fun <T> ThenResult.into(property: KMutableProperty0<T>): IntoResult<T?> {
        return key.into(property)
    }

    infix fun String.alter(key: String): AlterResult {
        val alterResult = AlterResult()
        alterResult.alterKeys.run {
            add(this@alter)
            add(key)
        }
        return alterResult
    }

    inner class AlterResult {
        val alterKeys = mutableListOf<String>()
    }

    infix fun AlterResult.alter(key: String): AlterResult {
        alterKeys.add(key)
        return this
    }

    infix fun <T> AlterResult.into(property: KMutableProperty0<T>): IntoResult<T?> {
        alterKeys.forEach {
            if (data.jsonObject.has(it)) {
                return it.into(property)
            }
        }
        return IntoResult()
    }

    inline infix fun <reified T> String.parse(block: Parser.(t: T) -> Unit): CustomParseResult<T> {
        val value = T::class.java.newInstance()
        when (val thenObject = data.jsonObject.opt(this)) {
            null -> {
                Log.w(TAG, "key \"${this}\"不存在于${data.jsonObject}中")
            }
            is JSONObject -> {
                block.invoke(Parser(ParserData(data.jsonDsl, thenObject, this)), value)
            }
            else -> {
                Log.w(TAG, "key \"${this}\"对应的值不是JSONObject，无法执行then方法")
            }
        }
        return CustomParseResult(value)
    }

    inner class CustomParseResult<T>(val value: T)

    infix fun <T> CustomParseResult<T>.into(property: KMutableProperty0<in T>): IntoResult<T> {
        property.set(value)
        return IntoResult(value)
    }

    inline fun <reified T> String.parseList(block: Parser.() -> Unit) {

    }
}