package com.worktile.json

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0

const val TAG = "JsonDsl"

fun Any.parse(block: Parser.() -> Unit) {
    val parserData = JsonDsl.jsonMap[this]
    parserData?.run {
        block.invoke(Parser(parserData))
    } ?: throw Exception("没有找到json, $this")
}

open class Parser(val data: ParserData) {

    infix fun parse(block: Parser.() -> Unit) {
        block.invoke(this)
    }

    inline fun <reified T> checkTypeAndSet(
        kClass: KClass<*>,
        property: KMutableProperty0<T>,
        value: Any
    ): IntoResult<T?> {
        if (kClass.java.isAssignableFrom(T::class.java)) {
            property.set(value as T)
            return IntoResult(value)
        } else {
            Log.w(TAG, "需要一个Number类型属性，但${value}不是")
        }
        return IntoResult()
    }

    inline infix fun <reified T> String.into(property: KMutableProperty0<T>): IntoResult<T?> {
        val value = data.jsonObject.opt(this)
        value?.run {
            when (value) {
                is JSONObject -> {
                    val result = data.jsonDsl.parse(value) as T
                    property.set(result)
                    return IntoResult(result)
                }
                is JSONArray -> {
                    if (List::class.java.isAssignableFrom(T::class.java)) {
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
                        Log.w(TAG, "key ${this@into}对应的值是数组类型，但${property}不是。jsonObject: ${data.jsonObject}")
                    }
                }
                is Number -> return checkTypeAndSet(Number::class, property, value)
                is String -> return checkTypeAndSet(String::class, property, value)
                else -> {
                    Log.w(TAG, "无法解析，key：${this@into}, jsonObject: ${data.jsonObject}")
                }
            }
        } ?: Log.w(TAG, "key \"${this}\"不存在于${data.jsonObject}中")
        return IntoResult()
    }

    inner class IntoResult<T>(val propertyValue: T? = null)

    inline infix fun <reified T> IntoResult<T>.attach(block: Parser.(t: T?) -> Unit) {
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

    inline infix fun <reified T> ThenResult.into(property: KMutableProperty0<T>): IntoResult<T?> {
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

    inline infix fun <reified T> AlterResult.into(property: KMutableProperty0<T>): IntoResult<T?> {
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

    inline infix fun <reified T> CustomParseResult<T>.into(property: KMutableProperty0<in T>): IntoResult<T> {
        property.set(value)
        return IntoResult(value)
    }

    inline fun <reified T> String.parseList(block: Parser.() -> Unit) {

    }
}