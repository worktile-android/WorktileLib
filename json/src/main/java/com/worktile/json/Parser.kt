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

open class Parser(parserData: ParserData) {
    val jsonObject = parserData.jsonObject
    val dsl = parserData.jsonDsl

    inline infix fun <reified T> String.into(property: KMutableProperty0<T>) {
        val value = jsonObject.opt(this)
        value?.run {
            when (T::class) {
                Number::class, String::class -> { property.set(value as T) }
                else -> {
                    when (value) {
                        is JSONObject -> { property.set(dsl.parse(value)) }
                        is JSONArray -> {
                            val list = mutableListOf<T>()
                            for (index in 0 until value.length()) {

                            }
                        }
                        else -> {
                            Log.w(TAG, "使用String.into方法将json解析成另一个类的对象时，需要一个JSONObject，但${value}不是")
                        }
                    }
                }
            }
        } ?: Log.w(TAG, "key \"${this}\"不存在于${jsonObject}中")
    }

    infix fun String.then(block: Parser.() -> Unit): ThenResult {
        val thenObject = jsonObject.opt(this@then)
        thenObject?.run {
            if (thenObject is JSONObject) {
                block(Parser(ParserData(dsl, thenObject)))
            } else {
                Log.w(TAG, "key \"${this@then}\"对应的值不是JSONObject，无法执行then方法")
            }
        } ?: Log.w(TAG, "key \"${this@then}\"不存在于${jsonObject}中")
        return ThenResult(this)
    }

    inner class ThenResult(val key: String)

    inline infix fun <reified T> ThenResult.into(property: KMutableProperty0<T>) {
        key.into(property)
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

    inline infix fun <reified T> AlterResult.into(property: KMutableProperty0<T>) {
        alterKeys.forEach {
            if (jsonObject.has(it)) {
                it.into(property)
                return
            }
        }
    }

    infix fun <T> String.bindList(listProperty: KMutableProperty0<List<T>>) {

    }

    infix fun <T> String.intoList(listProperty: KMutableProperty0<List<T>>) {

    }
}